package mangaloads.liquid.com

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View

class ImeGlassPanelView(context: Context) : View(context) {
    private val border = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        isClickable = false
        text.textSize = 14f * resources.displayMetrics.scaledDensity
    }

    override fun onDraw(canvas: Canvas) {
        val d = resources.displayMetrics.density
        val rect = RectF(10f * d, 10f * d, width - 10f * d, height - 10f * d)

        border.style = Paint.Style.FILL
        border.color = 0x10FFFFFF
        canvas.drawRoundRect(rect, 30f * d, 30f * d, border)

        border.style = Paint.Style.STROKE
        border.strokeWidth = 1.4f * d
        border.color = 0x88FFFFFF.toInt()
        canvas.drawRoundRect(rect, 30f * d, 30f * d, border)

        text.color = 0xEEFFFFFF.toInt()
        canvas.drawText("LIVE AMBIENT GLASS", 28f * d, 42f * d, text)
        text.color = 0xAAFFFFFF.toInt()
        canvas.drawText("Native blur • no screenshot loop", 28f * d, 66f * d, text)
    }
}
