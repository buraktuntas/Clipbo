package com.bt.clipbo.presentation.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ServiceStatusCard(
    isServiceRunning: Boolean,
    onToggleService: () -> Unit,
    isLoading: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animasyon değerleri
    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100), label = ""
    )

    val statusColor by animateColorAsState(
        targetValue = if (isServiceRunning) Color(0xFF4CAF50) else Color(0xFFE57373),
        animationSpec = tween(300), label = ""
    )

    // Gradient arka plan
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF8F4FF),
            Color(0xFFF3E5F5)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .shadow(
                elevation = if (isPressed) 2.dp else 8.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundGradient)
                .padding(24.dp)
        ) {
            Column {
                // Başlık ve durum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Animated icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF7B4397).copy(alpha = 0.2f),
                                            Color(0xFF7B4397).copy(alpha = 0.1f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "⚙️",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Clipboard Servisi",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF2D2D2D),
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = if (isServiceRunning) "Aktif ve dinleniyor" else "Durduruldu",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                        }
                    }

                    // Status indicator
                    StatusIndicator(
                        isActive = isServiceRunning,
                        isLoading = isLoading
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Kontrol alanı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isServiceRunning) "Servis Çalışıyor" else "Servis Durduruldu",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = statusColor
                            )

                            Text(
                                text = if (isServiceRunning)
                                    "Tüm kopyalama işlemleri kaydediliyor"
                                else
                                    "Başlatmak için açın",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666)
                            )
                        }

                        // Modern switch
                        Switch(
                            checked = isServiceRunning,
                            onCheckedChange = { onToggleService() },
                            enabled = !isLoading,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }
                }
            }
        }
    }
}