package com.bt.clipbo.presentation.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTagDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#FF5722") }

    val predefinedColors =
        listOf(
            "#FF5722", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722",
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Etiket Oluştur") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Etiket Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Renk Seçin",
                    style = MaterialTheme.typography.labelMedium,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Renk seçici
                predefinedColors.chunked(4).forEach { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowColors.forEach { color ->
                            Box(
                                modifier =
                                    Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(android.graphics.Color.parseColor(color)))
                                        .then(
                                            if (selectedColor == color) {
                                                Modifier.padding(4.dp)
                                            } else {
                                                Modifier
                                            },
                                        )
                                        .let { modifier ->
                                            if (selectedColor == color) {
                                                modifier.background(
                                                    MaterialTheme.colorScheme.primary,
                                                    RoundedCornerShape(4.dp),
                                                )
                                            } else {
                                                modifier
                                            }
                                        },
                            ) {
                                Button(
                                    onClick = { selectedColor = color },
                                    modifier = Modifier.fillMaxSize(),
                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                        ),
                                    contentPadding = PaddingValues(0.dp),
                                ) {}
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (tagName.isNotBlank()) {
                        onCreate(tagName, selectedColor)
                    }
                },
                enabled = tagName.isNotBlank(),
            ) {
                Text("Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        },
    )
}
