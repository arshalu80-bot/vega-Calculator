package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Operator
import com.example.ui.components.ButtonType
import com.example.ui.components.CalcButton
import com.example.ui.components.CalcDisplay
import com.example.ui.components.HistorySheet
import com.example.ui.components.TopBar
import com.example.ui.theme.BackgroundDarkNavy
import com.example.ui.theme.BackgroundPitchBlack
import com.example.ui.theme.ElectricBluePrimary
import com.example.viewmodel.CalculatorViewModel
import kotlin.math.min

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var showHistory by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    fun triggerHaptic() {
        if (state.hapticsEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundPitchBlack)
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.Zero, Key.NumPad0 -> { triggerHaptic(); viewModel.onDigit("0"); true }
                        Key.One, Key.NumPad1 -> { triggerHaptic(); viewModel.onDigit("1"); true }
                        Key.Two, Key.NumPad2 -> { triggerHaptic(); viewModel.onDigit("2"); true }
                        Key.Three, Key.NumPad3 -> { triggerHaptic(); viewModel.onDigit("3"); true }
                        Key.Four, Key.NumPad4 -> { triggerHaptic(); viewModel.onDigit("4"); true }
                        Key.Five, Key.NumPad5 -> { triggerHaptic(); viewModel.onDigit("5"); true }
                        Key.Six, Key.NumPad6 -> { triggerHaptic(); viewModel.onDigit("6"); true }
                        Key.Seven, Key.NumPad7 -> { triggerHaptic(); viewModel.onDigit("7"); true }
                        Key.Eight, Key.NumPad8 -> { triggerHaptic(); viewModel.onDigit("8"); true }
                        Key.Nine, Key.NumPad9 -> { triggerHaptic(); viewModel.onDigit("9"); true }
                        Key.Period, Key.NumPadDot -> { triggerHaptic(); viewModel.onDecimal(); true }
                        Key.Plus, Key.NumPadAdd -> { triggerHaptic(); viewModel.onOperator(Operator.ADD); true }
                        Key.Minus, Key.NumPadSubtract -> { triggerHaptic(); viewModel.onOperator(Operator.SUBTRACT); true }
                        Key.Multiply, Key.NumPadMultiply -> { triggerHaptic(); viewModel.onOperator(Operator.MULTIPLY); true }
                        Key.Slash, Key.NumPadDivide -> { triggerHaptic(); viewModel.onOperator(Operator.DIVIDE); true }
                        Key.Enter, Key.NumPadEnter, Key.Equals -> { triggerHaptic(); viewModel.onEquals(); true }
                        Key.Backspace -> { triggerHaptic(); viewModel.onBackspace(); true }
                        Key.Escape, Key.C -> { triggerHaptic(); viewModel.onClear(); true }
                        else -> false
                    }
                } else false
            }
            .testTag("calculator_screen_root")
    ) {
        // Ambient dark electric blue glow behind the display for ultra-premium atmosphere
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ElectricBluePrimary.copy(alpha = 0.12f),
                            BackgroundDarkNavy.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 480.dp)
                .align(Alignment.Center)
        ) {
            // Minimalist Top Bar with History & Quick Controls
            TopBar(
                hapticsEnabled = state.hapticsEnabled,
                onToggleHaptics = {
                    triggerHaptic()
                    viewModel.toggleHaptics()
                },
                onOpenHistory = {
                    triggerHaptic()
                    showHistory = true
                },
                onBackspace = {
                    triggerHaptic()
                    viewModel.onBackspace()
                }
            )

            // Flexible Display Screen Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.BottomEnd
            ) {
                CalcDisplay(
                    displayValue = state.formattedDisplay,
                    expression = state.expression,
                    onSwipeBackspace = {
                        triggerHaptic()
                        viewModel.onBackspace()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Authentic 4x5 Keypad Grid with Responsive Button Sizing
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val horizontalGap = 12.dp
                val verticalGap = 12.dp
                val availableWidth = maxWidth
                // Standard iOS 4-column layout
                val buttonDiameter = ((availableWidth - (horizontalGap * 3)) / 4).coerceAtMost(84.dp)
                val pillWidth = (buttonDiameter * 2) + horizontalGap

                Column(
                    verticalArrangement = Arrangement.spacedBy(verticalGap),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Row 1: AC/C, ±, %, ÷
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalcButton(
                            text = if (state.isAllClear) "AC" else "C",
                            buttonType = ButtonType.FUNCTION,
                            fontSize = 28.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_clear",
                            onClick = {
                                triggerHaptic()
                                viewModel.onClear()
                            }
                        )
                        CalcButton(
                            text = "±",
                            buttonType = ButtonType.FUNCTION,
                            fontSize = 28.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_toggle_sign",
                            onClick = {
                                triggerHaptic()
                                viewModel.onToggleSign()
                            }
                        )
                        CalcButton(
                            text = "%",
                            buttonType = ButtonType.FUNCTION,
                            fontSize = 28.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_percent",
                            onClick = {
                                triggerHaptic()
                                viewModel.onPercent()
                            }
                        )
                        CalcButton(
                            text = "÷",
                            buttonType = ButtonType.OPERATOR,
                            isActiveOperator = state.pendingOperator == Operator.DIVIDE && state.isEnteringNewNumber,
                            fontSize = 34.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_divide",
                            onClick = {
                                triggerHaptic()
                                viewModel.onOperator(Operator.DIVIDE)
                            }
                        )
                    }

                    // Row 2: 7, 8, 9, ×
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalcButton(
                            text = "7",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_7",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("7")
                            }
                        )
                        CalcButton(
                            text = "8",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_8",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("8")
                            }
                        )
                        CalcButton(
                            text = "9",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_9",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("9")
                            }
                        )
                        CalcButton(
                            text = "×",
                            buttonType = ButtonType.OPERATOR,
                            isActiveOperator = state.pendingOperator == Operator.MULTIPLY && state.isEnteringNewNumber,
                            fontSize = 34.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_multiply",
                            onClick = {
                                triggerHaptic()
                                viewModel.onOperator(Operator.MULTIPLY)
                            }
                        )
                    }

                    // Row 3: 4, 5, 6, −
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalcButton(
                            text = "4",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_4",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("4")
                            }
                        )
                        CalcButton(
                            text = "5",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_5",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("5")
                            }
                        )
                        CalcButton(
                            text = "6",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_6",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("6")
                            }
                        )
                        CalcButton(
                            text = "−",
                            buttonType = ButtonType.OPERATOR,
                            isActiveOperator = state.pendingOperator == Operator.SUBTRACT && state.isEnteringNewNumber,
                            fontSize = 34.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_subtract",
                            onClick = {
                                triggerHaptic()
                                viewModel.onOperator(Operator.SUBTRACT)
                            }
                        )
                    }

                    // Row 4: 1, 2, 3, +
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalcButton(
                            text = "1",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_1",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("1")
                            }
                        )
                        CalcButton(
                            text = "2",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_2",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("2")
                            }
                        )
                        CalcButton(
                            text = "3",
                            buttonType = ButtonType.NUMERIC,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_3",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("3")
                            }
                        )
                        CalcButton(
                            text = "+",
                            buttonType = ButtonType.OPERATOR,
                            isActiveOperator = state.pendingOperator == Operator.ADD && state.isEnteringNewNumber,
                            fontSize = 34.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_add",
                            onClick = {
                                triggerHaptic()
                                viewModel.onOperator(Operator.ADD)
                            }
                        )
                    }

                    // Row 5: 0 (pill spanning 2 columns), ., =
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(horizontalGap),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CalcButton(
                            text = "0",
                            buttonType = ButtonType.NUMERIC,
                            isPill = true,
                            modifier = Modifier
                                .width(pillWidth)
                                .height(buttonDiameter),
                            testTag = "btn_0",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDigit("0")
                            }
                        )
                        CalcButton(
                            text = ".",
                            buttonType = ButtonType.NUMERIC,
                            fontSize = 32.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_decimal",
                            onClick = {
                                triggerHaptic()
                                viewModel.onDecimal()
                            }
                        )
                        CalcButton(
                            text = "=",
                            buttonType = ButtonType.OPERATOR,
                            fontSize = 34.sp,
                            modifier = Modifier.size(buttonDiameter),
                            testTag = "btn_equals",
                            onClick = {
                                triggerHaptic()
                                viewModel.onEquals()
                            }
                        )
                    }
                }
            }
        }

        // History Bottom Sheet
        if (showHistory) {
            HistorySheet(
                history = state.history,
                onSelectRecord = { record ->
                    triggerHaptic()
                    viewModel.onSelectHistory(record)
                },
                onClearHistory = {
                    triggerHaptic()
                    viewModel.onClearHistory()
                },
                onDismiss = { showHistory = false }
            )
        }
    }
}
