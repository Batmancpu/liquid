package com.mangoloads.expmango

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0C0E14"))
            setPadding(48, 64, 48, 64)
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val title = TextView(this).apply {
            text = "Liquid Glass Lab"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Native Android Keyboard • Cross-app Glass Test • Offline"
            textSize = 13f
            setTextColor(Color.parseColor("#9AA0A6"))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 48)
        }
        root.addView(subtitle)

        val enableButton = Button(this).apply {
            text = "Enable Liquid Glass Lab in Settings"
            setBackgroundColor(Color.parseColor("#FF8C00"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }
        }
        root.addView(enableButton)

        val selectButton = Button(this).apply {
            text = "Switch Input Method"
            setBackgroundColor(Color.parseColor("#262A36"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showInputMethodPicker()
            }
        }
        root.addView(selectButton)

        val systemBlurEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            getSystemService(WindowManager::class.java)?.isCrossWindowBlurEnabled == true

        val capability = TextView(this).apply {
            text = if (systemBlurEnabled) {
                "System cross-window blur: ENABLED (visual effect still needs phone testing)"
            } else {
                "System cross-window blur: DISABLED or unavailable (solid-glass fallback)"
            }
            textSize = 13f
            setTextColor(Color.parseColor("#C9D6E6"))
            setPadding(0, 36, 0, 0)
            gravity = Gravity.CENTER
        }
        root.addView(capability)

        val instructions = TextView(this).apply {
            text = "Test it outside this app: enable and select the keyboard, then open a browser, messenger or notes app. Type over different colored backgrounds. The native system blur may be disabled by ColorOS or battery saver."
            textSize = 13f
            setTextColor(Color.parseColor("#9AA0A6"))
            setPadding(0, 18, 0, 0)
        }
        root.addView(instructions)

        val testField = EditText(this).apply {
            hint = "Local typing check (cross-app test is required)"
            setHintTextColor(Color.parseColor("#666D7D"))
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#1B1F2A"))
            setPadding(32, 32, 32, 32)
        }
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 48
        }
        root.addView(testField, params)

        setContentView(root)
    }
}
