package com.mangoloads.expmango

import android.text.InputType
import android.view.inputmethod.EditorInfo

enum class FieldType {
    NORMAL,
    EMAIL,
    URI,
    PASSWORD,
    PIN,
    OTP,
    CODE,
    TERMINAL,
    SEARCH,
    INCOGNITO
}

data class FieldPolicy(
    val type: FieldType,
    val isPredictionEnabled: Boolean,
    val isAutocorrectEnabled: Boolean,
    val isLearningEnabled: Boolean,
    val isEmailDomainMode: Boolean
)

object FieldPolicyResolver {
    fun resolve(info: EditorInfo?, forceIncognito: Boolean = false): FieldPolicy {
        if (info == null) {
            return FieldPolicy(FieldType.NORMAL, true, true, true, false)
        }

        if (forceIncognito || (info.imeOptions and EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING) != 0) {
            return FieldPolicy(FieldType.INCOGNITO, true, false, false, false)
        }

        val inputType = info.inputType
        val classType = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION

        return when (classType) {
            InputType.TYPE_CLASS_NUMBER -> {
                if (variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
                    FieldPolicy(FieldType.PIN, false, false, false, false)
                } else {
                    FieldPolicy(FieldType.PIN, false, false, false, false)
                }
            }
            InputType.TYPE_CLASS_TEXT -> {
                when (variation) {
                    InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                    InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD -> {
                        FieldPolicy(FieldType.PASSWORD, false, false, false, false)
                    }
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> {
                        FieldPolicy(FieldType.EMAIL, true, false, true, true)
                    }
                    InputType.TYPE_TEXT_VARIATION_URI -> {
                        FieldPolicy(FieldType.URI, false, false, false, false)
                    }
                    else -> {
                        FieldPolicy(FieldType.NORMAL, true, true, true, false)
                    }
                }
            }
            else -> FieldPolicy(FieldType.NORMAL, true, true, true, false)
        }
    }
}
