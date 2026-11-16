package com.mangoloads.expmango

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import java.util.function.Consumer

class MangoImeService : InputMethodService(), MangoKeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: MangoKeyboardView
    private var currentPolicy = FieldPolicy(FieldType.NORMAL, true, true, true, false)
    private var composingText = StringBuilder()
    private var lastCommittedWord: String? = null
    private var crossWindowBlurEnabled = false
    private var blurWindowManager: WindowManager? = null
    private var crossWindowBlurListener: Consumer<Boolean>? = null

    override fun onCreate() {
        super.onCreate()
        DictionaryManager.init(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(WindowManager::class.java)
            blurWindowManager = manager
            crossWindowBlurEnabled = manager?.isCrossWindowBlurEnabled == true
            val listener = Consumer<Boolean> { enabled ->
                crossWindowBlurEnabled = enabled
                configureGlassWindow()
            }
            crossWindowBlurListener = listener
            manager?.addCrossWindowBlurEnabledListener(mainExecutor, listener)
        }
    }

    override fun onCreateInputView(): View {
        keyboardView = MangoKeyboardView(this, this)
        configureGlassWindow()
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentPolicy = FieldPolicyResolver.resolve(info)
        composingText.clear()
        // Never carry context from a previous application's editor into this one.
        lastCommittedWord = null
        configureGlassWindow()
        updatePredictions()
    }

    private fun configureGlassWindow() {
        // The IME is a separate Android window. Do this on its own Dialog, not the
        // launcher Activity: only the IME window can appear over other apps.
        val imeWindow = window?.window ?: return
        imeWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = if (crossWindowBlurEnabled) (72f * resources.displayMetrics.density).toInt() else 0
            imeWindow.setBackgroundBlurRadius(radius)
        }
        if (::keyboardView.isInitialized) {
            keyboardView.setNativeBlurEnabled(crossWindowBlurEnabled)
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            crossWindowBlurListener?.let { listener ->
                blurWindowManager?.removeCrossWindowBlurEnabledListener(listener)
            }
        }
        crossWindowBlurListener = null
        blurWindowManager = null
        super.onDestroy()
    }

    override fun onKeyText(text: String) {
        composingText.append(text)
        currentInputConnection?.commitText(text, 1)
        updatePredictions()
    }

    override fun onBackspace() {
        if (composingText.isNotEmpty()) {
            composingText.deleteCharAt(composingText.length - 1)
        }
        currentInputConnection?.deleteSurroundingText(1, 0)
        updatePredictions()
    }

    override fun onSpace() {
        if (composingText.isNotEmpty()) {
            val committed = composingText.toString()
            LearningEngine.recordCommit(this, committed, lastCommittedWord, currentPolicy)
            lastCommittedWord = committed
            composingText.clear()
        }
        currentInputConnection?.commitText(" ", 1)
        updatePredictions()
    }

    override fun onEnter() {
        if (composingText.isNotEmpty()) {
            val committed = composingText.toString()
            LearningEngine.recordCommit(this, committed, lastCommittedWord, currentPolicy)
            lastCommittedWord = committed
            composingText.clear()
        }
        sendKeyChar('\n')
        updatePredictions()
    }

    override fun onSuggestionClicked(suggestion: String) {
        val currentLen = composingText.length
        if (currentLen > 0) {
            currentInputConnection?.deleteSurroundingText(currentLen, 0)
        }
        currentInputConnection?.commitText("$suggestion ", 1)
        LearningEngine.recordCommit(this, suggestion, lastCommittedWord, currentPolicy)
        lastCommittedWord = suggestion
        composingText.clear()
        updatePredictions()
    }

    private fun updatePredictions() {
        if (!::keyboardView.isInitialized) return
        val current = composingText.toString()
        val list = PredictionEngine.getPredictions(current, lastCommittedWord, currentPolicy)
        keyboardView.setSuggestions(list)
    }
}
