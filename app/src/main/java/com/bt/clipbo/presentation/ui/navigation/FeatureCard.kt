package com.bt.clipbo.presentation.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFFF3E5F5),
    isHighlighted: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "",
    )

    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 2.dp.value else 8.dp.value,
        animationSpec = tween(100),
        label = "",
    )

    // Gradient background
    val backgroundGradient =
        if (isHighlighted) {
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color(0xFF7B4397).copy(alpha = 0.1f),
                        backgroundColor,
                    ),
            )
        } else {
            Brush.verticalGradient(
                colors =
                    listOf(
                        Color.White.copy(alpha = 0.8f),
                        backgroundColor,
                    ),
            )
        }

    Card(
        modifier =
            modifier
                .aspectRatio(1f)
                .scale(cardScale)
                .shadow(
                    elevation = elevation.dp,
                    shape = RoundedCornerShape(20.dp),
                ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.Transparent),
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                    .padding(20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Icon container
                Box(
                    modifier =
                        Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(
                                if (isHighlighted) {
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                Color(0xFF7B4397).copy(alpha = 0.2f),
                                                Color(0xFF7B4397).copy(alpha = 0.1f),
                                            ),
                                    )
                                } else {
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                Color.White.copy(alpha = 0.8f),
                                                Color.White.copy(alpha = 0.4f),
                                            ),
                                    )
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF2D2D2D),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
    }
}
