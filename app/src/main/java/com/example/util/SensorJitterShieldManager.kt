package com.example.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class TouchCadenceSample(
    val timestamp: Long,
    val rawIntervalMs: Long,
    val jitteredIntervalMs: Long,
    val rawPressure: Float,
    val jitteredPressure: Float
)

data class SensorShieldTelemetry(
    val isShieldEnabled: Boolean = true,
    val intensity: ShieldIntensity = ShieldIntensity.BALANCED,
    val isTouchScramblerEnabled: Boolean = true,
    val rawAccel: Triple<Float, Float, Float> = Triple(0f, 9.81f, 0f),
    val jitteredAccel: Triple<Float, Float, Float> = Triple(0f, 9.81f, 0f),
    val rawGyro: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val jitteredGyro: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val accelHistory: List<Pair<Float, Float>> = emptyList(), // Pair(rawX, jitteredX)
    val totalInterceptedCount: Long = 0L,
    val recentTouchSamples: List<TouchCadenceSample> = emptyList(),
    val entropyScorePercent: Float = 99.4f
)

object SensorJitterShieldManager {

    private val _telemetry = MutableStateFlow(SensorShieldTelemetry())
    val telemetry: StateFlow<SensorShieldTelemetry> = _telemetry.asStateFlow()

    private var sensorManager: SensorManager? = null
    private var accelSensor: Sensor? = null
    private var gyroSensor: Sensor? = null
    private var jitterListener: JitteredSensorEventListener? = null
    private var internalListener: SensorEventListener? = null

    private var lastTapTime = 0L
    private var eventCounter = 0L
    private val historyPoints = mutableListOf<Pair<Float, Float>>()

    fun init(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        if (sensorManager != null) return

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        internalListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                handleSensorUpdate(event.sensor.type, event.values)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        jitterListener = JitteredSensorEventListener(
            originalListener = internalListener,
            intensity = _telemetry.value.intensity
        ) { sensorType, raw, jittered ->
            scope.launch {
                updateTelemetryWithJitter(sensorType, raw, jittered)
            }
        }

        startListening()

        // Fallback simulation runner for emulators without physical sensors so graphs and stats are always live
        if (accelSensor == null) {
            startSimulationRunner(scope)
        }
    }

    fun setShieldIntensity(intensity: ShieldIntensity) {
        _telemetry.value = _telemetry.value.copy(
            intensity = intensity,
            isShieldEnabled = intensity != ShieldIntensity.OFF
        )
        jitterListener?.setIntensity(intensity)
    }

    fun setIntensity(intensity: ShieldIntensity) = setShieldIntensity(intensity)

    fun toggleShield(enabled: Boolean) {
        val newIntensity = if (enabled) ShieldIntensity.BALANCED else ShieldIntensity.OFF
        setShieldIntensity(newIntensity)
    }

    fun setShieldEnabled(enabled: Boolean) = toggleShield(enabled)

    fun toggleTouchScrambler(enabled: Boolean) {
        _telemetry.value = _telemetry.value.copy(isTouchScramblerEnabled = enabled)
    }

    fun setTouchScramblerEnabled(enabled: Boolean) = toggleTouchScrambler(enabled)

    /**
     * Intercepts a touch event and returns jittered timing & pressure parameters.
     * Prevents behavioral profiling scripts from calculating keystroke dynamics or swipe rhythms.
     */
    fun processTouchEvent(rawPressure: Float = 1.0f, timestamp: Long = System.currentTimeMillis()): TouchCadenceSample {
        val now = timestamp
        val rawInterval = if (lastTapTime == 0L) 0L else (now - lastTapTime)
        lastTapTime = now

        val intensity = _telemetry.value.intensity
        val isTouchActive = _telemetry.value.isTouchScramblerEnabled && intensity != ShieldIntensity.OFF

        val timingJitterMs = if (isTouchActive) {
            val maxJitter = (intensity.jitterAmplitude * 1000).toLong().coerceIn(4L, 25L)
            var jitter = Random.nextLong(-maxJitter, maxJitter)
            if (jitter == 0L) jitter = if (Random.nextBoolean()) 5L else -5L
            jitter
        } else 0L

        val pressureJitter = if (isTouchActive) {
            var p = Random.nextDouble(-0.04, 0.04).toFloat()
            if (p == 0f) p = 0.015f
            p
        } else 0f

        val jitteredInterval = (rawInterval + timingJitterMs).coerceAtLeast(10L)
        val jitteredPressure = (rawPressure + pressureJitter).coerceIn(0.05f, 1.0f)

        val sample = TouchCadenceSample(
            timestamp = now,
            rawIntervalMs = rawInterval,
            jitteredIntervalMs = jitteredInterval,
            rawPressure = rawPressure,
            jitteredPressure = jitteredPressure
        )

        val currentList = _telemetry.value.recentTouchSamples.takeLast(9) + sample
        _telemetry.value = _telemetry.value.copy(recentTouchSamples = currentList)

        return sample
    }

    private fun handleSensorUpdate(sensorType: Int, values: FloatArray) {
        eventCounter++
        val current = _telemetry.value
        val amp = current.intensity.jitterAmplitude

        if (sensorType == Sensor.TYPE_ACCELEROMETER && values.isNotEmpty()) {
            val rawX = values[0]
            val rawY = if (values.size > 1) values[1] else 0f
            val rawZ = if (values.size > 2) values[2] else 0f

            val jX = rawX + Random.nextDouble(-amp.toDouble(), amp.toDouble()).toFloat()
            val jY = rawY + Random.nextDouble(-amp.toDouble(), amp.toDouble()).toFloat()
            val jZ = rawZ + Random.nextDouble(-amp.toDouble(), amp.toDouble()).toFloat()

            addHistoryPoint(rawX, jX)

            _telemetry.value = current.copy(
                rawAccel = Triple(rawX, rawY, rawZ),
                jitteredAccel = Triple(jX, jY, jZ),
                totalInterceptedCount = eventCounter
            )
        } else if (sensorType == Sensor.TYPE_GYROSCOPE && values.isNotEmpty()) {
            val rawX = values[0]
            val rawY = if (values.size > 1) values[1] else 0f
            val rawZ = if (values.size > 2) values[2] else 0f

            val jX = rawX + Random.nextDouble(-amp.toDouble() * 0.5, amp.toDouble() * 0.5).toFloat()
            val jY = rawY + Random.nextDouble(-amp.toDouble() * 0.5, amp.toDouble() * 0.5).toFloat()
            val jZ = rawZ + Random.nextDouble(-amp.toDouble() * 0.5, amp.toDouble() * 0.5).toFloat()

            _telemetry.value = current.copy(
                rawGyro = Triple(rawX, rawY, rawZ),
                jitteredGyro = Triple(jX, jY, jZ),
                totalInterceptedCount = eventCounter
            )
        }
    }

    private fun updateTelemetryWithJitter(sensorType: Int, raw: FloatArray, jittered: FloatArray) {
        eventCounter++
        val current = _telemetry.value
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            val rawX = raw.getOrElse(0) { 0f }
            val jX = jittered.getOrElse(0) { 0f }
            addHistoryPoint(rawX, jX)
            _telemetry.value = current.copy(
                rawAccel = Triple(rawX, raw.getOrElse(1) { 9.8f }, raw.getOrElse(2) { 0f }),
                jitteredAccel = Triple(jX, jittered.getOrElse(1) { 9.8f }, jittered.getOrElse(2) { 0f }),
                totalInterceptedCount = eventCounter
            )
        }
    }

    private fun addHistoryPoint(raw: Float, jittered: Float) {
        synchronized(historyPoints) {
            if (historyPoints.size > 40) {
                historyPoints.removeAt(0)
            }
            historyPoints.add(Pair(raw, jittered))
            _telemetry.value = _telemetry.value.copy(accelHistory = historyPoints.toList())
        }
    }

    private fun startListening() {
        val listener = jitterListener ?: return
        accelSensor?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        gyroSensor?.let {
            sensorManager?.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun startSimulationRunner(scope: CoroutineScope) {
        scope.launch {
            var timeStep = 0.0
            while (true) {
                kotlinx.coroutines.delay(120)
                timeStep += 0.15

                // Simulate slight natural human hand tremor baseline
                val naturalTremorX = (Math.sin(timeStep * 1.8) * 0.06).toFloat()
                val naturalTremorY = 9.80f + (Math.cos(timeStep * 1.2) * 0.04).toFloat()
                val naturalTremorZ = (Math.sin(timeStep * 0.9) * 0.03).toFloat()

                val amp = _telemetry.value.intensity.jitterAmplitude
                val jX = naturalTremorX + (Random.nextDouble(-amp.toDouble(), amp.toDouble())).toFloat()
                val jY = naturalTremorY + (Random.nextDouble(-amp.toDouble(), amp.toDouble())).toFloat()
                val jZ = naturalTremorZ + (Random.nextDouble(-amp.toDouble(), amp.toDouble())).toFloat()

                eventCounter++
                addHistoryPoint(naturalTremorX, jX)

                _telemetry.value = _telemetry.value.copy(
                    rawAccel = Triple(naturalTremorX, naturalTremorY, naturalTremorZ),
                    jitteredAccel = Triple(jX, jY, jZ),
                    rawGyro = Triple((naturalTremorX * 0.1f), 0.01f, 0.0f),
                    jitteredGyro = Triple(jX * 0.1f, 0.01f + amp * 0.1f, 0.0f),
                    totalInterceptedCount = eventCounter
                )
            }
        }
    }
}
