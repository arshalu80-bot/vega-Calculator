package com.example

import com.example.model.Operator
import com.example.viewmodel.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setup() {
        viewModel = CalculatorViewModel()
    }

    @Test
    fun testSimpleAddition() {
        viewModel.onDigit("5")
        viewModel.onOperator(Operator.ADD)
        viewModel.onDigit("3")
        viewModel.onEquals()

        assertEquals("8", viewModel.uiState.value.rawInput)
        assertEquals("5 + 3 =", viewModel.uiState.value.expression)
    }

    @Test
    fun testFloatingPointPrecisionCorrection() {
        // 0.1 + 0.2 should equal 0.3 without IEEE 754 precision artifact
        viewModel.onDigit("0")
        viewModel.onDecimal()
        viewModel.onDigit("1")
        viewModel.onOperator(Operator.ADD)
        viewModel.onDigit("0")
        viewModel.onDecimal()
        viewModel.onDigit("2")
        viewModel.onEquals()

        assertEquals("0.3", viewModel.uiState.value.rawInput)
    }

    @Test
    fun testDivisionByZero() {
        viewModel.onDigit("9")
        viewModel.onOperator(Operator.DIVIDE)
        viewModel.onDigit("0")
        viewModel.onEquals()

        assertTrue(viewModel.uiState.value.hasError)
        assertEquals("Error", viewModel.uiState.value.formattedDisplay)
    }

    @Test
    fun testToggleSign() {
        viewModel.onDigit("4")
        viewModel.onDigit("2")
        viewModel.onToggleSign()
        assertEquals("-42", viewModel.uiState.value.rawInput)
        viewModel.onToggleSign()
        assertEquals("42", viewModel.uiState.value.rawInput)
    }

    @Test
    fun testPercentageStandalone() {
        viewModel.onDigit("5")
        viewModel.onDigit("0")
        viewModel.onPercent()
        assertEquals("0.5", viewModel.uiState.value.rawInput)
    }

    @Test
    fun testPercentageChainedAddition() {
        // 200 + 10% => 200 + 20 = 220
        viewModel.onDigit("2")
        viewModel.onDigit("0")
        viewModel.onDigit("0")
        viewModel.onOperator(Operator.ADD)
        viewModel.onDigit("1")
        viewModel.onDigit("0")
        viewModel.onPercent()
        viewModel.onEquals()

        assertEquals("220", viewModel.uiState.value.rawInput)
    }

    @Test
    fun testClearAndAllClearSwitching() {
        // Initially AC
        assertTrue(viewModel.uiState.value.isAllClear)

        // Typing digit changes it to C
        viewModel.onDigit("7")
        assertFalse(viewModel.uiState.value.isAllClear)

        // Clearing resets to 0 and becomes AC
        viewModel.onClear()
        assertEquals("0", viewModel.uiState.value.rawInput)
        assertTrue(viewModel.uiState.value.isAllClear)
    }

    @Test
    fun testSwipeBackspace() {
        viewModel.onDigit("1")
        viewModel.onDigit("2")
        viewModel.onDigit("3")
        viewModel.onBackspace()
        assertEquals("12", viewModel.uiState.value.rawInput)
        viewModel.onBackspace()
        assertEquals("1", viewModel.uiState.value.rawInput)
        viewModel.onBackspace()
        assertEquals("0", viewModel.uiState.value.rawInput)
    }

    @Test
    fun testRepeatEquals() {
        // 5 + 3 = 8, then = again => 11, then = again => 14
        viewModel.onDigit("5")
        viewModel.onOperator(Operator.ADD)
        viewModel.onDigit("3")
        viewModel.onEquals()
        assertEquals("8", viewModel.uiState.value.rawInput)

        viewModel.onEquals()
        assertEquals("11", viewModel.uiState.value.rawInput)

        viewModel.onEquals()
        assertEquals("14", viewModel.uiState.value.rawInput)
    }
}
