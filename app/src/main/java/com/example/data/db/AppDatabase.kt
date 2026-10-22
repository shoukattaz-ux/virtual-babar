package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ImportedAppEntity
import com.example.data.model.ProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [ProfileEntity::class, ImportedAppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao
    abstract fun importedAppDao(): ImportedAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "virtual_space_db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialProfiles(database.profileDao())
                    }
                }
            }

            private suspend fun populateInitialProfiles(profileDao: ProfileDao) {
                // Seed the default Virtual Machine Main Profile
                val defaultVmProfile = ProfileEntity(
                    profileName = "Virtual Machine Main Profile",
                    androidId = "8f4b1a20c93e47d2",
                    deviceModel = "Pixel 8 Pro",
                    deviceBrand = "Google",
                    macAddress = "74:D0:2B:6C:8E:19",
                    serialNumber = "ZX1G224H89B",
                    isDefaultVm = true
                )
                profileDao.insertProfile(defaultVmProfile)
            }
        }
    }
}
