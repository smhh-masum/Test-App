package com.example.calculator

enum class CalculatorMode {
    SIMPLE,
    SCIENTIFIC
}

data class CalculatorState(
    val expression: String = "",
    val displayValue: String = "0",
    val previewResult: String = "",
    val mode: CalculatorMode = CalculatorMode.SIMPLE,
    val isRad: Boolean = true,
    val isPasscodeSetupNeeded: Boolean = false,
    val showPasscodeSetupDialog: Boolean = false,
    val showUnlockFailedHint: Boolean = false,
    val lastCalculationSuccess: Boolean = false
)
