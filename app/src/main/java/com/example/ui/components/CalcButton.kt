package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlueActiveBg
import com.example.ui.theme.ElectricBlueActiveText
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.ElectricBluePrimary
import com.example.ui.theme.KeyBorderElectric
import com.example.ui.theme.KeyBorderHighlight
import com.example.ui.theme.KeyFunctionBackground
import com.example.ui.theme.KeyFunctionPressed
import com.example.ui.theme.KeyNumericBackground
import com.example.ui.theme.KeyNumericPressed
import com.example.ui.theme.TextSilver
import com.example.ui.theme.TextWhite

enum class ButtonType {
    NUMERIC,
    FUNCTION,
    OPERATOR
}

@Composable
fun CalcButton(
    text: String,
    buttonType: ButtonType,
    modifier: Modifier = Modifier,
    isActiveOperator: Boolean = false,
    isPill: Boolean = false,
    fontSize: TextUnit = 32.sp,
    testTag: String = "btn_$text",
    contentDescription: String = text,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth tactile scale down to 0.92 on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btn_scale_$text"
    )

    // Dynamic color determination based on type, pressed state, and active operator highlight
    val targetBackgroundColor = when {
        buttonType == ButtonType.OPERATOR && isActiveOperator -> ElectricBlueActiveBg
        buttonType == ButtonType.OPERATOR && isPressed -> ElectricBlueLight.copy(alpha = 0.85f)
        buttonType == ButtonType.OPERATOR -> ElectricBluePrimary
        buttonType == ButtonType.FUNCTION && isPressed -> KeyFunctionPressed
        buttonType == ButtonType.FUNCTION -> KeyFunctionBackground
        isPressed -> KeyNumericPressed
        else -> KeyNumericBackground
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBackgroundColor,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "btn_bg_$text"
    )

    val targetTextColor = when {
        buttonType == ButtonType.OPERATOR && isActiveOperator -> ElectricBlueActiveText
        buttonType == ButtonType.FUNCTION -> TextSilver
        else -> TextWhite
    }

    val animatedTextColor by animateColorAsState(
        targetValue = targetTextColor,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "btn_text_$text"
    )

    val shape = CircleShape

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .then(
                if (buttonType == ButtonType.OPERATOR) {
                    Modifier.shadow(
                        elevation = if (isActiveOperator) 12.dp else 4.dp,
                        shape = shape,
                        ambientColor = ElectricBluePrimary.copy(alpha = 0.5f),
                        spotColor = ElectricBluePrimary.copy(alpha = 0.8f)
                    )
                } else {
                    Modifier
                }
            )
            .background(
                brush = if (buttonType == ButtonType.OPERATOR && !isActiveOperator) {
                    Brush.verticalGradient(
                        colors = listOf(
                            ElectricBlueLight,
                            ElectricBluePrimary
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            animatedBgColor,
                            animatedBgColor
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = when {
                    buttonType == ButtonType.OPERATOR && isActiveOperator -> ElectricBluePrimary
                    buttonType == ButtonType.OPERATOR -> KeyBorderElectric
                    else -> KeyBorderHighlight
                },
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom scale provides tactile feedback
                role = Role.Button,
                onClick = onClick
            )
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isPill) {
            // Authentic iOS Zero Pill alignment: '0' label is positioned over the left circle column
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = text,
                    color = animatedTextColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Start
                )
            }
        } else {
            Text(
                text = text,
                color = animatedTextColor,
                fontSize = fontSize,
                fontWeight = if (buttonType == ButtonType.OPERATOR) FontWeight.Medium else FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center
            )
        }
    }
}
