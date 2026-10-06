package mangoloads.liquid.com

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import kotlin.math.max

class KeyboardBackdropView(
    context: Context,
    private val host: ImeGlassPanelView
) : View(context) {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accent = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        val outer = host.outerRect()

        fill.style = Paint.Style.FILL
        fill.color = 0x1AFFFFFF
        canvas.drawRoundRect(outer, dp(26f), dp(26f), fill)

        val rows = host.currentRows()
        val rects = host.layoutRects(rows)

        for (i in rects.indices) {
            val rect = rects[i]
            val active = host.isKeyPressed(i)

            fill.color = if (active) 0x36FFFFFF else 0x20FFFFFF
            canvas.drawRoundRect(rect, dp(12f), dp(12f), fill)

            edge.style = Paint.Style.STROKE
            edge.strokeWidth = dp(0.8f)
            edge.color = if (active) 0x70FFFFFF else 0x32FFFFFF
            canvas.drawRoundRect(rect, dp(12f), dp(12f), edge)

            // Fine internal detail gives the optical lens something to bend.
            accent.style = Paint.Style.STROKE
            accent.strokeWidth = dp(0.65f)
            accent.color = 0x18FFFFFF
            canvas.drawLine(
                rect.left + dp(10f),
                rect.top + dp(7f),
                rect.right - dp(10f),
                rect.top + dp(7f),
                accent
            )
        }

        edge.style = Paint.Style.STROKE
        edge.strokeWidth = dp(1.1f)
        edge.color = 0x55FFFFFF
        canvas.drawRoundRect(outer, dp(26f), dp(26f), edge)
    }
}

class KeyboardForegroundView(
    context: Context,
    private val host: ImeGlassPanelView
) : View(context) {

    private val text = Paint(Paint.ANTI_ALIAS_FLAG)
    private val small = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        isClickable = true
        text.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        small.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private fun dp(value: Float): Float =
        value * resources.displayMetrics.density

    override fun onDraw(canvas: Canvas) {
        val d = resources.displayMetrics.density

        small.textSize = dp(11f)
        small.color = 0xD8FFFFFF.toInt()
        canvas.drawText("LIQUID GLASS", dp(22f), dp(27f), small)

        val status = if (host.isNativeBlurEnabled()) {
            "LIVE AMBIENT • OPTICAL"
        } else {
            "FALLBACK • OPTICAL"
        }
        val statusWidth = small.measureText(status)
        canvas.drawText(status, width - dp(22f) - statusWidth, dp(27f), small)

        val rows = host.currentRows()
        val rects = host.layoutRects(rows)

        for (i in rects.indices) {
            val key = rows.flatten()[i]
            val rect = rects[i]

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

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                host.pressAt(event.x, event.y)
                invalidate()
                return true
            }

            android.view.MotionEvent.ACTION_UP -> {
                host.releaseAt(event.x, event.y)
                invalidate()
                return true
            }

            android.view.MotionEvent.ACTION_CANCEL -> {
                host.cancelPress()
                invalidate()
                return true
            }
        }
        return true
    }
}
