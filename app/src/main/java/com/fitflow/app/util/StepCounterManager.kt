package com.fitflow.app.util

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StepCounterManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fitflow_steps", Context.MODE_PRIVATE)

    private val _todaySteps = MutableStateFlow(0)
    val todaySteps: StateFlow<Int> = _todaySteps

    val isSensorAvailable: Boolean get() = stepSensor != null

    fun start() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val totalSinceBoot = event.values[0].toInt()

        val todayKey = DateUtils.todayKey()
        val storedDay = prefs.getString(KEY_BASELINE_DAY, null)
        var baseline = prefs.getInt(KEY_BASELINE_VALUE, -1)

        if (storedDay != todayKey || baseline == -1 || totalSinceBoot < baseline) {
            baseline = totalSinceBoot
            prefs.edit()
                .putString(KEY_BASELINE_DAY, todayKey)
                .putInt(KEY_BASELINE_VALUE, baseline)
                .apply()
        }

        val steps = (totalSinceBoot - baseline).coerceAtLeast(0)
        _todaySteps.value = steps
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no-op
    }

    companion object {
        private const val KEY_BASELINE_DAY = "baseline_day"
        private const val KEY_BASELINE_VALUE = "baseline_value"
    }
}
