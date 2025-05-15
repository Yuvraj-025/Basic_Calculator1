package com.example.basiccalc

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private var canAddOperation = false
    private var canAddDecimal = true

    private lateinit var workingsTV: TextView
    private lateinit var resultsTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        workingsTV = findViewById(R.id.workingsTV)
        resultsTV = findViewById(R.id.resultsTV)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun numberAction(view: View) {
        if (view is Button) {
            if (view.text == ".") {
                if (canAddDecimal) {
                    workingsTV.append(".")
                    canAddDecimal = false
                }
            } else {
                workingsTV.append(view.text)
            }
            canAddOperation = true
        }
    }

    fun operatorAction(view: View) {
        if (view is Button && canAddOperation) {
            when(view.text){
                "÷" -> {
                    workingsTV.append("/")
                }
                "x" -> {
                    workingsTV.append("*")
                }
                else -> {
                    workingsTV.append(view.text)
                }
            }
            canAddOperation = false
            canAddDecimal = true
        }
    }

    fun allClearAction(view: View) {
        workingsTV.text = ""
        resultsTV.text = ""
        canAddDecimal = true
        canAddOperation = false
    }

    fun backspaceAction(view: View) {
        val length = workingsTV.length()
        if (length > 0) {
            val removedChar = workingsTV.text.last()
            workingsTV.text = workingsTV.text.dropLast(1)
            if (removedChar == '.') {
                canAddDecimal = true
            }
        }
    }

    fun equalsAction(view: View) {
        try {
            val result = calculateResults()
            resultsTV.text = result
        } catch (e: Exception) {
            resultsTV.text = "Error"
        }
    }

    private fun calculateResults(): String {
        val elements = digitsOperators()
        if (elements.isEmpty()) return ""

        val processedList = processMultiplicationAndDivision(elements)
        val result = processAdditionAndSubtraction(processedList)

        return result.toString()
    }

    private fun digitsOperators(): MutableList<Any> {
        val list = mutableListOf<Any>()
        var currentNumber = ""

        for (char in workingsTV.text) {
            if (char.isDigit() || char == '.') {
                currentNumber += char
            } else {
                if (currentNumber.isNotEmpty()) {
                    list.add(currentNumber.toFloat())
                    currentNumber = ""
                }
                list.add(char)
            }
        }

        if (currentNumber.isNotEmpty()) {
            list.add(currentNumber.toFloat())
        }

        return list
    }

    private fun processMultiplicationAndDivision(list: MutableList<Any>): MutableList<Any> {
        val result = mutableListOf<Any>()
        var index = 0

        while (index < list.size) {
            val current = list[index]

            if (current is Char && (current == '*' || current == '/')) {
                if (result.isEmpty() || result.last() !is Float) {
                    throw IllegalArgumentException("Invalid expression before operator '$current'")
                }

                val prev = result.removeAt(result.lastIndex) as Float
                val next = list.getOrNull(index + 1)
                if (next !is Float) {
                    throw IllegalArgumentException("Invalid or missing number after operator '$current'")
                }

                val computed = if (current == '*') {
                    prev * next
                } else {
                    if (next == 0f) throw ArithmeticException("Division by zero")
                    prev / next
                }

                result.add(computed)
                index += 2
            } else {
                result.add(current)
                index++
            }
        }

        return result
    }



    private fun processAdditionAndSubtraction(list: MutableList<Any>): Float {
        var result = list[0] as Float
        var index = 1

        while (index < list.size) {
            val operator = list[index] as Char
            val next = list[index + 1] as Float

            when (operator) {
                '+' -> result += next
                '-' -> result -= next
            }

            index += 2
        }

        return result
    }

}
