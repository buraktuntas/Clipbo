package com.bt.clipbo.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.presentation.ui.main.ClipboApp
import com.bt.clipbo.presentation.ui.tags.TagAssignmentDialog
import com.bt.clipbo.presentation.ui.tags.TagChipRow
import com.bt.clipbo.ui.theme.ClipboTheme

@Composable
fun ClipboardItemCard(
    item: ClipboardEntity,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onAssignTags: () -> Unit // Yeni parametre
) {
    var showTagDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (item.isPinned) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        onClick = onCopy
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // İçerik türü ve zaman
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = getTypeIcon(item.type),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        Text(
                            text = item.type,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = formatTime(item.timestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (item.isPinned) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📌",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        if (item.isSecure) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "🔒",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // İçerik önizlemesi
                    Text(
                        text = if (item.isSecure) "••••••••" else item.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Etiketler gösterimi
                    if (item.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))

                        val tags = item.tags.split(",").filter { it.isNotBlank() }
                        if (tags.isNotEmpty()) {
                            TagChipRow(tags = tags)
                        }
                    }
                }

                // Aksiyon butonları
                Column {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = if (item.isPinned) "📌" else "📍",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Etiket atama butonu
                    IconButton(
                        onClick = { showTagDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text(
                            text = "🏷️",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (!item.isPinned) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Etiket atama dialog'u
    if (showTagDialog) {
        TagAssignmentDialog(
            clipboardItem = item,
            onDismiss = { showTagDialog = false },
            onSave = {
                showTagDialog = false
                onAssignTags()
            }
        )
    }
}

@Composable
fun getTypeIcon(type: String): String {
    return when (type) {
        "URL" -> "🔗"
        "EMAIL" -> "📧"
        "PHONE" -> "📱"
        "IBAN" -> "🏦"
        "PASSWORD" -> "🔒"
        "PIN" -> "🔢"
        "ADDRESS" -> "📍"
        else -> "📋"
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