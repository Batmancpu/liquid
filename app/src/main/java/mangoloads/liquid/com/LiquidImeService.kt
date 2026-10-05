package mangoloads.liquid.com

import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager

class LiquidImeService : InputMethodService() {
    private var blurEnabled = false

    override fun onCreateInputView(): View {
        val panel = ImeGlassPanelView(this)
        window?.window?.let { configureWindow(it, panel) }
        panel.post { window?.window?.let { configureWindow(it, panel) } }
        return panel
    }

    override fun onStartInputView(
        info: android.view.inputmethod.EditorInfo?,
        restarting: Boolean
    ) {
        super.onStartInputView(info, restarting)
        window?.window?.let { configureWindow(it, window?.window?.decorView) }
    }

    override fun onDestroy() {
        window?.window?.let { clearWindowBlur(it) }
        super.onDestroy()
    }

    private fun configureWindow(window: Window, input: View?) {
        window.setDimAmount(0f)
        window.setBackgroundDrawable(
            GradientDrawable().apply {
                setColor(0x18FFFFFF)
                cornerRadius = 34f * resources.displayMetrics.density
            }
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val wm = getSystemService(WindowManager::class.java)
            val supported = runCatching { wm?.isCrossWindowBlurEnabled == true }.getOrDefault(false)
            blurEnabled = supported && !isBatterySaver()
            window.setBackgroundBlurRadius(if (blurEnabled) 86 else 0)

            val attrs = window.attributes
            attrs.blurBehindRadius = 0
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
            window.attributes = attrs
        }

        input?.background = null
        input?.invalidate()
    }

    private fun clearWindowBlur(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setBackgroundBlurRadius(0)
        }
    }

    private fun isBatterySaver(): Boolean {
        return getSystemService(android.os.PowerManager::class.java)?.isPowerSaveMode == true
    }
}
