package com.mangoloads.expmango

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View

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

    private val bgPaint = Paint().apply { color = Color.parseColor("#151821") }
    private val keyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#262A36") }
    private val keyActivePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3C4254") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        textAlign = Paint.Align.CENTER
    }
    private val stripPaint = Paint().apply { color = Color.parseColor("#1D212D") }
    private val suggestionTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF9F1C")
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

    fun setSuggestions(list: List<String>) {
        suggestions = list
        invalidate()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        buildLayout(width.toFloat(), height.toFloat())
    }

    private fun buildLayout(w: Float, h: Float) {
        keyItems.clear()
        suggestionRects.clear()

        val stripHeight = 44f * resources.displayMetrics.density
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
                listOf("?123", "SPACE", "ENTER")
            )
        }

        val rowHeight = keyboardHeight / rows.size
        val padding = 4f * resources.displayMetrics.density

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

                val displayLabel = if (k.length == 1 && isShifted) k.uppercase() else k
                keyItems.add(KeyItem(displayLabel, rect, action, value))
                curX += kw + padding
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Draw suggestion strip
        val stripHeight = 44f * resources.displayMetrics.density
        canvas.drawRect(0f, 0f, width.toFloat(), stripHeight, stripPaint)
        suggestionTextPaint.textSize = 15f * resources.displayMetrics.density

        suggestionRects.forEach { (sug, rect) ->
            canvas.drawText(
                sug,
                rect.centerX(),
                rect.centerY() - (suggestionTextPaint.descent() + suggestionTextPaint.ascent()) / 2f,
                suggestionTextPaint
            )
        }

        // Draw keys
        textPaint.textSize = 18f * resources.displayMetrics.density
        val rRadius = 8f * resources.displayMetrics.density

        keyItems.forEach { item ->
            val isPressed = item == activePressedKey
            val p = if (isPressed) keyActivePaint else keyPaint

            canvas.drawRoundRect(item.rect, rRadius, rRadius, p)
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
