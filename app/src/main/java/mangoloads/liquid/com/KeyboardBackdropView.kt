package mangoloads.liquid.com

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class KeyboardBackdropView(
    context: android.content.Context,
    private val host: ImeGlassPanelView
) : View(context) {

    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edge = Paint(Paint.ANTI_ALIAS_FLAG)
    private val accent = Paint(Paint.ANTI_ALIAS_FLAG)

    private fun dp(value: Float): Float = value * resources.displayMetrics.density

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
