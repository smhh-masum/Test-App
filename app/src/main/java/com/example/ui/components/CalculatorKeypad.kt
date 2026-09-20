package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculatorMode

@Composable
fun CalculatorKeypad(
    mode: CalculatorMode,
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onFunction: (String) -> Unit,
    onHiddenButtonTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (mode == CalculatorMode.SIMPLE) {
            SimpleKeypad(
                onDigit = onDigit,
                onOperator = onOperator,
                onDecimal = onDecimal,
                onClear = onClear,
                onDelete = onDelete,
                onToggleSign = onToggleSign,
                onEquals = onEquals,
                onFunction = onFunction,
                onHiddenButtonTrigger = onHiddenButtonTrigger
            )
        } else {
            ScientificKeypad(
                onDigit = onDigit,
                onOperator = onOperator,
                onDecimal = onDecimal,
                onClear = onClear,
                onDelete = onDelete,
                onToggleSign = onToggleSign,
                onEquals = onEquals,
                onFunction = onFunction,
                onHiddenButtonTrigger = onHiddenButtonTrigger
            )
        }
    }
}

@Composable
private fun SimpleKeypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onFunction: (String) -> Unit,
    onHiddenButtonTrigger: () -> Unit
) {
    // Row 1: AC, +/-, %, ÷
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("AC", onClick = onClear, buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        CalculatorButton("+/-", onClick = onToggleSign, buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        CalculatorButton("%", onClick = { onOperator("%") }, buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        CalculatorButton("÷", onClick = { onOperator("÷") }, buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
    }

    // Row 2: 7, 8, 9, ×
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("7", onClick = { onDigit("7") }, modifier = Modifier.weight(1f))
        CalculatorButton("8", onClick = { onDigit("8") }, modifier = Modifier.weight(1f))
        CalculatorButton("9", onClick = { onDigit("9") }, modifier = Modifier.weight(1f))
        CalculatorButton("×", onClick = { onOperator("×") }, buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
    }

    // Row 3: 4, 5, 6, −
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("4", onClick = { onDigit("4") }, modifier = Modifier.weight(1f))
        CalculatorButton("5", onClick = { onDigit("5") }, modifier = Modifier.weight(1f))
        CalculatorButton("6", onClick = { onDigit("6") }, modifier = Modifier.weight(1f))
        CalculatorButton("−", onClick = { onOperator("−") }, buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
    }

    // Row 4: 1, 2, 3, +
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("1", onClick = { onDigit("1") }, modifier = Modifier.weight(1f))
        CalculatorButton("2", onClick = { onDigit("2") }, modifier = Modifier.weight(1f))
        CalculatorButton("3", onClick = { onDigit("3") }, modifier = Modifier.weight(1f))
        CalculatorButton("+", onClick = { onOperator("+") }, buttonType = ButtonType.OPERATOR, modifier = Modifier.weight(1f))
    }

    // Row 5: 0, ., ⌫, =
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
        CalculatorButton(".", onClick = onDecimal, modifier = Modifier.weight(1f))
        CalculatorButton("⌫", onClick = onDelete, buttonType = ButtonType.FUNCTION, modifier = Modifier.weight(1f))
        CalculatorButton("=", onClick = onEquals, buttonType = ButtonType.EQUALS, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScientificKeypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onDecimal: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onFunction: (String) -> Unit,
    onHiddenButtonTrigger: () -> Unit
) {
    // Scientific function rows with compact height
    val sciHeight = 46.dp
    val sciFontSize = 15.sp

    // Sci Row 1
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("sin", onClick = { onFunction("sin") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("cos", onClick = { onFunction("cos") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("tan", onClick = { onFunction("tan") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("ln", onClick = { onFunction("ln") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("log", onClick = { onFunction("log") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
    }

    // Sci Row 2
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("√", onClick = { onFunction("√") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("x²", onClick = { onFunction("x²") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("x^y", onClick = { onFunction("x^y") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("π", onClick = { onFunction("π") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("e", onClick = { onFunction("e") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
    }

    // Sci Row 3
    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("(", onClick = { onFunction("(") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton(")", onClick = { onFunction(")") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("n!", onClick = { onFunction("n!") }, buttonType = ButtonType.SCIENTIFIC, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("%", onClick = { onOperator("%") }, buttonType = ButtonType.FUNCTION, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
        CalculatorButton("AC", onClick = onClear, buttonType = ButtonType.FUNCTION, height = sciHeight, fontSize = sciFontSize, modifier = Modifier.weight(1f))
    }

    // Numeric & operator rows (height 54.dp for compact elegance)
    val numHeight = 52.dp

    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("7", onClick = { onDigit("7") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("8", onClick = { onDigit("8") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("9", onClick = { onDigit("9") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("÷", onClick = { onOperator("÷") }, buttonType = ButtonType.OPERATOR, height = numHeight, modifier = Modifier.weight(1f))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("4", onClick = { onDigit("4") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("5", onClick = { onDigit("5") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("6", onClick = { onDigit("6") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("×", onClick = { onOperator("×") }, buttonType = ButtonType.OPERATOR, height = numHeight, modifier = Modifier.weight(1f))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("1", onClick = { onDigit("1") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("2", onClick = { onDigit("2") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("3", onClick = { onDigit("3") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("−", onClick = { onOperator("−") }, buttonType = ButtonType.OPERATOR, height = numHeight, modifier = Modifier.weight(1f))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("+/-", onClick = onToggleSign, buttonType = ButtonType.FUNCTION, height = numHeight, fontSize = 18.sp, modifier = Modifier.weight(1f))
        CalculatorButton("0", onClick = { onDigit("0") }, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton(".", onClick = onDecimal, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("+", onClick = { onOperator("+") }, buttonType = ButtonType.OPERATOR, height = numHeight, modifier = Modifier.weight(1f))
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        CalculatorButton("⌫", onClick = onDelete, buttonType = ButtonType.FUNCTION, height = numHeight, modifier = Modifier.weight(1f))
        CalculatorButton("=", onClick = onEquals, buttonType = ButtonType.EQUALS, height = numHeight, modifier = Modifier.weight(3f))
    }
}
