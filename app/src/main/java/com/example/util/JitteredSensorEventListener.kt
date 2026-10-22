package com.example.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import kotlin.random.Random

enum class ShieldIntensity(
    val label: String,
    val jitterAmplitude: Float,
    val description: String
) {
    OFF("Disabled", 0.0f, "Direct raw sensor pass-through"),
    SUBTLE("Subtle (±0.002)", 0.002f, "Minimal noise, defeats basic hardware calibration fingerprinting"),
    BALANCED("Balanced (±0.005)", 0.005f, "Recommended shield against commercial telemetry & anti-fraud scripts"),
    AGGRESSIVE("Aggressive (±0.010)", 0.010f, "High entropy noise with drift modulation"),
    GHOST_MODE("Ghost Mode (±0.020)", 0.020f, "Maximum anti-tracking protection with thermal drift simulation");

    val maxNoise: Float get() = jitterAmplitude
}

/**
 * Proxy wrapper for Android's SensorEventListener that injects micro-jitters
 * (randomized mathematical noise) into physical sensor readings on the fly.
 * Defeats sensor hardware manufacturing imperfection fingerprinting.
 */
class JitteredSensorEventListener(
    private val originalListener: SensorEventListener? = null,
    private var intensity: ShieldIntensity = ShieldIntensity.BALANCED,
    private val onValuesJittered: ((sensorType: Int, raw: FloatArray, jittered: FloatArray) -> Unit)? = null
) : SensorEventListener {

    companion object {
        fun applyJitter(rawValue: Float, intensity: ShieldIntensity): Float {
            if (intensity.jitterAmplitude <= 0f) return rawValue
            val noise = Random.nextDouble(-intensity.jitterAmplitude.toDouble(), intensity.jitterAmplitude.toDouble()).toFloat()
            val finalNoise = if (noise == 0f) 0.0001f else noise
            return rawValue + finalNoise
        }

        fun applyJitter(rawValues: FloatArray, intensity: ShieldIntensity): FloatArray {
            if (intensity.jitterAmplitude <= 0f) return rawValues.clone()
            val result = rawValues.clone()
            for (i in result.indices) {
                val noise = Random.nextDouble(-intensity.jitterAmplitude.toDouble(), intensity.jitterAmplitude.toDouble()).toFloat()
                result[i] += if (noise == 0f) 0.0001f else noise
            }
            return result
        }
    }

    // Dynamic thermal drift offsets that slowly oscillate to prevent calibration profile matching
    private var driftX = 0f
    private var driftY = 0f
    private var driftZ = 0f
    private var eventCounter = 0L

    fun setIntensity(newIntensity: ShieldIntensity) {
        this.intensity = newIntensity
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val amp = intensity.jitterAmplitude
        val isTargetSensor = event.sensor.type == Sensor.TYPE_ACCELEROMETER ||
            event.sensor.type == Sensor.TYPE_GYROSCOPE ||
            event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD ||
            event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION

        if (amp > 0f && isTargetSensor && event.values.isNotEmpty()) {
            eventCounter++

            // Slowly evolve drift offset every 50 events
            if (eventCounter % 50 == 0L) {
                driftX = (Random.nextDouble(-amp * 0.2, amp * 0.2)).toFloat()
                driftY = (Random.nextDouble(-amp * 0.2, amp * 0.2)).toFloat()
                driftZ = (Random.nextDouble(-amp * 0.2, amp * 0.2)).toFloat()
            }

            // Capture raw copy for telemetry/reporting
            val rawCopy = event.values.clone()

            // Inject microscopic random noise (jitter) into the physical axes
            // This defeats sensor hardware silicon fingerprinting
            val jitterX = Random.nextDouble(-amp.toDouble(), amp.toDouble()).toFloat() + driftX
            val jitterY = Random.nextDouble(-amp.toDouble(), amp.toDouble()).toFloat() + driftY
            val jitterZ = Random.nextDouble(-amp.toDouble(), amp.toDouble()).toFloat() + driftZ

            event.values[0] += jitterX
            if (event.values.size > 1) event.values[1] += jitterY
            if (event.values.size > 2) event.values[2] += jitterZ

            onValuesJittered?.invoke(event.sensor.type, rawCopy, event.values)
        }

        // Pass the modified event to the listener inside the virtual machine
        originalListener?.onSensorChanged(event)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        originalListener?.onAccuracyChanged(sensor, accuracy)
    }

    /**
     * Standalone pure-math jittering function for custom processing or unit tests
     */
    fun applyJitter(rawValues: FloatArray, customAmp: Float = intensity.jitterAmplitude): FloatArray {
        if (customAmp <= 0f) return rawValues.clone()
        val result = rawValues.clone()
        result[0] += Random.nextDouble(-customAmp.toDouble(), customAmp.toDouble()).toFloat()
        if (result.size > 1) {
            result[1] += Random.nextDouble(-customAmp.toDouble(), customAmp.toDouble()).toFloat()
        }
        if (result.size > 2) {
            result[2] += Random.nextDouble(-customAmp.toDouble(), customAmp.toDouble()).toFloat()
        }
        return result
    }
}
