package mangoloads.liquid.com

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * No-screen-capture ambient light source.
 *
 * TYPE_LIGHT supplies real-world illumination in lux.
 * TYPE_GRAVITY supplies the device tilt used to move the glass highlight.
 *
 * This is intentionally physical ambient illumination, not screen color.
 */
internal class AmbientLightController(context: Context) : SensorEventListener {

    data class State(
        val lux: Float = 20f,
        val intensity: Float = 0.32f,
        val lightX: Float = 0f,
        val lightY: Float = -0.55f
    )

    private val manager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val lightSensor =
        manager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val gravitySensor =
        manager.getDefaultSensor(Sensor.TYPE_GRAVITY)

    @Volatile
    private var state = State()

    private var running = false

    fun start() {
        if (running) return
        running = true

        lightSensor?.let {
            manager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        gravitySensor?.let {
            manager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stop() {
        if (!running) return
        running = false
        manager.unregisterListener(this)
    }

    fun current(): State = state

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LIGHT -> {
                val lux = event.values.firstOrNull()?.coerceAtLeast(0f) ?: return

                // Log-shaped response: indoor changes remain visible without
                // making bright sunlight saturate the material.
                val intensity =
                    (kotlin.math.ln(1f + lux) / kotlin.math.ln(1f + 500f))
                        .coerceIn(0f, 1f)

                val old = state
                state = old.copy(
                    lux = lux,
                    intensity = intensity
                )
            }

            Sensor.TYPE_GRAVITY -> {
                val gx = event.values.getOrNull(0) ?: 0f
                val gy = event.values.getOrNull(1) ?: -9.8f

                val x = (-gx / 9.8f).coerceIn(-1f, 1f)
                val y = (-gy / 9.8f).coerceIn(-1f, 1f)

                val old = state
                state = old.copy(
                    lightX = x,
                    lightY = y
                )
            }
        }
    }
}
