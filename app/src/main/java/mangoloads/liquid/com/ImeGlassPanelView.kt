package mangoloads.liquid.com

import android.graphics.Color
import android.graphics.RectF
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.liquidglass.GlassMaterial
import com.example.liquidglass.LiquidGlassView
import kotlin.math.max

class ImeGlassPanelView(context: android.content.Context) : FrameLayout(context) {
    internal enum class Action {
        TEXT, BACKSPACE, ENTER, SHIFT, SPACE, MODE
    }

    internal data class KeyDef(
        val label: String,
        val action: Action,
        val value: String = "",
        val weight: Float = 1f
    )

    private var pressedKey: KeyDef? = null
    private var shift = false
    private var numeric = false

    private val backdropView: KeyboardBackdropView
    private val glassView: LiquidGlassView
    private val foregroundView: KeyboardForegroundView

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    init {
        isClickable = false
        clipChildren = false
        setBackgroundColor(Color.TRANSPARENT)

        backdropView = KeyboardBackdropView(context, this)
        addView(
            backdropView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        glassView = LiquidGlassView(context).apply {
            enableDynamicBackground = true
            material = GlassMaterial.CLEAR
            cornerRadius = dp(26f)
            refractionHeight = dp(30f)
            bevelWidth = dp(26f)
            dispersionStrength = 0.055f
            enableSensorHighlight = true
            enableAdaptiveTint = true
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

        foregroundView = KeyboardForegroundView(context, this)
        addView(
            foregroundView,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        foregroundView.bringToFront()

        post { glassView.invalidate() }
    }

    override fun onDetachedFromWindow() {
        pressedKey = null
        super.onDetachedFromWindow()
    }

    internal fun outerRect(): RectF = RectF(
        dp(8f),
        dp(6f),
        width - dp(8f),
        height - dp(6f)
    )

    internal fun isNativeBlurEnabled(): Boolean =
        (context as? LiquidImeService)?.isNativeBlurEnabled() == true

    internal fun currentRows(): List<List<KeyDef>> {
        if (numeric) {
            return listOf(
                "1234567890".map { KeyDef(it.toString(), Action.TEXT, it.toString()) },
                listOf(
                    KeyDef("-", Action.TEXT, "-"),
                    KeyDef("/", Action.TEXT, "/"),
                    KeyDef(":", Action.TEXT, ":"),
                    KeyDef(";", Action.TEXT, ";"),
                    KeyDef("(", Action.TEXT, "("),
                    KeyDef(")", Action.TEXT, ")"),
                    KeyDef("$", Action.TEXT, "$"),
                    KeyDef("&", Action.TEXT, "&"),
                    KeyDef("@", Action.TEXT, "@")
                ),
                listOf(
                    KeyDef("ABC", Action.MODE, "ABC"),
                    KeyDef(".", Action.TEXT, "."),
                    KeyDef(",", Action.TEXT, ","),
                    KeyDef("?", Action.TEXT, "?"),
                    KeyDef("!", Action.TEXT, "!"),
                    KeyDef("⌫", Action.BACKSPACE)
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
                KeyDef("?123", Action.MODE, "", 1.2f),
                KeyDef(",", Action.TEXT, ","),
                KeyDef("SPACE", Action.SPACE, " ", 4.5f),
                KeyDef(".", Action.TEXT, "."),
                KeyDef("↵", Action.ENTER, "", 1.2f)
            )
        )
    }

    internal fun layoutRects(rows: List<List<KeyDef>>): List<RectF> {
        val gap = dp(5f)
        val side = dp(14f)
        val top = dp(39f)
        val bottom = dp(11f)
        val availableWidth = width - side * 2f
        val availableHeight = height - top - bottom
        val rowHeight =
            (availableHeight - gap * (rows.size - 1)) / max(rows.size, 1)

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

    internal fun isKeyPressed(index: Int): Boolean {
        val key = currentRows().flatten().getOrNull(index)
        return key != null && key === pressedKey
    }

    internal fun pressAt(x: Float, y: Float) {
        pressedKey = keyAt(x, y)
        invalidate()
        backdropView.invalidate()
        foregroundView.invalidate()
    }

    internal fun releaseAt(x: Float, y: Float) {
        val key = keyAt(x, y)
        val pressed = pressedKey
        if (pressed != null && pressed === key) handleKey(pressed)
        pressedKey = null
        invalidate()
        backdropView.invalidate()
        foregroundView.invalidate()
    }

    internal fun cancelPress() {
        pressedKey = null
        invalidate()
        backdropView.invalidate()
        foregroundView.invalidate()
    }

    private fun keyAt(x: Float, y: Float): KeyDef? {
        val rows = currentRows()
        val rects = layoutRects(rows)
        for (i in rects.indices) {
            if (rects[i].contains(x, y)) return rows.flatten()[i]
        }
        return null
    }

    private fun handleKey(key: KeyDef) {
        val ime = context as? LiquidImeService ?: return

        when (key.action) {
            Action.TEXT -> {
                val value = if (key.value.length == 1 && shift) key.value.uppercase() else key.value
                ime.commitText(value)
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
        invalidate()
        foregroundView.invalidate()
        backdropView.invalidate()
    }
}

class KeyboardForegroundView(
    context: android.content.Context,
    private val host: ImeGlassPanelView
) : View(context) {

    private val text = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
    private val small = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

    init {
        isClickable = true
        text.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
        small.typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)
    }

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    override fun onDraw(canvas: android.graphics.Canvas) {
        small.textSize = dp(11f)
        small.color = 0xD8FFFFFF.toInt()
        canvas.drawText("LIQUID GLASS", dp(22f), dp(27f), small)

        val status = if (host.isNativeBlurEnabled()) "LIVE AMBIENT • OPTICAL" else "FALLBACK • OPTICAL"
        canvas.drawText(
            status,
            width - dp(22f) - small.measureText(status),
            dp(27f),
            small
        )

        val rows = host.currentRows()
        val rects = host.layoutRects(rows)
        val flat = rows.flatten()

        for (i in rects.indices) {
            val key = flat[i]
            val rect = rects[i]

            if (host.isKeyPressed(i)) {
                val p = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    style = android.graphics.Paint.Style.FILL
                    color = 0x22FFFFFF
                }
                canvas.drawRoundRect(rect, dp(12f), dp(12f), p)
            }

            text.textSize = when (key.action) {
                ImeGlassPanelView.Action.SPACE -> dp(10f)
                ImeGlassPanelView.Action.MODE -> dp(9f)
                else -> dp(14f)
            }
            text.color = Color.WHITE

            val label = host.displayLabel(key)
            val tx = rect.centerX() - text.measureText(label) / 2f
            val ty = rect.centerY() - (text.ascent() + text.descent()) / 2f
            canvas.drawText(label, tx, ty, text)
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
