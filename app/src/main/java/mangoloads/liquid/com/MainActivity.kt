package mangoloads.liquid.com

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView

class MainActivity : android.app.Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep this lab Activity full-size when the IME appears so the native
        // cross-window blur has real content behind the keyboard instead of a
        // resized/empty region. This is only for the Liquid Lab test harness.
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(6, 8, 16))
        setContentView(root)

        root.addView(AnimatedBackdropView(this), FrameLayout.LayoutParams(-1, -1))

        val glass = LiquidGlassView(this).apply {
            enableDynamicBackground = true
            material = GlassMaterial.CLEAR
            cornerRadius = 34f * resources.displayMetrics.density
            refractionHeight = 72f
            bevelWidth = 28f
            dispersionStrength = 0.08f
            enableSensorHighlight = true
            enableAdaptiveTint = true
        }

        glass.addView(
            TextView(this).apply {
                text = "LIQUID GLASS\nOptical lens test"
                textSize = 21f
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
            },
            FrameLayout.LayoutParams(-1, -1)
        )

        root.addView(
            glass,
            FrameLayout.LayoutParams(
                (300 * resources.displayMetrics.density).toInt(),
                (132 * resources.displayMetrics.density).toInt()
            ).apply { gravity = Gravity.CENTER }
        )

        val edit = EditText(this).apply {
            hint = "Tap here to test the glass keyboard"
            textSize = 18f
            setTextColor(Color.WHITE)
            setHintTextColor(0xAAFFFFFF.toInt())
            setBackgroundColor(0xFF1457FF.toInt())
            setSingleLine(false)
            setPadding(
                (16 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt(),
                (16 * resources.displayMetrics.density).toInt(),
                (12 * resources.displayMetrics.density).toInt()
            )
        }
        root.addView(
            edit,
            FrameLayout.LayoutParams(-1, (82 * resources.displayMetrics.density).toInt()).apply {
                gravity = Gravity.BOTTOM
                leftMargin = (20 * resources.displayMetrics.density).toInt()
                rightMargin = (20 * resources.displayMetrics.density).toInt()
                bottomMargin = (90 * resources.displayMetrics.density).toInt()
            }
        )

        val button = Button(this).apply {
            text = "Open keyboard settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        root.addView(
            button,
            FrameLayout.LayoutParams(-1, (62 * resources.displayMetrics.density).toInt()).apply {
                gravity = Gravity.BOTTOM
                leftMargin = (20 * resources.displayMetrics.density).toInt()
                rightMargin = (20 * resources.displayMetrics.density).toInt()
                bottomMargin = (18 * resources.displayMetrics.density).toInt()
            }
        )

        val info = TextView(this).apply {
            text = "Phase 1: optical refraction\nPhase 2: native cross-window ambient haze"
            textSize = 12f
            setTextColor(0xDDFFFFFF.toInt())
            setPadding(12, 6, 12, 6)
            setBackgroundColor(0x40000000)
        }
        root.addView(
            info,
            FrameLayout.LayoutParams(-2, -2).apply {
                gravity = Gravity.TOP or Gravity.START
                leftMargin = (16 * resources.displayMetrics.density).toInt()
                topMargin = (24 * resources.displayMetrics.density).toInt()
            }
        )

        edit.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.post {
                    getSystemService(InputMethodManager::class.java)
                        ?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }
}
