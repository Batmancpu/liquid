package mangaloads.liquid.com

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import kotlin.math.sin

class AnimatedBackdropView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var t = 0f

    init {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        postInvalidateOnAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawColor(Color.rgb(6, 8, 16))
        val w = width.toFloat()
        val h = height.toFloat()

        drawOrb(canvas, w * (0.25f + 0.07f * sin(t * 0.71f)), h * 0.26f, 330f, 0xFF0A84FF.toInt())
        drawOrb(canvas, w * (0.72f + 0.09f * sin(t * 0.53f + 1.8f)), h * 0.38f, 380f, 0xFFFF375F.toInt())
        drawOrb(canvas, w * 0.50f, h * (0.72f + 0.06f * sin(t * 0.83f)), 430f, 0xFF30D158.toInt())

        paint.shader = null
        paint.color = 0x66FFFFFF
        paint.textSize = 28f
        canvas.drawText("Move the lens over detail", 28f, h * 0.86f, paint)

        t += 0.012f
        postInvalidateOnAnimation()
    }

    private fun drawOrb(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        paint.shader = RadialGradient(
            x, y, radius,
            intArrayOf(color, color and 0x00FFFFFF),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(x, y, radius, paint)
        paint.shader = null
    }
}
