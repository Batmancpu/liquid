package com.mangoloads.expmango

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.roundToInt

class MangoKeyboardView(
    context: Context,
    private val listener: OnKeyboardActionListener
) : View(context) {

    interface OnKeyboardActionListener {
        fun onKeyText(text: String)
        fun onBackspace()
        fun onEnter()
        fun onSpace()
        fun onSuggestionClicked(suggestion: String)
    }

    private var isShifted = false
    private var isNumeric = false
    private var suggestions = listOf<String>()
    private var activePressedKey: KeyItem? = null
    private var nativeBlurEnabled = false
    private val density = resources.displayMetrics.density
    private val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
        Configuration.UI_MODE_NIGHT_YES

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyActivePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rimPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = density * 0.8f
    }
    private val topHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = density * 0.75f
        strokeCap = Paint.Cap.ROUND
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val suggestionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    data class KeyItem(
        val label: String,
        val rect: RectF,
        val action: String,
        val value: String = ""
    )

    private val keyItems = mutableListOf<KeyItem>()
    private val suggestionRects = mutableListOf<Pair<String, RectF>>()

    init {
        applyGlassPalette()
    }

    fun setNativeBlurEnabled(enabled: Boolean) {
        if (nativeBlurEnabled == enabled) return
        nativeBlurEnabled = enabled
        applyGlassPalette()
        invalidate()
    }

    private fun applyGlassPalette() {
        // The compositor supplies live background haze when enabled. Every fill in
        // this View must remain translucent or it will hide that external backdrop.
        bgPaint.color = if (isNightMode) {
            Color.argb(if (nativeBlurEnabled) 88 else 238, 20, 24, 34)
        } else {
            Color.argb(if (nativeBlurEnabled) 98 else 242, 237, 241, 247)
        }
        stripPaint.color = if (isNightMode) Color.argb(72, 28, 34, 47)
        else Color.argb(66, 255, 255, 255)
        keyPaint.color = if (isNightMode) Color.argb(115, 249, 250, 255)
        else Color.argb(172, 255, 255, 255)
        keyActivePaint.color = if (isNightMode) Color.argb(174, 255, 255, 255)
        else Color.argb(220, 255, 255, 255)
        rimPaint.color = if (isNightMode) Color.argb(130, 255, 255, 255)
        else Color.argb(167, 255, 255, 255)
        topHighlightPaint.color = Color.argb(if (isNightMode) 165 else 210, 255, 255, 255)
        textPaint.color = if (isNightMode) Color.WHITE else Color.rgb(29, 33, 43)
        suggestionTextPaint.color = textPaint.color
    }

    fun setSuggestions(list: List<String>) {
        suggestions = list
        // Rebuild hitboxes as text changes without requiring a new View layout.
        if (width > 0 && height > 0) buildLayout(width.toFloat(), height.toFloat())
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // The stock View measurement can consume the entire screen in an IME.
        // Constrain the experimental keyboard to a phone-sized panel.
        val desiredHeight = (290f * density).roundToInt()
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), resolveSize(desiredHeight, heightMeasureSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        buildLayout(width.toFloat(), height.toFloat())
    }

    private fun buildLayout(w: Float, h: Float) {
        keyItems.clear()
        suggestionRects.clear()

        val stripHeight = 44f * density
        val keyboardTop = stripHeight
        val keyboardHeight = h - keyboardTop

        // Suggestion strip layout
        if (suggestions.isNotEmpty()) {
            val sugWidth = w / suggestions.size.coerceAtLeast(1)
            suggestions.forEachIndexed { index, sug ->
                val r = RectF(index * sugWidth, 0f, (index + 1) * sugWidth, stripHeight)
                suggestionRects.add(Pair(sug, r))
            }
        }

        val rows = if (isNumeric) {
            listOf(
                "1234567890".map { it.toString() },
                listOf("-", "/", ":", ";", "(", ")", "$", "&", "@"),
                listOf("ABC", ".", ",", "?", "!", "DEL"),
                listOf("ABC", "SPACE", "ENTER")
            )
        } else {
            listOf(
                "qwertyuiop".map { it.toString() },
                "asdfghjkl".map { it.toString() },
                listOf("SHIFT", "z", "x", "c", "v", "b", "n", "m", "DEL"),
                listOf("?123", ",", "SPACE", ".", "ENTER")
            )
        }

        val rowHeight = keyboardHeight / rows.size
        val padding = 4f * density

        rows.forEachIndexed { rIdx, row ->
            val y = keyboardTop + rIdx * rowHeight
            val totalWeight = row.sumOf {
                when (it) {
                    "SPACE" -> 4.0
                    "SHIFT", "DEL", "ABC", "?123", "ENTER" -> 1.5
                    else -> 1.0
                }
            }.toFloat()

            val unitW = (w - (row.size + 1) * padding) / totalWeight
            var curX = padding

            row.forEach { k ->
                val weight = when (k) {
                    "SPACE" -> 4.0f
                    "SHIFT", "DEL", "ABC", "?123", "ENTER" -> 1.5f
                    else -> 1.0f
                }
                val kw = unitW * weight
                val rect = RectF(curX, y + padding, curX + kw, y + rowHeight - padding)

                val (action, value) = when (k) {
                    "SHIFT" -> Pair("SHIFT", "")
                    "DEL" -> Pair("DEL", "")
                    "ENTER" -> Pair("ENTER", "")
                    "SPACE" -> Pair("SPACE", " ")
                    "?123", "ABC" -> Pair("MODE", "")
                    else -> Pair("TEXT", if (isShifted) k.uppercase() else k)
                }

                val displayLabel = when (k) {
                    "SHIFT" -> "⇧"
                    "DEL" -> "⌫"
                    "SPACE" -> "space"
                    "ENTER" -> "↵"
                    else -> if (k.length == 1 && isShifted) k.uppercase() else k
                }
                keyItems.add(KeyItem(displayLabel, rect, action, value))
                curX += kw + padding
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val panelRadius = 24f * density
        val panelRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(panelRect, panelRadius, panelRadius, bgPaint)

        // Draw suggestion strip
        val stripHeight = 44f * density
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), stripHeight + 7f * density),
            panelRadius, panelRadius, stripPaint)
        suggestionTextPaint.textSize = 15f * density

        suggestionRects.forEach { (sug, rect) ->
            canvas.drawText(
                sug,
                rect.centerX(),
                rect.centerY() - (suggestionTextPaint.descent() + suggestionTextPaint.ascent()) / 2f,
                suggestionTextPaint
            )
        }

        // Draw keys
        val rRadius = 12f * density

        keyItems.forEach { item ->
            val isPressed = item == activePressedKey
            val p = if (isPressed) keyActivePaint else keyPaint

            // A refractive-looking edge cue belongs to our own key geometry; the
            // background haze underneath is owned by Android's window compositor.
            canvas.drawRoundRect(item.rect, rRadius, rRadius, p)
            canvas.drawRoundRect(item.rect, rRadius, rRadius, rimPaint)
            canvas.drawLine(item.rect.left + 8f * density, item.rect.top + 1.2f * density,
                item.rect.right - 8f * density, item.rect.top + 1.2f * density,
                topHighlightPaint)
            textPaint.textSize = if (item.label.length > 2) 13f * density else 19f * density
            canvas.drawText(
                item.label,
                item.rect.centerX(),
                item.rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f,
                textPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Check suggestions
                suggestionRects.firstOrNull { it.second.contains(x, y) }?.let {
                    listener.onSuggestionClicked(it.first)
                    return true
                }

                activePressedKey = keyItems.firstOrNull { it.rect.contains(x, y) }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                val releasedKey = keyItems.firstOrNull { it.rect.contains(x, y) }
                if (releasedKey != null && releasedKey == activePressedKey) {
                    when (releasedKey.action) {
                        "TEXT" -> {
                            listener.onKeyText(releasedKey.value)
                            if (isShifted) {
                                isShifted = false
                                buildLayout(width.toFloat(), height.toFloat())
                            }
                        }
                        "DEL" -> listener.onBackspace()
                        "ENTER" -> listener.onEnter()
                        "SPACE" -> listener.onSpace()
                        "SHIFT" -> {
                            isShifted = !isShifted
                            buildLayout(width.toFloat(), height.toFloat())
                        }
                        "MODE" -> {
                            isNumeric = !isNumeric
                            buildLayout(width.toFloat(), height.toFloat())
                        }
                    }
                }
                activePressedKey = null
                invalidate()
                return true
            }
            MotionEvent.ACTION_CANCEL -> {
                activePressedKey = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
