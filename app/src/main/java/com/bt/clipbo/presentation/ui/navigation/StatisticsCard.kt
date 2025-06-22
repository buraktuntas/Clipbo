package com.bt.clipbo.presentation.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun StatisticsCard(
    todayCount: Int,
    weekCount: Int,
    totalCount: Int,
    onViewAllStats: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "",
    )

    // Gradient arka plan
    val backgroundGradient =
        Brush.verticalGradient(
            colors =
                listOf(
                    Color(0xFFF8F4FF),
                    Color(0xFFF3E5F5),
                ),
        )

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .scale(cardScale)
                .shadow(
                    elevation = if (isPressed) 2.dp else 8.dp,
                    shape = RoundedCornerShape(20.dp),
                ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.Transparent),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(backgroundGradient)
                    .padding(24.dp),
        ) {
            Column {
                // Başlık
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors =
                                            listOf(
                                                Color(0xFF7B4397).copy(alpha = 0.2f),
                                                Color(0xFF7B4397).copy(alpha = 0.1f),
                                            ),
                                    ),
                                ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "📊",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Aktivite İstatistikleri",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF2D2D2D),
                            fontWeight = FontWeight.SemiBold,
                        )

                        Text(
                            text = "Clipboard kullanım verileri",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // İstatistik kartları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        value = todayCount,
                        label = "Bugün",
                        icon = "📅",
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f),
                    )

                    StatCard(
                        value = weekCount,
                        label = "Bu Hafta",
                        icon = "📈",
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f),
                    )

                    StatCard(
                        value = totalCount,
                        label = "Toplam",
                        icon = "🎯",
                        color = Color(0xFF7B4397),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Daha fazla detay butonu
                ElevatedButton(
                    onClick = onViewAllStats,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors =
                        ButtonDefaults.elevatedButtonColors(
                            containerColor = Color.White.copy(alpha = 0.9f),
                        ),
                ) {
                    Icon(
                        Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF7B4397),
                        modifier = Modifier.size(18.dp),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Detaylı İstatistikler",
                        color = Color(0xFF7B4397),
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}
