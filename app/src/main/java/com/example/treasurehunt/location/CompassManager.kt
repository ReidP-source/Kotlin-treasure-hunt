package com.example.treasurehunt.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
/**
 *  Reid Pettibone
 *  CS 492
 *  OSU
 * */
class CompassManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _headingDegrees = MutableStateFlow<Float?>(null)
    val headingDegrees: StateFlow<Float?> = _headingDegrees.asStateFlow()
    private var isRunning = false

    fun start() {
        if (isRunning) return
        rotationSensor?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
            isRunning = true
        }
    }

    fun stop() {
        if (!isRunning) return
        sensorManager.unregisterListener(this)
        isRunning = false
        _headingDegrees.value = null
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ROTATION_VECTOR) return
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        var heading = Math.toDegrees(orientation[0].toDouble()).toFloat()
        if (heading < 0f) heading += 360f
        _headingDegrees.value = heading
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
