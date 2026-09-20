package com.example.calculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.E
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object CalculatorEngine {

    private val formatSymbols = DecimalFormatSymbols(Locale.US)
    private val decimalFormat = DecimalFormat("#,##0.########", formatSymbols)
    private val scientificFormat = DecimalFormat("0.######E0", formatSymbols)

    fun evaluate(expression: String, isRad: Boolean): CalculationResult {
        if (expression.isBlank()) return CalculationResult.Success(0.0, "0")

        return try {
            val sanitized = sanitizeExpression(expression)
            val tokens = tokenize(sanitized)
            val parser = Parser(tokens, isRad)
            val value = parser.parseExpression()

            if (value.isNaN()) {
                CalculationResult.Error("Undefined")
            } else if (value.isInfinite()) {
                CalculationResult.Error("Cannot divide by zero")
            } else {
                CalculationResult.Success(value, formatNumber(value))
            }
        } catch (e: ArithmeticException) {
            CalculationResult.Error("Cannot divide by zero")
        } catch (e: Exception) {
            CalculationResult.Error("Invalid expression")
        }
    }

    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "Undefined"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
        if (abs(value) == 0.0) return "0"

        return if (abs(value) >= 1e12 || (abs(value) < 1e-6 && abs(value) > 0)) {
            scientificFormat.format(value)
        } else {
            decimalFormat.format(value)
        }
    }

    private fun sanitizeExpression(expr: String): String {
        return expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", PI.toString())
            .replace("e", E.toString())
            .replace(" ", "")
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expr.length) {
            val c = expr[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expr.length && (expr[i].isDigit() || expr[i] == '.' || expr[i] == 'E' || (expr[i] == '-' && sb.isNotEmpty() && sb.last() == 'E'))) {
                        sb.append(expr[i])
                        i++
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c in "+-*/^%!()" -> {
                    tokens.add(c.toString())
                    i++
                }
                expr.startsWith("sin", i) -> {
                    tokens.add("sin")
                    i += 3
                }
                expr.startsWith("cos", i) -> {
                    tokens.add("cos")
                    i += 3
                }
                expr.startsWith("tan", i) -> {
                    tokens.add("tan")
                    i += 3
                }
                expr.startsWith("asin", i) -> {
                    tokens.add("asin")
                    i += 4
                }
                expr.startsWith("acos", i) -> {
                    tokens.add("acos")
                    i += 4
                }
                expr.startsWith("atan", i) -> {
                    tokens.add("atan")
                    i += 4
                }
                expr.startsWith("ln", i) -> {
                    tokens.add("ln")
                    i += 2
                }
                expr.startsWith("log", i) -> {
                    tokens.add("log")
                    i += 3
                }
                expr.startsWith("sqrt", i) -> {
                    tokens.add("sqrt")
                    i += 4
                }
                expr.startsWith("√", i) -> {
                    tokens.add("sqrt")
                    i += 1
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private class Parser(private val tokens: List<String>, private val isRad: Boolean) {
        private var pos = 0

        private fun peek(): String? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): String = tokens[pos++]

        fun parseExpression(): Double {
            var result = parseTerm()
            while (peek() == "+" || peek() == "-") {
                val op = consume()
                val nextTerm = parseTerm()
                result = if (op == "+") result + nextTerm else result - nextTerm
            }
            return result
        }

        private fun parseTerm(): Double {
            var result = parseFactor()
            while (peek() == "*" || peek() == "/" || peek() == "%") {
                val op = consume()
                val nextFactor = parseFactor()
                result = when (op) {
                    "*" -> result * nextFactor
                    "/" -> {
                        if (nextFactor == 0.0) throw ArithmeticException("Division by zero")
                        result / nextFactor
                    }
                    "%" -> result % nextFactor
                    else -> result
                }
            }
            return result
        }

        private fun parseFactor(): Double {
            var result = parseUnary()
            if (peek() == "^") {
                consume()
                val power = parseFactor() // Right-associative
                result = result.pow(power)
            }
            while (peek() == "!") {
                consume()
                result = factorial(result)
            }
            return result
        }

        private fun parseUnary(): Double {
            if (peek() == "-") {
                consume()
                return -parseUnary()
            }
            if (peek() == "+") {
                consume()
                return parseUnary()
            }

            val token = peek() ?: return 0.0

            when (token) {
                "sin", "cos", "tan", "asin", "acos", "atan", "ln", "log", "sqrt" -> {
                    val fn = consume()
                    var arg = if (peek() == "(") {
                        consume()
                        val inner = parseExpression()
                        if (peek() == ")") consume()
                        inner
                    } else {
                        parseUnary()
                    }

                    return when (fn) {
                        "sin" -> {
                            val radians = if (isRad) arg else Math.toRadians(arg)
                            sin(radians)
                        }
                        "cos" -> {
                            val radians = if (isRad) arg else Math.toRadians(arg)
                            cos(radians)
                        }
                        "tan" -> {
                            val radians = if (isRad) arg else Math.toRadians(arg)
                            tan(radians)
                        }
                        "asin" -> {
                            val res = asin(arg)
                            if (isRad) res else Math.toDegrees(res)
                        }
                        "acos" -> {
                            val res = acos(arg)
                            if (isRad) res else Math.toDegrees(res)
                        }
                        "atan" -> {
                            val res = atan(arg)
                            if (isRad) res else Math.toDegrees(res)
                        }
                        "ln" -> {
                            if (arg <= 0) Double.NaN else ln(arg)
                        }
                        "log" -> {
                            if (arg <= 0) Double.NaN else log10(arg)
                        }
                        "sqrt" -> {
                            if (arg < 0) Double.NaN else sqrt(arg)
                        }
                        else -> arg
                    }
                }
                "(" -> {
                    consume()
                    val inner = parseExpression()
                    if (peek() == ")") consume()
                    return inner
                }
                else -> {
                    consume()
                    return token.toDoubleOrNull() ?: 0.0
                }
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0 || n != kotlin.math.floor(n) || n > 170) return Double.NaN
            var res = 1.0
            for (i in 2..n.toInt()) {
                res *= i
            }
            return res
        }
    }
}

sealed class CalculationResult {
    data class Success(val numericValue: Double, val formattedText: String) : CalculationResult()
    data class Error(val message: String) : CalculationResult()
}
