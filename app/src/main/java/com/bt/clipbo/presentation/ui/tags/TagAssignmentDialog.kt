package com.bt.clipbo.presentation.ui.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bt.clipbo.data.database.ClipboardEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagAssignmentDialog(
    clipboardItem: ClipboardEntity,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    viewModel: TagAssignmentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // Clipboard item'ı set et
    LaunchedEffect(clipboardItem) {
        viewModel.setClipboardItem(clipboardItem)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("🏷️ Etiket Ata")
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = clipboardItem.preview.take(30) + if (clipboardItem.preview.length > 30) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        text = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                        .verticalScroll(rememberScrollState()),
                // Bu satır eklendi
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    uiState.availableTags.forEach { tag ->
                        TagSelectionItem(
                            tag = tag,
                            isSelected = uiState.selectedTags.contains(tag.name),
                            onToggle = { viewModel.toggleTag(tag) },
                        )
                    }
                    CreateNewTagButton(
                        onClick = { viewModel.showCreateTagDialog() },
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.saveTagAssignments()
                    onSave()
                },
                enabled = !uiState.isSaving,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Kaydet")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
    )

    // Yeni etiket oluşturma dialog'u
    if (uiState.showCreateTagDialog) {
        CreateTagInlineDialog(
            onDismiss = { viewModel.hideCreateTagDialog() },
            onCreate = { name, color ->
                viewModel.createNewTag(name, color)
                viewModel.hideCreateTagDialog()
            },
        )
    }
}
