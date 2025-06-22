package com.bt.clipbo.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bt.clipbo.utils.BackupRestoreManager
import okhttp3.internal.concurrent.formatDuration

@Composable
fun BackupProgressDialog(
    backupRestoreManager: BackupRestoreManager,
    onDismiss: () -> Unit,
) {
    val progress by backupRestoreManager.backupProgress.collectAsState()

    AlertDialog(
        onDismissRequest = { if (progress.isCompleted) onDismiss() },
        title = { Text("🔄 ${progress.stage.displayName}") },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = progress.progress / 100f,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("${progress.progress}% - ${progress.message}")

                if (progress.totalItems > 0) {
                    Text(
                        "${progress.currentItem}/${progress.totalItems} öğe",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (progress.estimatedTimeLeft > 0) {
                    Text(
                        "Kalan süre: ${formatDuration(progress.estimatedTimeLeft)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }

                if (progress.error != null) {
                    Text(
                        "❌ ${progress.error}",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            if (progress.isCompleted) {
                Button(onClick = onDismiss) {
                    Text("Tamam")
                }
            }
        },
    )
}
