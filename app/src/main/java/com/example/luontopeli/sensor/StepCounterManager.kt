// 📁 sensor/StepCounterManager.kt
package com.example.luontopeli.sensor // Varmista että paketti on oikein!

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Sensorien hallintapalvelu askelmittarille ja gyroskoopille.
 */
class StepCounterManager(context: Context) {

    /** Android-järjestelmän SensorManager sensorien rekisteröimiseen */
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /** Askeltunnistin-sensori (null jos laite ei tue sitä) */
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    /** Gyroskooppi-sensori (null jos laite ei tue sitä) */
    private val gyroSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var stepListener: SensorEventListener? = null
    private var gyroListener: SensorEventListener? = null

    // Ravistuksen tunnistuksen aputila
    private var lastShakeTime = 0L

    /**
     * Käynnistää askelten laskemisen.
     */
    fun startStepCounting(onStep: () -> Unit) {
        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                    onStep()
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        stepSensor?.let {
            sensorManager.registerListener(
                stepListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }

    /**
     * Käynnistää gyroskoopin lukemisen.
     */
    fun startGyroscope(onRotation: (Float, Float, Float) -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    onRotation(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        gyroSensor?.let {
            sensorManager.registerListener(
                gyroListener,
                it,
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
    }

    fun isStepSensorAvailable(): Boolean = stepSensor != null

    /**
     * Ravistustunnistus gyroskooppi-datan perusteella.
     * Kutsutaan yleensä gyroskoopin callbackin sisällä.
     */
    fun detectShake(x: Float, y: Float, z: Float): Boolean {
        // Laske pyörimisnopeuden suuruus (vektorimagnitudi)
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
        val now = System.currentTimeMillis()

        // Tunnista ravistus jos yli kynnysarvon ja cooldown kulunut
        if (magnitude > SHAKE_THRESHOLD && now - lastShakeTime > SHAKE_COOLDOWN) {
            lastShakeTime = now
            return true  // Ravistus havaittu!
        }
        return false
    }

    companion object {
        /** Keskimääräinen askelpituus metreinä matkan laskemiseen */
        const val STEP_LENGTH_METERS = 0.74f

        /** Kuinka voimakas ravistus vaaditaan (rad/s) */
        const val SHAKE_THRESHOLD = 5.0f

        /** Aika millisekunteina, jotta ei tunnisteta samaa ravistusta useasti peräkkäin */
        const val SHAKE_COOLDOWN = 1000L
    }
}