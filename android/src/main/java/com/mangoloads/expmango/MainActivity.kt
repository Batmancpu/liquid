package com.mangoloads.expmango

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
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
            text = "EXP Mango Keyboard"
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "100% Offline • English + Hinglish • Predictive Engine"
            textSize = 13f
            setTextColor(Color.parseColor("#9AA0A6"))
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 48)
        }
        root.addView(subtitle)

        val enableButton = Button(this).apply {
            text = "Enable EXP Mango in Settings"
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

        val testField = EditText(this).apply {
            hint = "Tap here to test typing (e.g. kya baat hai)"
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
