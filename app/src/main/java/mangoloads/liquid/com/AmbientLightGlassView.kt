package mangoloads.liquid.com

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.RuntimeShader
import android.view.View
import kotlin.math.min

/**
 * Procedural ambient illumination layer.
 *
 * It never reads the screen. It turns physical ambient lux + device tilt into
 * a subtle, moving optical highlight and rim response.
 */
internal class AmbientLightGlassView(
    context: android.content.Context,
    private val host: ImeGlassPanelView
) : View(context) {

    private val shader = RuntimeShader("""
        uniform float2 size;
        uniform float2 lightDir;
        uniform float ambient;
        uniform float radius;

        float sdRoundRect(float2 p, float2 b, float r) {
            float2 q = abs(p) - b + r;
            return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
        }

        half4 main(float2 p) {
            float2 c = size * 0.5;
            float2 b = c - 1.0;

            float d = sdRoundRect(p - c, b, radius);
            if (d > 0.0) {
                return half4(0.0);
            }

            float eps = 1.4;
            float dx =
                sdRoundRect(p - c + float2(eps, 0.0), b, radius) -
                sdRoundRect(p - c - float2(eps, 0.0), b, radius);
            float dy =
                sdRoundRect(p - c + float2(0.0, eps), b, radius) -
                sdRoundRect(p - c - float2(0.0, eps), b, radius);

            float2 n = normalize(float2(dx, dy) + 0.0001);
            float edge = 1.0 - smoothstep(0.0, min(radius, 70.0), -d);

            float2 l = normalize(lightDir + float2(0.0, -0.08));
            float ndl = max(dot(n, l), 0.0);

            float spec = pow(ndl, 22.0);
            float rim = pow(edge, 1.55);

            float intensity = mix(0.10, 0.34, ambient);

            // A restrained white/silver physical highlight rather than a
            // decorative colored overlay.
            float alpha = (spec * 0.34 + rim * 0.018) * intensity;

            return half4(
                1.0,
                1.0,
                1.0,
                alpha
            );
        }
    """)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val state = host.ambientState()
        val d = resources.displayMetrics.density

        shader.setFloatUniform(
            "size",
            width.toFloat(),
            height.toFloat()
        )
        shader.setFloatUniform(
            "lightDir",
            state.lightX,
            state.lightY
        )
        shader.setFloatUniform(
            "ambient",
            state.intensity
        )
        shader.setFloatUniform(
            "radius",
            min(38f * d, min(width, height) * 0.35f)
        )

        paint.shader = shader
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            38f * d,
            38f * d,
            paint
        )
        paint.shader = null
    }
}
