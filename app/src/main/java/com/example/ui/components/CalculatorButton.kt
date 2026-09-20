package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ButtonType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    EQUALS,
    SCIENTIFIC
}

@Composable
fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    buttonType: ButtonType = ButtonType.NUMBER,
    fontSize: TextUnit = 24.sp,
    height: Dp = 68.dp,
    testTag: String = "btn_$text"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val (bgColor, contentColor) = when (buttonType) {
        ButtonType.NUMBER -> Pair(Color(0xFF262C38), Color(0xFFF1F5F9))
        ButtonType.OPERATOR -> Pair(Color(0xFFF59E0B), Color(0xFF0F172A))
        ButtonType.EQUALS -> Pair(Color(0xFFEA580C), Color(0xFFFFFFFF))
        ButtonType.FUNCTION -> Pair(Color(0xFF3B4455), Color(0xFFE2E8F0))
        ButtonType.SCIENTIFIC -> Pair(Color(0xFF1E2838), Color(0xFF93C5FD))
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isPressed) bgColor.copy(alpha = 0.75f) else bgColor,
        animationSpec = tween(durationMillis = 100),
        label = "btn_bg_anim"
    )

    val scale = if (isPressed) 0.94f else 1.0f

    Surface(
        modifier = modifier
            .height(height)
            .padding(4.dp)
            .scale(scale)
            .clip(RoundedCornerShape(22.dp))
            .testTag(testTag)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = animatedBg,
        tonalElevation = 2.dp,
        shadowElevation = if (isPressed) 1.dp else 3.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = fontSize,
                fontWeight = if (buttonType == ButtonType.EQUALS || buttonType == ButtonType.OPERATOR) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
