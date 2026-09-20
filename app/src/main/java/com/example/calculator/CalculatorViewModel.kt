package com.example.calculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.VaultSecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel(
    private val securityManager: VaultSecurityManager,
    private val onUnlockVault: () -> Unit
) : ViewModel() {

    private val _state = MutableStateFlow(
        CalculatorState(
            isPasscodeSetupNeeded = !securityManager.isPasscodeSet()
        )
    )
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    fun onDigit(digit: String) {
        _state.update { curr ->
            val newExpr = if (curr.lastCalculationSuccess && !isOperatorEnding(curr.expression)) {
                digit
            } else if (curr.expression == "0") {
                digit
            } else {
                curr.expression + digit
            }
            curr.copy(
                expression = newExpr,
                displayValue = newExpr,
                previewResult = computePreview(newExpr, curr.isRad),
                lastCalculationSuccess = false,
                showUnlockFailedHint = false
            )
        }
    }

    fun onDecimal() {
        _state.update { curr ->
            val expr = curr.expression
            if (expr.isEmpty() || curr.lastCalculationSuccess) {
                curr.copy(expression = "0.", displayValue = "0.", lastCalculationSuccess = false)
            } else {
                // Check if last number already has a decimal
                val lastToken = expr.takeLastWhile { it.isDigit() || it == '.' }
                if (!lastToken.contains('.')) {
                    val newExpr = expr + "."
                    curr.copy(
                        expression = newExpr,
                        displayValue = newExpr,
                        previewResult = computePreview(newExpr, curr.isRad)
                    )
                } else {
                    curr
                }
            }
        }
    }

    fun onOperator(operator: String) {
        _state.update { curr ->
            val expr = curr.expression
            if (expr.isEmpty()) {
                if (operator == "−") {
                    curr.copy(expression = "−", displayValue = "−", lastCalculationSuccess = false)
                } else {
                    curr
                }
            } else if (isOperatorEnding(expr)) {
                // Replace last operator
                val newExpr = expr.dropLast(1) + operator
                curr.copy(expression = newExpr, displayValue = newExpr, lastCalculationSuccess = false)
            } else {
                val newExpr = expr + operator
                curr.copy(
                    expression = newExpr,
                    displayValue = newExpr,
                    previewResult = computePreview(newExpr, curr.isRad),
                    lastCalculationSuccess = false
                )
            }
        }
    }

    fun onFunction(fn: String) {
        _state.update { curr ->
            val expr = curr.expression
            val newExpr = when (fn) {
                "sin", "cos", "tan", "ln", "log" -> {
                    if (curr.lastCalculationSuccess || expr == "0") "$fn(" else expr + "$fn("
                }
                "√" -> {
                    if (curr.lastCalculationSuccess || expr == "0") "√(" else expr + "√("
                }
                "π" -> {
                    if (curr.lastCalculationSuccess || expr == "0") "π" else expr + "π"
                }
                "e" -> {
                    if (curr.lastCalculationSuccess || expr == "0") "e" else expr + "e"
                }
                "x²" -> {
                    if (expr.isNotEmpty() && !isOperatorEnding(expr)) expr + "^2" else expr
                }
                "x^y" -> {
                    if (expr.isNotEmpty() && !isOperatorEnding(expr)) expr + "^" else expr
                }
                "n!" -> {
                    if (expr.isNotEmpty() && !isOperatorEnding(expr)) expr + "!" else expr
                }
                "(" -> {
                    if (curr.lastCalculationSuccess || expr == "0") "(" else expr + "("
                }
                ")" -> {
                    if (expr.isNotEmpty()) expr + ")" else expr
                }
                else -> expr
            }
            curr.copy(
                expression = newExpr,
                displayValue = newExpr,
                previewResult = computePreview(newExpr, curr.isRad),
                lastCalculationSuccess = false
            )
        }
    }

    fun onClear() {
        _state.update { curr ->
            curr.copy(
                expression = "",
                displayValue = "0",
                previewResult = "",
                lastCalculationSuccess = false,
                showUnlockFailedHint = false
            )
        }
    }

    fun onDelete() {
        _state.update { curr ->
            val expr = curr.expression
            if (expr.isNotEmpty()) {
                val newExpr = expr.dropLast(1)
                curr.copy(
                    expression = newExpr,
                    displayValue = if (newExpr.isEmpty()) "0" else newExpr,
                    previewResult = computePreview(newExpr, curr.isRad),
                    lastCalculationSuccess = false
                )
            } else {
                curr
            }
        }
    }

    fun onToggleSign() {
        _state.update { curr ->
            val expr = curr.expression
            if (expr.isEmpty()) return@update curr

            if (expr.startsWith("−")) {
                val newExpr = expr.removePrefix("−")
                curr.copy(
                    expression = newExpr,
                    displayValue = if (newExpr.isEmpty()) "0" else newExpr,
                    previewResult = computePreview(newExpr, curr.isRad)
                )
            } else {
                val newExpr = "−$expr"
                curr.copy(
                    expression = newExpr,
                    displayValue = newExpr,
                    previewResult = computePreview(newExpr, curr.isRad)
                )
            }
        }
    }

    fun onEquals() {
        val expr = _state.value.expression
        if (expr.isBlank()) return

        // Evaluate normal math
        val eval = CalculatorEngine.evaluate(expr, _state.value.isRad)
        when (eval) {
            is CalculationResult.Success -> {
                _state.update {
                    it.copy(
                        expression = eval.formattedText,
                        displayValue = eval.formattedText,
                        previewResult = "",
                        lastCalculationSuccess = true
                    )
                }
            }
            is CalculationResult.Error -> {
                _state.update {
                    it.copy(
                        displayValue = eval.message,
                        previewResult = "",
                        lastCalculationSuccess = false
                    )
                }
            }
        }
    }

    /**
     * Hidden Button Action:
     * When pressed, checks whether the current input or expression matches the secret passcode.
     * If not set yet, shows the passcode setup dialog.
     * If set and matches, unlocks the vault!
     * If set and doesn't match, evaluates math or shows result like a normal calculator.
     */
    fun onHiddenButtonClick() {
        if (!securityManager.isPasscodeSet()) {
            _state.update { it.copy(showPasscodeSetupDialog = true) }
            return
        }

        val rawInput = _state.value.expression.trim()
        val candidate = rawInput.filter { it.isDigit() }

        if (candidate.isNotEmpty() && securityManager.verifyPasscode(candidate)) {
            // Unlock vault!
            onClear()
            onUnlockVault()
        } else {
            // Act like normal equals or indicate subtly
            if (rawInput.isNotEmpty()) {
                onEquals()
            }
            _state.update { it.copy(showUnlockFailedHint = true) }
        }
    }

    fun onSetNewPasscode(passcode: String, recoveryAnswer: String?) {
        val success = securityManager.setPasscode(passcode, recoveryAnswer)
        if (success) {
            _state.update {
                it.copy(
                    isPasscodeSetupNeeded = false,
                    showPasscodeSetupDialog = false
                )
            }
            onClear()
            onUnlockVault()
        }
    }

    fun dismissPasscodeDialog() {
        _state.update { it.copy(showPasscodeSetupDialog = false) }
    }

    fun openPasscodeSetup() {
        _state.update { it.copy(showPasscodeSetupDialog = true) }
    }

    fun toggleMode() {
        _state.update { curr ->
            val nextMode = if (curr.mode == CalculatorMode.SIMPLE) CalculatorMode.SCIENTIFIC else CalculatorMode.SIMPLE
            curr.copy(mode = nextMode)
        }
    }

    fun toggleRadDeg() {
        _state.update { curr ->
            val nextRad = !curr.isRad
            curr.copy(
                isRad = nextRad,
                previewResult = computePreview(curr.expression, nextRad)
            )
        }
    }

    private fun isOperatorEnding(expr: String): Boolean {
        if (expr.isEmpty()) return false
        val lastChar = expr.last()
        return lastChar in "+−×÷^"
    }

    private fun computePreview(expr: String, isRad: Boolean): String {
        if (expr.isBlank() || isOperatorEnding(expr)) return ""
        return when (val res = CalculatorEngine.evaluate(expr, isRad)) {
            is CalculationResult.Success -> "= ${res.formattedText}"
            is CalculationResult.Error -> ""
        }
    }
}
