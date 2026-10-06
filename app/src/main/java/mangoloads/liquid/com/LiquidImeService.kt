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

    private fun keyboardHeightPx(): Int =
        (292f * resources.displayMetrics.density).toInt()

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
        super.onConfigureWindow(win, false, isCandidatesOnly)
        win.setGravity(Gravity.BOTTOM)
        win.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            keyboardHeightPx()
        )
    }

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onEvaluateInputViewShown(): Boolean = true

    private fun configureWindow(window: Window) {
        window.setDimAmount(0f)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setGravity(Gravity.BOTTOM)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val wm = getSystemService(WindowManager::class.java)
            blurEnabled = runCatching {
                wm?.isCrossWindowBlurEnabled == true
            }.getOrDefault(false) && !isBatterySaver()

            window.setBackgroundBlurRadius(if (blurEnabled) 78 else 0)

            val attrs = window.attributes
            attrs.width = WindowManager.LayoutParams.MATCH_PARENT
            attrs.height = keyboardHeightPx()
            attrs.dimAmount = 0f
            attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
            window.attributes = attrs
        } else {
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                keyboardHeightPx()
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
