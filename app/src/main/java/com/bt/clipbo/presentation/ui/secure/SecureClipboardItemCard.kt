package com.bt.clipbo.presentation.ui.secure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.presentation.ui.components.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureClipboardItemCard(
    item: ClipboardEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleSecure: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFF1A237E).copy(alpha = 0.05f),
            ),
        shape = RoundedCornerShape(12.dp),
        onClick = onCopy,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                when (item.type) {
                                    "PASSWORD" -> "🔒"
                                    "PIN" -> "🔢"
                                    "IBAN" -> "🏦"
                                    else -> "🔐"
                                },
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = item.type,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1A237E),
                        )

                        if (item.isPinned) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "📌", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Güvenli içerik maskelenmiş göster
                    Text(
                        text = "•".repeat(minOf(item.content.length, 12)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🕐 ${formatTime(item.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Column {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            if (item.isPinned) Icons.Default.PushPin else Icons.Default.PinDrop,
                            contentDescription = if (item.isPinned) "Sabitlemeyi kaldır" else "Sabitle",
                            tint = if (item.isPinned) Color(0xFF1A237E) else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = onToggleSecure) {
                        Icon(
                            Icons.Default.LockOpen,
                            contentDescription = "Güvenlik modundan çıkar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    if (!item.isPinned) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}
