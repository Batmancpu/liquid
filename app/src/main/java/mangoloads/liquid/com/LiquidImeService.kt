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
        window?.window?.let { configureWindow(it) }
    }

    override fun onCreateInputView(): View {
        val view = ImeGlassPanelView(this)
        panel = view
        window?.window?.let { configureWindow(it) }
        view.post { window?.window?.let { configureWindow(it) } }
        return view
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        window?.window?.let { configureWindow(it) }
        panel?.resetTransientState()
    }

    override fun onWindowShown() {
        super.onWindowShown()
        window?.window?.let { configureWindow(it) }
    }

    override fun onConfigureWindow(
        win: Window,
        isFullscreen: Boolean,
        isCandidatesOnly: Boolean
    ) {
        // Let InputMethodService keep the correct MATCH_PARENT x WRAP_CONTENT
        // non-fullscreen IME geometry. A fixed pixel height here was causing
        // ColorOS to place the IME surface above the navigation region.
        super.onConfigureWindow(win, false, isCandidatesOnly)

        win.setGravity(Gravity.BOTTOM)
        val attrs = win.attributes
        attrs.width = WindowManager.LayoutParams.MATCH_PARENT
        attrs.height = WindowManager.LayoutParams.WRAP_CONTENT
        attrs.dimAmount = 0f
        attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        win.attributes = attrs
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onEvaluateInputViewShown(): Boolean = true

    private fun configureWindow(window: Window) {
        window.setDimAmount(0f)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.BOTTOM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.navigationBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            window.setNavigationBarDividerColor(Color.TRANSPARENT)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val wm = getSystemService(WindowManager::class.java)
            blurEnabled = runCatching {
                wm?.isCrossWindowBlurEnabled == true
            }.getOrDefault(false) && !isBatterySaver()

            window.setBackgroundBlurRadius(if (blurEnabled) 78 else 0)

            val attrs = window.attributes
            attrs.width = WindowManager.LayoutParams.MATCH_PARENT
            attrs.height = WindowManager.LayoutParams.WRAP_CONTENT
            attrs.dimAmount = 0f
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = attrs
        } else {
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
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
            android.view.KeyEvent(
                android.view.KeyEvent.ACTION_DOWN,
                android.view.KeyEvent.KEYCODE_ENTER
            )
        )
        currentInputConnection?.sendKeyEvent(
            android.view.KeyEvent(
                android.view.KeyEvent.ACTION_UP,
                android.view.KeyEvent.KEYCODE_ENTER
            )
        )
    }

    private fun isBatterySaver(): Boolean {
        return getSystemService(PowerManager::class.java)?.isPowerSaveMode == true
    }

    override fun onDestroy() {
        panel = null
        window?.window?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.setBackgroundBlurRadius(0)
            }
        }
        super.onDestroy()
    }
}
