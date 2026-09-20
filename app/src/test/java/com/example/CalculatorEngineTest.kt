package com.example

import com.example.calculator.CalculationResult
import com.example.calculator.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testSimpleArithmetic() {
        val res1 = CalculatorEngine.evaluate("12 + 8", isRad = true)
        assertTrue(res1 is CalculationResult.Success)
        assertEquals("20", (res1 as CalculationResult.Success).formattedText)

        val res2 = CalculatorEngine.evaluate("15 × 3 − 5", isRad = true)
        assertTrue(res2 is CalculationResult.Success)
        assertEquals("40", (res2 as CalculationResult.Success).formattedText)

        val res3 = CalculatorEngine.evaluate("100 ÷ 4", isRad = true)
        assertTrue(res3 is CalculationResult.Success)
        assertEquals("25", (res3 as CalculationResult.Success).formattedText)
    }

    @Test
    fun testScientificFunctions() {
        val sinRes = CalculatorEngine.evaluate("sin(90)", isRad = false)
        assertTrue(sinRes is CalculationResult.Success)
        assertEquals("1", (sinRes as CalculationResult.Success).formattedText)

        val sqrtRes = CalculatorEngine.evaluate("√(144)", isRad = true)
        assertTrue(sqrtRes is CalculationResult.Success)
        assertEquals("12", (sqrtRes as CalculationResult.Success).formattedText)

        val factRes = CalculatorEngine.evaluate("5!", isRad = true)
        assertTrue(factRes is CalculationResult.Success)
        assertEquals("120", (factRes as CalculationResult.Success).formattedText)
    }

    @Test
    fun testDivisionByZero() {
        val res = CalculatorEngine.evaluate("10 ÷ 0", isRad = true)
        assertTrue(res is CalculationResult.Error)
    }
}
