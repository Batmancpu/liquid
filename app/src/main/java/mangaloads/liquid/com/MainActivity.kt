package mangoloads.liquid.com

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView
import java.lang.ref.WeakReference

class MainActivity : android.app.Activity() {
    private var rootRef: WeakReference<View>? = null

    companion object {
        @Volatile
        private var activeBackdropRoot: WeakReference<View>? = null

        fun activeBackdropSource(): View? {
            val view = activeBackdropRoot?.get()
            return view?.takeIf { it.isAttachedToWindow }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )

        val root = FrameLayout(this)
        root.setBackgroundColor(Color.rgb(6, 8, 16))
        setContentView(root)

        rootRef = WeakReference(root)
        activeBackdropRoot = rootRef

        // Animated colour field.
        root.addView(
            AnimatedBackdropView(this),
            FrameLayout.LayoutParams(-1, -1)
        )

        // Straight white optical reference lines. These intentionally sit
        // behind every LiquidGlassView so bending/compression is immediately
        // visible when the lens is working.
        root.addView(
            OpticalGuideLinesView(this),
            FrameLayout.LayoutParams(-1, -1)
        )

        // Standalone optical test surface.
        val glass = LiquidGlassView(this).apply {
            enableDynamicBackground = true
            enableBackdropBlur = true
            useShaderPipeline = true
            material = GlassMaterial.CLEAR

            cornerRadius = 38f * resources.displayMetrics.density
            bevelWidth = 30f
            refractionHeight = 52f
            refractionFalloff = 1.4f
            edgeSoftness = 2.0f
            dispersionStrength = 0.05f

            // Keep the material optically clear while testing geometry.
            glassTint = Color.TRANSPARENT
            enableAdaptiveTint = false
            enableSensorHighlight = true
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
                (320 * resources.displayMetrics.density).toInt(),
                (160 * resources.displayMetrics.density).toInt()
            ).apply {
                gravity = Gravity.CENTER
            }
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
            FrameLayout.LayoutParams(
                -1,
                (82 * resources.displayMetrics.density).toInt()
            ).apply {
                gravity = Gravity.BOTTOM
                leftMargin = (20 * resources.displayMetrics.density).toInt()
                rightMargin = (20 * resources.displayMetrics.density).toInt()
                bottomMargin = (90 * resources.displayMetrics.density).toInt()
            }
        )

        val button = Button(this).apply {
            text = "Open keyboard settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }

        root.addView(
            button,
            FrameLayout.LayoutParams(
                -1,
                (62 * resources.displayMetrics.density).toInt()
            ).apply {
                gravity = Gravity.BOTTOM
                leftMargin = (20 * resources.displayMetrics.density).toInt()
                rightMargin = (20 * resources.displayMetrics.density).toInt()
                bottomMargin = (18 * resources.displayMetrics.density).toInt()
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

    override fun onDestroy() {
        val current = activeBackdropRoot?.get()
        if (current === rootRef?.get()) {
            activeBackdropRoot = null
        }
        rootRef = null
        super.onDestroy()
    }
}

private class OpticalGuideLinesView(context: android.content.Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.0f * resources.displayMetrics.density
        color = 0xDFFFFFFF.toInt()
    }

    override fun onDraw(canvas: Canvas) {
        val d = resources.displayMetrics.density
        val inset = 24f * d
        val usable = width - inset * 2f
        val count = 11

        for (i in 0 until count) {
            val x = inset + usable * i / (count - 1)
            canvas.drawLine(
                x,
                120f * d,
                x,
                height - 20f * d,
                paint
            )
        }
    }
}
