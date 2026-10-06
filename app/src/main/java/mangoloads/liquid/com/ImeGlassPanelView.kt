package mangoloads.liquid.com

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView
import kotlin.math.max

class ImeGlassPanelView(context: android.content.Context) : FrameLayout(context) {
    internal enum class Action { TEXT, BACKSPACE, ENTER, SHIFT, SPACE, MODE }

    internal data class KeyDef(
        val label: String,
        val action: Action,
        val value: String = "",
        val weight: Float = 1f
    )

    private var pressedKey: KeyDef? = null
    private var shift = false
    private var numeric = false

    private val surfaceView: KeyboardGlassSurfaceView
    private val glassView: LiquidGlassView
    private val foregroundView: KeyboardForegroundView

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    init {
        isClickable = false
        clipChildren = false
        setBackgroundColor(Color.TRANSPARENT)

        // Base keyboard geometry goes underneath the optical surface.
        surfaceView = KeyboardGlassSurfaceView(context, this)
        addView(
            surfaceView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // REAL Liquid Glass lens. The backdrop source is the Lab Activity
        // window, not the keyboard's own sibling views.
        glassView = LiquidGlassView(context).apply {
            enableDynamicBackground = true
            enableBackdropBlur = true
            useShaderPipeline = true
            material = GlassMaterial.CLEAR

            cornerRadius = dp(38f)
            bevelWidth = dp(30f)
            refractionHeight = 52f
            refractionFalloff = 1.4f
            edgeSoftness = 2.0f
            dispersionStrength = 0.045f

            glassTint = Color.TRANSPARENT
            enableAdaptiveTint = false
            enableSensorHighlight = true

            isClickable = false
            isFocusable = false
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }

        addView(
            glassView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Text remains crisp above the glass.
        foregroundView = KeyboardForegroundView(context, this)
        addView(
            foregroundView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
    }

    internal fun refreshBackdropMode() {
        val root = MainActivity.activeBackdropSource()
        val valid = root != null && root.isShown && root.isAttachedToWindow

        if (valid) {
            glassView.backdropSource = root
            glassView.enableDynamicBackground = true
            glassView.visibility = View.VISIBLE
        } else {
            glassView.backdropSource = null
            glassView.enableDynamicBackground = false
            glassView.visibility = View.INVISIBLE
        }

        (context as? LiquidImeService)?.updateOpticalBackdropAvailable(valid)
        invalidateAll()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desired = dp(286f).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(desired, MeasureSpec.getSize(heightMeasureSpec))
            else -> desired
        }

        setMeasuredDimension(width, height)

        val ws = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val hs = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        surfaceView.measure(ws, hs)
        glassView.measure(ws, hs)
        foregroundView.measure(ws, hs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        surfaceView.layout(0, 0, width, height)
        glassView.layout(0, 0, width, height)
        foregroundView.layout(0, 0, width, height)
    }

    internal fun outerRect(): RectF = RectF(
        dp(6f),
        dp(5f),
        width - dp(6f),
        height - dp(5f)
    )

    internal fun currentRows(): List<List<KeyDef>> {
        if (numeric) {
            return listOf(
                "1234567890".map { KeyDef(it.toString(), Action.TEXT, it.toString()) },
                listOf(
                    KeyDef("-", Action.TEXT, "-"), KeyDef("/", Action.TEXT, "/"),
                    KeyDef(":", Action.TEXT, ":"), KeyDef(";", Action.TEXT, ";"),
                    KeyDef("(", Action.TEXT, "("), KeyDef(")", Action.TEXT, ")"),
                    KeyDef("$", Action.TEXT, "$"), KeyDef("&", Action.TEXT, "&"),
                    KeyDef("@", Action.TEXT, "@")
                ),
                listOf(
                    KeyDef("ABC", Action.MODE, "ABC"), KeyDef(".", Action.TEXT, "."),
                    KeyDef(",", Action.TEXT, ","), KeyDef("?", Action.TEXT, "?"),
                    KeyDef("!", Action.TEXT, "!"), KeyDef("⌫", Action.BACKSPACE)
                ),
                listOf(
                    KeyDef("ABC", Action.MODE, "ABC", 1.1f),
                    KeyDef("SPACE", Action.SPACE, " ", 4f),
                    KeyDef("↵", Action.ENTER, "", 1.1f)
                )
            )
        }

        return listOf(
            "qwertyuiop".map { KeyDef(it.toString(), Action.TEXT, it.toString()) },
            "asdfghjkl".map { KeyDef(it.toString(), Action.TEXT, it.toString()) },
            listOf(
                KeyDef("⇧", Action.SHIFT, "", 1.25f),
                *"zxcvbnm".map { KeyDef(it.toString(), Action.TEXT, it.toString()) }.toTypedArray(),
                KeyDef("⌫", Action.BACKSPACE, "", 1.25f)
            ),
            listOf(
                KeyDef("?123", Action.MODE, "", 1.2f), KeyDef(",", Action.TEXT, ","),
                KeyDef("SPACE", Action.SPACE, " ", 4.5f), KeyDef(".", Action.TEXT, "."),
                KeyDef("↵", Action.ENTER, "", 1.2f)
            )
        )
    }

    internal fun layoutRects(rows: List<List<KeyDef>>): List<RectF> {
        val gap = dp(5f)
        val side = dp(12f)
        val top = dp(15f)
        val bottom = dp(12f)
        val availableWidth = width - side * 2f
        val availableHeight = height - top - bottom
        val rowHeight = (availableHeight - gap * (rows.size - 1)) / max(rows.size, 1)

        val result = ArrayList<RectF>()
        var y = top

        for (row in rows) {
            val weightSum = row.sumOf { it.weight.toDouble() }.toFloat()
            val baseUnit =
                (availableWidth - gap * (row.size - 1)) / max(weightSum, 1f)

            var x = side
            for (key in row) {
                val w = baseUnit * key.weight
                result += RectF(x, y, x + w, y + rowHeight)
                x += w + gap
            }
            y += rowHeight + gap
        }

        return result
    }

    internal fun displayLabel(key: KeyDef): String =
        if (key.action == Action.TEXT && key.value.length == 1 && shift) {
            key.value.uppercase()
        } else {
            key.label
        }

    internal fun isNativeBlurEnabled(): Boolean =
        (context as? LiquidImeService)?.isNativeBlurEnabled() == true

    internal fun isOpticalMode(): Boolean =
        (context as? LiquidImeService)?.hasLocalOpticalBackdrop() == true

    internal fun isKeyPressed(index: Int): Boolean =
        currentRows().flatten().getOrNull(index)?.let { it == pressedKey } == true

    internal fun pressAt(x: Float, y: Float) {
        pressedKey = keyAt(x, y)
        invalidateAll()
    }

    internal fun releaseAt(x: Float, y: Float) {
        val key = keyAt(x, y)
        val pressed = pressedKey
        if (pressed != null && pressed == key) {
            handleKey(pressed)
        }
        pressedKey = null
        invalidateAll()
    }

    internal fun cancelPress() {
        pressedKey = null
        invalidateAll()
    }

    private fun invalidateAll() {
        invalidate()
        surfaceView.invalidate()
        glassView.invalidate()
        foregroundView.invalidate()
    }

    private fun keyAt(x: Float, y: Float): KeyDef? {
        val rows = currentRows()
        val rects = layoutRects(rows)

        for (i in rects.indices) {
            if (rects[i].contains(x, y)) {
                return rows.flatten()[i]
            }
        }

        return null
    }

    private fun handleKey(key: KeyDef) {
        val ime = context as? LiquidImeService ?: return

        when (key.action) {
            Action.TEXT -> {
                ime.commitText(
                    if (key.value.length == 1 && shift) {
                        key.value.uppercase()
                    } else {
                        key.value
                    }
                )
                if (shift) shift = false
            }

            Action.BACKSPACE -> ime.deleteBackward()
            Action.ENTER -> ime.sendEnter()

            Action.SPACE -> {
                ime.commitText(" ")
                if (shift) shift = false
            }

            Action.SHIFT -> shift = !shift
            Action.MODE -> numeric = !numeric
        }
    }

    internal fun resetTransientState() {
        pressedKey = null
        invalidateAll()
        refreshBackdropMode()
    }
}

private class KeyboardGlassSurfaceView(
    context: android.content.Context,
    private val host: ImeGlassPanelView
) : View(context) {
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlight = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun dp(value: Float): Float =
        resources.displayMetrics.density * value

    override fun onDraw(canvas: Canvas) {
        val outer = host.outerRect()

        // Base material only. The actual optical surface is the LiquidGlassView
        // drawn immediately above this view.
        fill.style = Paint.Style.FILL
        fill.color = 0x08FFFFFF
        canvas.drawRoundRect(outer, dp(38f), dp(38f), fill)

        val rows = host.currentRows()
        val rects = host.layoutRects(rows)

        for (i in rects.indices) {
            val rect = rects[i]
            val active = host.isKeyPressed(i)

            fill.color = if (active) 0x2AFFFFFF else 0x11FFFFFF
            canvas.drawRoundRect(rect, dp(13f), dp(13f), fill)

            stroke.style = Paint.Style.STROKE
            stroke.strokeWidth = dp(0.7f)
            stroke.color = if (active) 0x5FFFFFFF else 0x24FFFFFF
            canvas.drawRoundRect(rect, dp(13f), dp(13f), stroke)

            highlight.style = Paint.Style.STROKE
            highlight.strokeWidth = dp(0.45f)
            highlight.color = 0x15FFFFFF
            canvas.drawLine(
                rect.left + dp(8f),
                rect.top + dp(6f),
                rect.right - dp(8f),
                rect.top + dp(6f),
                highlight
            )
        }
    }
}

private class KeyboardForegroundView(
    context: android.content.Context,
    private val host: ImeGlassPanelView
) : View(context) {
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    init {
        isClickable = true
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        val rows = host.currentRows()
        val rects = host.layoutRects(rows)
        val flat = rows.flatten()

        for (i in rects.indices) {
            val key = flat[i]
            val rect = rects[i]

            text.textSize = when (key.action) {
                ImeGlassPanelView.Action.SPACE -> dp(10f)
                ImeGlassPanelView.Action.MODE -> dp(9f)
                else -> dp(14f)
            }

            text.color = Color.WHITE
            val label = host.displayLabel(key)

            canvas.drawText(
                label,
                rect.centerX() - text.measureText(label) / 2f,
                rect.centerY() - (text.ascent() + text.descent()) / 2f,
                text
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                host.pressAt(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_UP -> {
                host.releaseAt(event.x, event.y)
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                host.cancelPress()
                return true
            }
        }

        return true
    }
}
