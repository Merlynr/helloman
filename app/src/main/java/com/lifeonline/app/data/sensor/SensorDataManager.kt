package com.lifeonline.app.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorDataManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val prefs = context.getSharedPreferences("sensor_steps", Context.MODE_PRIVATE)

    private val _currentSteps = MutableStateFlow(0)
    val currentSteps: StateFlow<Int> = _currentSteps.asStateFlow()

    init {
        loadTodaySteps()
        registerListener()
    }

    private fun getTodayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun loadTodaySteps() {
        val today = getTodayDate()
        val savedDate = prefs.getString("saved_date", "") ?: ""
        if (savedDate == today) {
            _currentSteps.value = prefs.getInt("today_steps", 0)
        } else {
            // 新的一天，重置当日步数基准
            prefs.edit()
                .putString("saved_date", today)
                .putInt("today_steps", 0)
                .putFloat("baseline_hardware_steps", -1f)
                .apply()
            _currentSteps.value = 0
        }
    }

    fun registerListener() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun unregisterListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val totalHardwareSteps = event.values[0]
            val today = getTodayDate()
            val savedDate = prefs.getString("saved_date", "") ?: ""

            var baseline = prefs.getFloat("baseline_hardware_steps", -1f)

            if (savedDate != today || baseline < 0f) {
                baseline = totalHardwareSteps
                prefs.edit()
                    .putString("saved_date", today)
                    .putFloat("baseline_hardware_steps", baseline)
                    .apply()
            }

            val todaySteps = (totalHardwareSteps - baseline).toInt().coerceAtLeast(0)
            prefs.edit().putInt("today_steps", todaySteps).apply()
            _currentSteps.value = todaySteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * 手动微调或模拟步数（便于测试与无计步传感器设备）
     */
    fun addManualSteps(steps: Int) {
        val updated = _currentSteps.value + steps
        _currentSteps.value = updated
        prefs.edit().putInt("today_steps", updated).apply()
    }
}
