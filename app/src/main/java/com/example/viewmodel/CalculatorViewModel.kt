package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.CalculationRecord
import com.example.model.CalculatorState
import com.example.model.Operator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    private val mathContext = MathContext(16, RoundingMode.HALF_UP)
    private val maxDigits = 12

    fun onDigit(digit: String) {
        _uiState.update { state ->
            if (state.hasError) {
                return@update state.copy(
                    rawInput = digit,
                    hasError = false,
                    isEnteringNewNumber = false,
                    isResultState = false
                )
            }

            if (state.isEnteringNewNumber || state.isResultState) {
                state.copy(
                    rawInput = digit,
                    isEnteringNewNumber = false,
                    isResultState = false
                )
            } else {
                val current = state.rawInput
                // Ignore leading duplicate zero
                if (current == "0") {
                    state.copy(rawInput = digit)
                } else {
                    // Count only digit characters to enforce max precision limit
                    val digitCount = current.count { it.isDigit() }
                    if (digitCount < maxDigits) {
                        state.copy(rawInput = current + digit)
                    } else {
                        state
                    }
                }
            }
        }
    }

    fun onDecimal() {
        _uiState.update { state ->
            if (state.hasError || state.isEnteringNewNumber || state.isResultState) {
                state.copy(
                    rawInput = "0.",
                    hasError = false,
                    isEnteringNewNumber = false,
                    isResultState = false
                )
            } else if (!state.rawInput.contains(".")) {
                state.copy(rawInput = state.rawInput + ".")
            } else {
                state
            }
        }
    }

    fun onToggleSign() {
        _uiState.update { state ->
            if (state.hasError || state.rawInput == "0") return@update state

            val current = state.rawInput
            val toggled = if (current.startsWith("-")) {
                current.substring(1)
            } else {
                "-$current"
            }
            state.copy(rawInput = toggled)
        }
    }

    fun onPercent() {
        _uiState.update { state ->
            if (state.hasError) return@update state

            try {
                val currentVal = BigDecimal(state.rawInput)
                val resultVal: BigDecimal

                if (state.accumulator != null && state.pendingOperator != null) {
                    val accum = state.accumulator
                    resultVal = when (state.pendingOperator) {
                        Operator.ADD, Operator.SUBTRACT -> {
                            // In standard calculator mode: A + B% => B% of A
                            accum.multiply(currentVal, mathContext).divide(BigDecimal(100), mathContext)
                        }
                        Operator.MULTIPLY, Operator.DIVIDE -> {
                            currentVal.divide(BigDecimal(100), mathContext)
                        }
                    }
                } else {
                    resultVal = currentVal.divide(BigDecimal(100), mathContext)
                }

                val formatted = formatBigDecimal(resultVal)
                state.copy(
                    rawInput = formatted,
                    isEnteringNewNumber = false
                )
            } catch (e: Exception) {
                state.copy(hasError = true, rawInput = "Error")
            }
        }
    }

    fun onOperator(op: Operator) {
        _uiState.update { state ->
            if (state.hasError) return@update state

            // If an operator was already selected and user hasn't typed a new number yet,
            // simply update the pending operator and expression
            if (state.isEnteringNewNumber && state.accumulator != null) {
                val accStr = formatBigDecimal(state.accumulator)
                return@update state.copy(
                    pendingOperator = op,
                    expression = "$accStr ${op.displaySymbol}"
                )
            }

            try {
                val currentVal = BigDecimal(state.rawInput)
                if (state.accumulator != null && state.pendingOperator != null) {
                    // Evaluate chained operation
                    val result = evaluate(state.accumulator, state.pendingOperator, currentVal)
                    if (result == null) {
                        return@update state.copy(
                            hasError = true,
                            rawInput = "Error",
                            expression = "",
                            accumulator = null,
                            pendingOperator = null
                        )
                    }
                    val resultStr = formatBigDecimal(result)
                    state.copy(
                        accumulator = result,
                        pendingOperator = op,
                        rawInput = resultStr,
                        expression = "$resultStr ${op.displaySymbol}",
                        isEnteringNewNumber = true,
                        isResultState = false,
                        lastOperand = null,
                        lastOperator = null
                    )
                } else {
                    val accStr = formatBigDecimal(currentVal)
                    state.copy(
                        accumulator = currentVal,
                        pendingOperator = op,
                        expression = "$accStr ${op.displaySymbol}",
                        isEnteringNewNumber = true,
                        isResultState = false,
                        lastOperand = null,
                        lastOperator = null
                    )
                }
            } catch (e: Exception) {
                state.copy(hasError = true, rawInput = "Error")
            }
        }
    }

    fun onEquals() {
        _uiState.update { state ->
            if (state.hasError) return@update state

            try {
                if (state.accumulator != null && state.pendingOperator != null) {
                    val currentVal = BigDecimal(state.rawInput)
                    val result = evaluate(state.accumulator, state.pendingOperator, currentVal)

                    if (result == null) {
                        return@update state.copy(
                            hasError = true,
                            rawInput = "Error",
                            expression = "",
                            accumulator = null,
                            pendingOperator = null
                        )
                    }

                    val accumStr = formatBigDecimal(state.accumulator)
                    val currentStr = formatBigDecimal(currentVal)
                    val resultStr = formatBigDecimal(result)
                    val fullExpression = "$accumStr ${state.pendingOperator.displaySymbol} $currentStr"

                    val newRecord = CalculationRecord(
                        expression = fullExpression,
                        result = resultStr
                    )

                    state.copy(
                        rawInput = resultStr,
                        expression = "$fullExpression =",
                        accumulator = null,
                        pendingOperator = null,
                        lastOperand = currentVal,
                        lastOperator = state.pendingOperator,
                        isEnteringNewNumber = true,
                        isResultState = true,
                        history = listOf(newRecord) + state.history.take(49)
                    )
                } else if (state.lastOperator != null && state.lastOperand != null) {
                    // Repeat equals feature
                    val currentVal = BigDecimal(state.rawInput)
                    val result = evaluate(currentVal, state.lastOperator, state.lastOperand)

                    if (result == null) {
                        return@update state.copy(
                            hasError = true,
                            rawInput = "Error",
                            expression = "",
                            accumulator = null,
                            pendingOperator = null
                        )
                    }

                    val currentStr = formatBigDecimal(currentVal)
                    val operandStr = formatBigDecimal(state.lastOperand)
                    val resultStr = formatBigDecimal(result)
                    val fullExpression = "$currentStr ${state.lastOperator.displaySymbol} $operandStr"

                    val newRecord = CalculationRecord(
                        expression = fullExpression,
                        result = resultStr
                    )

                    state.copy(
                        rawInput = resultStr,
                        expression = "$fullExpression =",
                        isEnteringNewNumber = true,
                        isResultState = true,
                        history = listOf(newRecord) + state.history.take(49)
                    )
                } else {
                    state
                }
            } catch (e: Exception) {
                state.copy(hasError = true, rawInput = "Error")
            }
        }
    }

    fun onClear() {
        _uiState.update { state ->
            if (state.hasError) {
                CalculatorState(history = state.history, hapticsEnabled = state.hapticsEnabled)
            } else if (!state.isAllClear) {
                // Clear Entry ('C') -> resets current number to 0, retains operator & accumulator
                state.copy(
                    rawInput = "0",
                    isEnteringNewNumber = false,
                    isResultState = false
                )
            } else {
                // All Clear ('AC') -> resets complete state
                CalculatorState(history = state.history, hapticsEnabled = state.hapticsEnabled)
            }
        }
    }

    fun onBackspace() {
        _uiState.update { state ->
            if (state.hasError || state.isEnteringNewNumber || state.isResultState) return@update state

            val current = state.rawInput
            if (current.length > 1) {
                val trimmed = current.dropLast(1)
                if (trimmed == "-" || trimmed.isEmpty()) {
                    state.copy(rawInput = "0")
                } else {
                    state.copy(rawInput = trimmed)
                }
            } else {
                state.copy(rawInput = "0")
            }
        }
    }

    fun onSelectHistory(record: CalculationRecord) {
        _uiState.update { state ->
            state.copy(
                rawInput = record.result,
                expression = "${record.expression} =",
                accumulator = null,
                pendingOperator = null,
                isEnteringNewNumber = true,
                isResultState = true,
                hasError = false
            )
        }
    }

    fun onClearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun toggleHaptics() {
        _uiState.update { it.copy(hapticsEnabled = !it.hapticsEnabled) }
    }

    fun onPasteValue(pastedText: String) {
        val sanitized = pastedText.trim().replace(",", "")
        try {
            val num = BigDecimal(sanitized)
            val formatted = formatBigDecimal(num)
            _uiState.update {
                it.copy(
                    rawInput = formatted,
                    isEnteringNewNumber = false,
                    isResultState = false,
                    hasError = false
                )
            }
        } catch (e: Exception) {
            // Invalid number pasted, ignore
        }
    }

    private fun evaluate(a: BigDecimal, op: Operator, b: BigDecimal): BigDecimal? {
        return try {
            when (op) {
                Operator.ADD -> a.add(b, mathContext)
                Operator.SUBTRACT -> a.subtract(b, mathContext)
                Operator.MULTIPLY -> a.multiply(b, mathContext)
                Operator.DIVIDE -> {
                    if (b.compareTo(BigDecimal.ZERO) == 0) {
                        null
                    } else {
                        a.divide(b, mathContext)
                    }
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatBigDecimal(value: BigDecimal): String {
        // Strip trailing zero digits for cleaner output
        val stripped = try {
            value.stripTrailingZeros()
        } catch (e: Exception) {
            value
        }

        val plain = stripped.toPlainString()
        val absVal = value.abs()

        // Use engineering/scientific notation for excessively large or tiny non-zero numbers
        if (absVal > BigDecimal.ZERO && (absVal >= BigDecimal("1000000000000") || absVal < BigDecimal("0.000000001"))) {
            return String.format(java.util.Locale.US, "%.6e", value)
                .replace("e+0", "e+")
                .replace("e-0", "e-")
        }

        // Limit fractional digits if too long
        if (plain.contains(".")) {
            val parts = plain.split(".")
            if (parts[1].length > 10) {
                return stripped.setScale(10, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
            }
        }
        return plain
    }
}
