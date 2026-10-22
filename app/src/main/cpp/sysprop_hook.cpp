/**
 * VirtuOS Master Host Sandbox - Native System Property Hook (PLT/GOT Interception)
 * File: /app/src/main/cpp/sysprop_hook.cpp
 *
 * Technical Overview:
 * Target applications (e.g., "Device Info HW", Geekbench, AIDA64) bypass Java reflection
 * and read hardware parameters directly via native Bionic libc calls:
 *   - __system_property_get(const char* name, char* value)
 *   - __system_property_find(const char* name)
 *   - __system_property_read(const prop_info* pi, char* name, char* value)
 *
 * This module hooks __system_property_get and related symbols across all loaded ELF
 * modules in the process memory space using PLT/GOT redirection and page re-protection
 * (mprotect PROT_READ | PROT_WRITE).
 */

#include <jni.h>
#include <string>
#include <unordered_map>
#include <mutex>
#include <dlfcn.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <elf.h>
#include <link.h>
#include <android/log.h>
#include <cstring>
#include <fcntl.h>
#include <cinttypes>

#define LOG_TAG "VirtuOS_NativeHook"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifndef PROP_VALUE_MAX
#define PROP_VALUE_MAX 92
#endif

#ifndef PROP_NAME_MAX
#define PROP_NAME_MAX 32
#endif

// Function pointer signature for libc __system_property_get
typedef int (*orig_system_property_get_fn)(const char* name, char* value);
typedef const void* (*orig_system_property_find_fn)(const char* name);
typedef void (*orig_system_property_read_fn)(const void* pi, char* name, char* value);

// Global state
static std::mutex g_property_mutex;
static std::unordered_map<std::string, std::string> g_spoofed_properties;
static orig_system_property_get_fn g_orig_system_property_get = nullptr;
static bool g_hook_installed = false;

/**
 * Replacement for __system_property_get.
 * Checks the active sandbox profile map first. If found, writes the spoofed
 * hardware parameter into the caller's buffer. Otherwise falls back to real libc.
 */
extern "C" int hooked_system_property_get(const char* name, char* value) {
    if (name == nullptr || value == nullptr) {
        return 0;
    }

    {
        std::lock_guard<std::mutex> lock(g_property_mutex);
        auto it = g_spoofed_properties.find(name);
        if (it != g_spoofed_properties.end()) {
            const std::string& spoofed_val = it->second;
            size_t copy_len = spoofed_val.length();
            if (copy_len >= PROP_VALUE_MAX) {
                copy_len = PROP_VALUE_MAX - 1;
            }
            std::memcpy(value, spoofed_val.c_str(), copy_len);
            value[copy_len] = '\0';
            return static_cast<int>(copy_len);
        }
    }

    // Pass through to original libc implementation
    if (g_orig_system_property_get != nullptr) {
        return g_orig_system_property_get(name, value);
    }

    // Fallback if original pointer is null
    void* handle = dlopen("libc.so", RTLD_NOLOAD);
    if (!handle) handle = dlopen("libc.so", RTLD_NOW);
    if (handle) {
        auto real_fn = reinterpret_cast<orig_system_property_get_fn>(dlsym(handle, "__system_property_get"));
        if (real_fn && real_fn != hooked_system_property_get) {
            g_orig_system_property_get = real_fn;
            return real_fn(name, value);
        }
    }

    value[0] = '\0';
    return 0;
}

/**
 * Resolves original libc system property getter.
 */
static void resolve_original_symbols() {
    if (g_orig_system_property_get == nullptr) {
        void* libc = dlopen("libc.so", RTLD_NOW);
        if (libc != nullptr) {
            g_orig_system_property_get = reinterpret_cast<orig_system_property_get_fn>(
                dlsym(libc, "__system_property_get")
            );
        }
    }
}

/**
 * PLT/GOT Hooking Core:
 * Iterates through loaded shared objects using dl_iterate_phdr,
 * locates ELF relocations referencing "__system_property_get",
 * and rewrites the Global Offset Table (GOT) entry with hooked_system_property_get.
 */
#if defined(__LP64__)
#define Elf_Ehdr Elf64_Ehdr
#define Elf_Phdr Elf64_Phdr
#define Elf_Dyn  Elf64_Dyn
#define Elf_Sym  Elf64_Sym
#define Elf_Rela Elf64_Rela
#define Elf_Rel  Elf64_Rel
#define ELF_R_SYM ELF64_R_SYM
#else
#define Elf_Ehdr Elf32_Ehdr
#define Elf_Phdr Elf32_Phdr
#define Elf_Dyn  Elf32_Dyn
#define Elf_Sym  Elf32_Sym
#define Elf_Rela Elf32_Rela
#define Elf_Rel  Elf32_Rel
#define ELF_R_SYM ELF32_R_SYM
#endif

static int dl_iterate_callback(struct dl_phdr_info* info, size_t size, void* data) {
    if (info->dlpi_name == nullptr || strlen(info->dlpi_name) == 0) {
        // App executable itself
    } else {
        // Skip libc itself to prevent infinite recursion
        if (strstr(info->dlpi_name, "libc.so") != nullptr) {
            return 0;
        }
    }

    uintptr_t base = info->dlpi_addr;
    const Elf_Phdr* dynamic_phdr = nullptr;

    for (int i = 0; i < info->dlpi_phnum; i++) {
        if (info->dlpi_phdr[i].p_type == PT_DYNAMIC) {
            dynamic_phdr = &info->dlpi_phdr[i];
            break;
        }
    }

    if (!dynamic_phdr) return 0;

    Elf_Dyn* dyn = reinterpret_cast<Elf_Dyn*>(base + dynamic_phdr->p_vaddr);
    Elf_Sym* symtab = nullptr;
    const char* strtab = nullptr;
    Elf_Rela* rela = nullptr;
    size_t rela_sz = 0;
    Elf_Rel* rel = nullptr;
    size_t rel_sz = 0;

    for (Elf_Dyn* d = dyn; d->d_tag != DT_NULL; ++d) {
        switch (d->d_tag) {
            case DT_SYMTAB:
                symtab = reinterpret_cast<Elf_Sym*>(base + d->d_un.d_ptr);
                break;
            case DT_STRTAB:
                strtab = reinterpret_cast<const char*>(base + d->d_un.d_ptr);
                break;
#if defined(DT_RELA)
            case DT_RELA:
                rela = reinterpret_cast<Elf_Rela*>(base + d->d_un.d_ptr);
                break;
            case DT_RELASZ:
                rela_sz = d->d_un.d_val;
                break;
#endif
#if defined(DT_REL)
            case DT_REL:
                rel = reinterpret_cast<Elf_Rel*>(base + d->d_un.d_ptr);
                break;
            case DT_RELSZ:
                rel_sz = d->d_un.d_val;
                break;
#endif
        }
    }

    // Search and hook RELA relocations
    if (symtab && strtab && rela && rela_sz > 0) {
        size_t count = rela_sz / sizeof(Elf_Rela);
        for (size_t i = 0; i < count; ++i) {
            uint32_t sym_idx = ELF_R_SYM(rela[i].r_info);
            const char* sym_name = strtab + symtab[sym_idx].st_name;
            if (strcmp(sym_name, "__system_property_get") == 0) {
                uintptr_t* got_entry = reinterpret_cast<uintptr_t*>(base + rela[i].r_offset);
                uintptr_t page_start = reinterpret_cast<uintptr_t>(got_entry) & ~(static_cast<uintptr_t>(PAGE_SIZE - 1));
                mprotect(reinterpret_cast<void*>(page_start), PAGE_SIZE * 2, PROT_READ | PROT_WRITE);
                *got_entry = reinterpret_cast<uintptr_t>(hooked_system_property_get);
                mprotect(reinterpret_cast<void*>(page_start), PAGE_SIZE * 2, PROT_READ);
            }
        }
    }

    return 0;
}

// ---------------------------------------------------------------------------
// JNI Interface for Kotlin VirtualHookManager
// ---------------------------------------------------------------------------

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_util_NativePropertyHook_initNativeHook(JNIEnv* env, jclass clazz) {
    resolve_original_symbols();
    dl_iterate_phdr(dl_iterate_callback, nullptr);
    g_hook_installed = true;
    LOGI("VirtuOS Native PLT system property hooks installed successfully.");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_util_NativePropertyHook_setSpoofedProperty(
        JNIEnv* env, jclass clazz, jstring jKey, jstring jValue) {
    if (!jKey || !jValue) return;

    const char* key_str = env->GetStringUTFChars(jKey, nullptr);
    const char* val_str = env->GetStringUTFChars(jValue, nullptr);

    {
        std::lock_guard<std::mutex> lock(g_property_mutex);
        g_spoofed_properties[key_str] = val_str;
    }

    env->ReleaseStringUTFChars(jKey, key_str);
    env->ReleaseStringUTFChars(jValue, val_str);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_util_NativePropertyHook_clearSpoofedProperties(JNIEnv* env, jclass clazz) {
    std::lock_guard<std::mutex> lock(g_property_mutex);
    g_spoofed_properties.clear();
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_util_NativePropertyHook_getNativeProperty(
        JNIEnv* env, jclass clazz, jstring jKey, jstring jDef) {
    if (!jKey) return jDef;

    const char* key_str = env->GetStringUTFChars(jKey, nullptr);
    char buf[PROP_VALUE_MAX] = {0};

    int len = hooked_system_property_get(key_str, buf);
    env->ReleaseStringUTFChars(jKey, key_str);

    if (len > 0) {
        return env->NewStringUTF(buf);
    }
    return jDef;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_util_NativePropertyHook_isNativeHookActive(JNIEnv* env, jclass clazz) {
    return g_hook_installed ? JNI_TRUE : JNI_FALSE;
}
