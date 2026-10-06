package mangoloads.liquid.com

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.PowerManager
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo

class LiquidImeService : InputMethodService() {
    private var blurEnabled = false
    private var panel: ImeGlassPanelView? = null

    override fun onCreate() {
        super.onCreate()
        window?.window?.let(::configureWindow)
    }

    override fun onCreateInputView(): View {
        val view = ImeGlassPanelView(this)
        panel = view
        window?.window?.let(::configureWindow)
        view.post { window?.window?.let(::configureWindow) }
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        window?.window?.let(::configureWindow)
        panel?.resetTransientState()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        window?.window?.let(::configureWindow)
    }

    override fun onConfigureWindow(win: Window, isFullscreen: Boolean, isCandidatesOnly: Boolean) {
        // Let InputMethodService use MATCH_PARENT x WRAP_CONTENT in non-fullscreen mode.
        // A forced fixed window height caused bottom clipping/dead space on ColorOS.
        super.onConfigureWindow(win, false, isCandidatesOnly)
        configureWindow(win)
    }

    override fun onEvaluateFullscreenMode(): Boolean = false
    override fun onEvaluateInputViewShown(): Boolean = true

    override fun onComputeInsets(outInsets: Insets) {
        super.onComputeInsets(outInsets)
        // Make the entire compact keyboard frame touchable.
        outInsets.touchableInsets = Insets.TOUCHABLE_INSETS_FRAME
    }

    private fun configureWindow(window: Window) {
        window.setDimAmount(0f)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.BOTTOM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarColor(Color.TRANSPARENT)
            window.isNavigationBarContrastEnforced = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val wm = getSystemService(WindowManager::class.java)
            blurEnabled = runCatching {
                wm?.isCrossWindowBlurEnabled == true
            }.getOrDefault(false) && !isBatterySaver()

            // True live environmental layer: the OS compositor blurs the
            // application behind the IME. No screenshots or bitmap loop.
            window.setBackgroundBlurRadius(if (blurEnabled) 78 else 0)

            val attrs = window.attributes
            attrs.dimAmount = 0f
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = attrs
        }
    }

    fun isNativeBlurEnabled(): Boolean = blurEnabled

    fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    fun deleteBackward() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    fun sendEnter() {
        currentInputConnection?.sendKeyEvent(
            android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)
        )
        currentInputConnection?.sendKeyEvent(
            android.view.KeyEvent(android.view.KeyEvent.ACTION_UP, android.view.KeyEvent.KEYCODE_ENTER)
        )
    }

    private fun isBatterySaver(): Boolean =
        getSystemService(PowerManager::class.java)?.isPowerSaveMode == true

    override fun onDestroy() {
        panel = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window?.window?.setBackgroundBlurRadius(0)
        }
        super.onDestroy()
    }
}
