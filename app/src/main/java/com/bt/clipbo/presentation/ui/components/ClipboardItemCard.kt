package com.bt.clipbo.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.presentation.ui.main.ClipboApp
import com.bt.clipbo.presentation.ui.tags.TagChipRow
import com.bt.clipbo.ui.theme.ClipboTheme

@Composable
fun ClipboardItemCard(
    item: ClipboardEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onAssignTags: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(100),
        label = "",
    )

    val cardColor by animateColorAsState(
        targetValue =
            when {
                item.isPinned -> MaterialTheme.colorScheme.primaryContainer
                item.isSecure -> Color(0xFFFFEBEE)
                else -> MaterialTheme.colorScheme.surface
            },
        animationSpec = tween(200),
        label = "",
    )

    val borderColor =
        when {
            item.isPinned -> MaterialTheme.colorScheme.primary
            item.isSecure -> Color(0xFFE91E63)
            else -> Color.Transparent
        }

    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .scale(cardScale),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (item.isPinned || item.isSecure) 6.dp else 2.dp,
            ),
        border = if (item.isPinned || item.isSecure) BorderStroke(1.dp, borderColor) else null,
        shape = RoundedCornerShape(16.dp),
        onClick = onCopy,
        interactionSource = interactionSource,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Sol taraf - İçerik
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    // Üst satır - Tip, Zaman ve Durum İkonları
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Tip ikonu ve container
                        TypeIconContainer(
                            type = item.type,
                            isSecure = item.isSecure,
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Tip ve zaman bilgisi
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = getTypeDisplayName(item.type),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color =
                                        when {
                                            item.isSecure -> Color(0xFFE91E63)
                                            item.isPinned -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                )

                                if (item.isPinned) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.PushPin,
                                        contentDescription = "Sabitli",
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }

                                if (item.isSecure) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        Icons.Default.Security,
                                        contentDescription = "Güvenli",
                                        modifier = Modifier.size(12.dp),
                                        tint = Color(0xFFE91E63),
                                    )
                                }
                            }

                            Text(
                                text = formatTime(item.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Hızlı aksiyonlar
                        QuickActions(
                            isPinned = item.isPinned,
                            onTogglePin = onTogglePin,
                            onDelete = onDelete,
                            showDelete = !item.isPinned,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // İçerik önizlemesi
                    ContentPreview(
                        content = item.content,
                        isSecure = item.isSecure,
                        type = item.type,
                    )

                    // Etiketler
                    if (item.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        val tags = item.tags.split(",").filter { it.isNotBlank() }
                        if (tags.isNotEmpty()) {
                            TagChipRow(tags = tags)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alt aksiyonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Karakter sayısı
                Text(
                    text = "${item.content.length} karakter",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                // Etiket butonu
                TextButton(
                    onClick = onAssignTags,
                    modifier = Modifier.height(32.dp),
                ) {
                    Icon(
                        Icons.Default.Label,
                        contentDescription = "Etiket",
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Etiket",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun TypeIconContainer(
    type: String,
    isSecure: Boolean,
) {
    val (icon, backgroundColor) = getTypeIconAndColor(type, isSecure)

    Box(
        modifier =
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    backgroundColor.copy(alpha = 0.8f),
                                    backgroundColor.copy(alpha = 0.6f),
                                ),
                        ),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type,
            modifier = Modifier.size(24.dp),
            tint = Color.White,
        )
    }
}

@Composable
fun ContentPreview(
    content: String,
    isSecure: Boolean,
    type: String,
) {
    val displayContent =
        when {
            isSecure -> "•".repeat(minOf(content.length, 20))
            type == "PASSWORD" -> "•".repeat(content.length)
            else -> content
        }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = displayContent,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp),
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun QuickActions(
    isPinned: Boolean,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    showDelete: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Pin butonu
        IconButton(
            onClick = onTogglePin,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                if (isPinned) Icons.Default.PushPin else Icons.Default.PinDrop,
                contentDescription = if (isPinned) "Sabitlemeyi kaldır" else "Sabitle",
                modifier = Modifier.size(18.dp),
                tint =
                    if (isPinned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
            )
        }

        // Silme butonu
        if (showDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Sil",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

fun getTypeIconAndColor(
    type: String,
    isSecure: Boolean? = false,
): Pair<ImageVector, Color> {
    return when (type) {
        "URL" -> Icons.Default.Link to Color(0xFF2196F3)
        "EMAIL" -> Icons.Default.Email to Color(0xFF4CAF50)
        "PHONE" -> Icons.Default.Phone to Color(0xFF9C27B0)
        "PASSWORD" -> Icons.Default.Lock to Color(0xFFE91E63)
        "IBAN" -> Icons.Default.AccountBalance to Color(0xFFFF9800)
        "PIN" -> Icons.Default.Pin to Color(0xFF795548)
        "ADDRESS" -> Icons.Default.LocationOn to Color(0xFF607D8B)
        else ->
            if (isSecure == true) {
                Icons.Default.Security to Color(0xFFE91E63)
            } else {
                Icons.Default.Description to Color(0xFF757575)
            }
    }
}

fun getTypeDisplayName(type: String): String {
    return when (type) {
        "URL" -> "Link"
        "EMAIL" -> "E-posta"
        "PHONE" -> "Telefon"
        "PASSWORD" -> "Şifre"
        "IBAN" -> "IBAN"
        "PIN" -> "PIN"
        "ADDRESS" -> "Adres"
        else -> "Metin"
    }
}

@Composable
fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Şimdi"
        diff < 3600_000 -> "${diff / 60_000}dk önce"
        diff < 86400_000 -> "${diff / 3600_000}s önce"
        diff < 604800_000 -> "${diff / 86400_000}g önce"
        else -> "${diff / 604800_000}h önce"
    }
}

@Preview(showBackground = true)
@Composable
fun ClipboAppPreview() {
    ClipboTheme {
        ClipboApp()
    }
}
