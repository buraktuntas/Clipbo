package com.bt.clipbo.presentation.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StatusIndicator(
    isActive: Boolean,
    isLoading: Boolean = false
) {
    val pulseScale by animateFloatAsState(
        targetValue = if (isActive) 1.2f else 1f,
        animationSpec = tween(1000), label = ""
    )

    Box(
        modifier = Modifier.size(50.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pulse effect için arka plan
        if (isActive) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.2f))
            )
        }

        // Ana durum göstergesi
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    if (isActive) Color(0xFF4CAF50) else Color(0xFFE57373)
                )
                .then(
                    if (isActive) {
                        Modifier.border(
                            2.dp,
                            Color.White,
                            CircleShape
                        )
                    } else Modifier
                )
        )

        // Loading indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(30.dp),
                strokeWidth = 2.dp,
                color = Color(0xFF7B4397)
            )
        }
    }
}
