package com.example.model

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class Operator(val symbol: String, val displaySymbol: String) {
    ADD("+", "+"),
    SUBTRACT("-", "−"),
    MULTIPLY("*", "×"),
    DIVIDE("/", "÷")
}

data class CalculationRecord(
    val id: Long = System.currentTimeMillis(),
    val expression: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CalculatorState(
    val rawInput: String = "0",
    val expression: String = "",
    val accumulator: BigDecimal? = null,
    val pendingOperator: Operator? = null,
    val lastOperand: BigDecimal? = null,
    val lastOperator: Operator? = null,
    val isEnteringNewNumber: Boolean = false,
    val isResultState: Boolean = false,
    val hasError: Boolean = false,
    val history: List<CalculationRecord> = emptyList(),
    val hapticsEnabled: Boolean = true
) {
    /**
     * Determines whether the clear button shows 'AC' or 'C'.
     * In iOS calculator, it shows 'AC' initially, after equals, or after pressing 'C'.
     * When typing an entry (rawInput != "0"), it displays 'C'.
     */
    val isAllClear: Boolean
        get() = hasError || (rawInput == "0" && !isResultState)

    /**
     * Returns formatted text for display with thousands commas.
     * Preserves active decimal points and negative signs during editing.
     */
    val formattedDisplay: String
        get() {
            if (hasError) return "Error"
            if (rawInput == "-") return "-"
            if (rawInput == "0" || rawInput.isEmpty()) return "0"

            val isNegative = rawInput.startsWith("-")
            val cleanValue = if (isNegative) rawInput.substring(1) else rawInput

            // If it contains 'e' or 'E' (scientific notation)
            if (cleanValue.contains('e', ignoreCase = true)) {
                return rawInput
            }

            val parts = cleanValue.split(".")
            val integerPart = parts[0]
            val hasDecimal = cleanValue.contains(".")
            val decimalPart = if (parts.size > 1) parts[1] else ""

            val symbols = DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = ','
            }
            val formatter = DecimalFormat("#,###", symbols)

            val formattedInteger = if (integerPart.isNotEmpty()) {
                try {
                    val bigInt = BigDecimal(integerPart)
                    formatter.format(bigInt)
                } catch (e: Exception) {
                    integerPart
                }
            } else {
                "0"
            }

            val sign = if (isNegative) "−" else ""
            return if (hasDecimal) {
                "$sign$formattedInteger.$decimalPart"
            } else {
                "$sign$formattedInteger"
            }
        }
}
