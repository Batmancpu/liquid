package com.mangoloads.expmango

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo

class MangoImeService : InputMethodService(), MangoKeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: MangoKeyboardView
    private var currentPolicy = FieldPolicy(FieldType.NORMAL, true, true, true, false)
    private var composingText = StringBuilder()
    private var lastCommittedWord: String? = null

    override fun onCreate() {
        super.onCreate()
        DictionaryManager.init(this)
    }

    override fun onCreateInputView(): View {
        keyboardView = MangoKeyboardView(this, this)
        return keyboardView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentPolicy = FieldPolicyResolver.resolve(info)
        composingText.clear()
        updatePredictions()
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
