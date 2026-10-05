package mangoloads.liquid.com

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.max

class ImeGlassPanelView(context: android.content.Context) : View(context) {
    private enum class Action {
        TEXT, BACKSPACE, ENTER, SHIFT, SPACE, MODE
    }

    private data class KeyDef(
        val label: String,
        val action: Action,
        val value: String = "",
        val weight: Float = 1f
    )

    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyBounds = ArrayList<Pair<RectF, KeyDef>>()
    private var pressedKey: KeyDef? = null
    private var shift = false
    private var numeric = false

    private val ime: LiquidImeService?
        get() = context as? LiquidImeService

    init {
        isClickable = true
        setLayerType(LAYER_TYPE_HARDWARE, null)

        textPaint.typeface = Typeface.create(
            "sans-serif-medium",
            Typeface.NORMAL
        )
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val desiredHeight = dp(292f).toInt()
        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> minOf(desiredHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        keyBounds.clear()

        val d = resources.displayMetrics.density
        val outer = RectF(dp(8f), dp(6f), width - dp(8f), height - dp(6f))

        keyPaint.style = Paint.Style.FILL
        keyPaint.color = 0x16FFFFFF
        canvas.drawRoundRect(outer, dp(26f), dp(26f), keyPaint)

        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = dp(1.2f)
        borderPaint.color = 0x55FFFFFF
        canvas.drawRoundRect(outer, dp(26f), dp(26f), borderPaint)

        textPaint.textSize = dp(11f)
        textPaint.color = 0xCFFFFFFF.toInt()
        canvas.drawText("LIQUID GLASS", dp(22f), dp(27f), textPaint)

        textPaint.textSize = dp(9f)
        textPaint.color = if (ime?.isNativeBlurEnabled() == true) {
            0x9FFFFFFF.toInt()
        } else {
            0x80FFFFFF.toInt()
        }
        val status = if (ime?.isNativeBlurEnabled() == true) {
            "LIVE AMBIENT"
        } else {
            "FALLBACK"
        }
        canvas.drawText(status, width - dp(22f) - textPaint.measureText(status), dp(27f), textPaint)

        val rows = buildRows()
        val side = dp(14f)
        val gap = dp(5f)
        val top = dp(39f)
        val bottom = dp(11f)
        val availableHeight = height - top - bottom
        val rowHeight = (availableHeight - gap * (rows.size - 1)) / rows.size
        val availableWidth = width - side * 2

        var y = top
        for (row in rows) {
            val weightSum = row.sumOf { it.weight.toDouble() }.toFloat()
            val weightedGap = gap * (row.size - 1)
            val baseUnit = (availableWidth - weightedGap) / max(weightSum, 1f)
            var x = side

            for (key in row) {
                val w = baseUnit * key.weight
                val rect = RectF(x, y, x + w, y + rowHeight)
                keyBounds += rect to key

                val active = pressedKey === key ||
                    (key.action == Action.SHIFT && shift) ||
                    (key.action == Action.MODE && numeric)

                keyPaint.style = Paint.Style.FILL
                keyPaint.color = when {
                    active -> 0x3AFFFFFF
                    else -> 0x18FFFFFF
                }
                canvas.drawRoundRect(rect, dp(12f), dp(12f), keyPaint)

                borderPaint.style = Paint.Style.STROKE
                borderPaint.strokeWidth = dp(0.8f)
                borderPaint.color = if (active) 0x70FFFFFF else 0x28FFFFFF
                canvas.drawRoundRect(rect, dp(12f), dp(12f), borderPaint)

                textPaint.textSize = when {
                    key.action == Action.SPACE -> dp(10f)
                    key.action == Action.MODE -> dp(9f)
                    else -> dp(14f)
                }
                textPaint.color = Color.WHITE

                val label = when {
                    key.action == Action.TEXT && shift && key.value.length == 1 ->
                        key.value.uppercase()
                    else -> key.label
                }

                val tx = rect.centerX() - textPaint.measureText(label) / 2f
                val ty = rect.centerY() - (textPaint.ascent() + textPaint.descent()) / 2f
                canvas.drawText(label, tx, ty, textPaint)

                x += w + gap
            }

            y += rowHeight + gap
        }
    }

    private fun buildRows(): List<List<KeyDef>> {
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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                pressedKey = findKey(event.x, event.y)
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP -> {
                val upKey = findKey(event.x, event.y)
                if (pressedKey != null && pressedKey == upKey) {
                    handleKey(pressedKey!!)
                }
                pressedKey = null
                invalidate()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                pressedKey = null
                invalidate()
                return true
            }
        }

        return true
    }

    private fun findKey(x: Float, y: Float): KeyDef? =
        keyBounds.firstOrNull { it.first.contains(x, y) }?.second

    private fun handleKey(key: KeyDef) {
        when (key.action) {
            Action.TEXT -> {
                val value = if (key.value.length == 1 && shift) {
                    key.value.uppercase()
                } else {
                    key.value
                }
                ime?.commitText(value)
                if (shift) shift = false
            }

            Action.BACKSPACE -> ime?.deleteBackward()
            Action.ENTER -> ime?.sendEnter()
            Action.SPACE -> {
                ime?.commitText(" ")
                if (shift) shift = false
            }

            Action.SHIFT -> {
                shift = !shift
            }

            Action.MODE -> {
                numeric = !numeric
                pressedKey = null
            }
        }
        invalidate()
    }

    fun resetTransientState() {
        pressedKey = null
        invalidate()
    }
}
