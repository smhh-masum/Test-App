package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.CalculatorMode

@Composable
fun CalculatorDisplay(
    expression: String,
    displayValue: String,
    previewResult: String,
    mode: CalculatorMode,
    isRad: Boolean,
    onToggleMode: () -> Unit,
    onToggleRad: () -> Unit,
    onHiddenButtonClick: () -> Unit,
    showUnlockFailedHint: Boolean,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Segmented Pill
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E2430),
                modifier = Modifier.testTag("mode_toggle_pill")
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeBadge(
                        title = "Simple",
                        isSelected = mode == CalculatorMode.SIMPLE,
                        onClick = { if (mode != CalculatorMode.SIMPLE) onToggleMode() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    ModeBadge(
                        title = "Scientific",
                        isSelected = mode == CalculatorMode.SCIENTIFIC,
                        onClick = { if (mode != CalculatorMode.SCIENTIFIC) onToggleMode() }
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rad/Deg toggle if scientific
                if (mode == CalculatorMode.SCIENTIFIC) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF263044),
                        modifier = Modifier
                            .clickable { onToggleRad() }
                            .padding(end = 8.dp)
                            .testTag("rad_deg_toggle")
                    ) {
                        Text(
                            text = if (isRad) "RAD" else "DEG",
                            color = Color(0xFF93C5FD),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Disguised Hidden Button
                // Discreet design: a stylish dot / emblem that acts as the secret trigger
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E2430),
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onHiddenButtonClick() }
                        .testTag("hidden_vault_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(6.dp)
                    ) {
                        // Subtle emblem that looks like a precision/status dot
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color(0xFF64748B), CircleShape)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Small expression history line
        Text(
            text = if (expression.isNotEmpty()) expression else " ",
            color = Color(0xFF94A3B8),
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .testTag("calculator_expression_text")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Main primary display value
        val fontSize = when {
            displayValue.length > 14 -> 30.sp
            displayValue.length > 10 -> 38.sp
            displayValue.length > 7 -> 46.sp
            else -> 54.sp
        }

        Text(
            text = displayValue,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("calculator_display_text")
        )

        // Live preview / error hint
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (previewResult.isNotEmpty()) {
                Text(
                    text = previewResult,
                    color = Color(0xFF38BDF8),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun ModeBadge(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color(0xFF3B82F6) else Color.Transparent,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
