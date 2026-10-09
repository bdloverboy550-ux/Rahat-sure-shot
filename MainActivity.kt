package com.rahat.sureshot

import android.app.Activity
import android.os.Bundle
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {
    private lateinit var pricesInput: EditText
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 28, 24, 24)
            gravity = Gravity.TOP
            setBackgroundColor(0xFFF4F6FA.toInt())
        }

        val title = TextView(this).apply {
            text = "Rahat Sure Shot"
            textSize = 28f
            setTextColor(0xFF152238.toInt())
            typeface = Typeface.DEFAULT_BOLD
        }
        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "শিক্ষামূলক Binary Signal Analyzer"
            textSize = 16f
            setTextColor(0xFF46556A.toInt())
            setPadding(0, 4, 0, 18)
        }
        root.addView(subtitle)

        val instructions = TextView(this).apply {
            text = "সাম্প্রতিক closing price কমা দিয়ে লিখুন (কমপক্ষে 20টি)।\nউদাহরণ: 1.0821, 1.0824, 1.0820, ..."
            textSize = 15f
            setTextColor(0xFF253247.toInt())
        }
        root.addView(instructions)

        pricesInput = EditText(this).apply {
            hint = "এখানে closing price লিখুন"
            minLines = 4
            gravity = Gravity.TOP
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setTextColor(0xFF152238.toInt())
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        val inputParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        inputParams.setMargins(0, 12, 0, 12)
        root.addView(pricesInput, inputParams)

        val button = Button(this).apply {
            text = "বিশ্লেষণ করো"
            setOnClickListener { analyze() }
        }
        root.addView(button)

        result = TextView(this).apply {
            text = "ফলাফল এখানে দেখা যাবে।"
            textSize = 18f
            setTextColor(0xFF152238.toInt())
            setPadding(0, 20, 0, 14)
        }
        root.addView(result)

        val disclaimer = TextView(this).apply {
            text = "সতর্কতা: এটি লাইভ মার্কেট ডেটা নেয় না এবং কোনো নিশ্চিত CALL/PUT পূর্বাভাস নয়। Binary trading-এ দ্রুত টাকা হারানোর ঝুঁকি আছে। বাস্তব টাকা ব্যবহার না করে আগে ডেমোতে পরীক্ষা করুন।"
            textSize = 13f
            setTextColor(0xFF9B2C2C.toInt())
            setPadding(0, 12, 0, 0)
        }
        root.addView(disclaimer)
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun analyze() {
        val values = pricesInput.text.toString()
            .split(Regex("[,\\s;]+"))
            .mapNotNull { it.trim().toDoubleOrNull() }

        if (values.size < 20) {
            result.text = "কমপক্ষে ২০টি সঠিক closing price দিন।"
            return
        }
        val rsi = calculateRsi(values.takeLast(15), 14)
        val ema12 = ema(values, 12)
        val ema26 = ema(values, 26)
        val trend = when {
            ema12 > ema26 -> "স্বল্পমেয়াদি গতি ঊর্ধ্বমুখী"
            ema12 < ema26 -> "স্বল্পমেয়াদি গতি নিম্নমুখী"
            else -> "স্পষ্ট দিক নেই"
        }
        val note = when {
            rsi >= 70 -> "RSI বেশি (সম্ভাব্য overbought)"
            rsi <= 30 -> "RSI কম (সম্ভাব্য oversold)"
            else -> "RSI মাঝামাঝি"
        }
        result.text = "বিশ্লেষণ (শুধু শিক্ষামূলক)\nRSI(14): %.1f\nEMA trend: %s\n%s\n\nএটি CALL/PUT নির্দেশ নয়।".format(rsi, trend, note)
    }

    private fun calculateRsi(data: List<Double>, period: Int): Double {
        if (data.size < period + 1) return 50.0
        var gains = 0.0
        var losses = 0.0
        for (i in data.size - period until data.size) {
            val diff = data[i] - data[i - 1]
            if (diff > 0) gains += diff else losses -= diff
        }
        if (losses == 0.0) return if (gains == 0.0) 50.0 else 100.0
        val rs = (gains / period) / (losses / period)
        return 100.0 - (100.0 / (1.0 + rs))
    }

    private fun ema(data: List<Double>, period: Int): Double {
        if (data.isEmpty()) return 0.0
        val k = 2.0 / (period + 1.0)
        var value = data.take(period).average()
        for (i in period until data.size) value = data[i] * k + value * (1 - k)
        return value
    }
}
