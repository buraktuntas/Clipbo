package com.bt.clipbo.presentation.ui.secure

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.presentation.ui.history.StatColumn

@Composable
fun SecureContentScreen(
    uiState: SecureClipboardUiState,
    onCopyItem: (ClipboardEntity) -> Unit,
    onDeleteItem: (ClipboardEntity) -> Unit,
    onTogglePin: (ClipboardEntity) -> Unit,
    onToggleSecure: (ClipboardEntity) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
    ) {
        // Güvenli içerik istatistiği
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = Color(0xFF1A237E).copy(alpha = 0.1f),
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                StatColumn("Güvenli İçerik", uiState.secureItems.size.toString())
                StatColumn("Parola", uiState.secureItems.count { it.type == "PASSWORD" }.toString())
                StatColumn("IBAN", uiState.secureItems.count { it.type == "IBAN" }.toString())
                StatColumn("PIN", uiState.secureItems.count { it.type == "PIN" }.toString())
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // İçerik listesi
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.secureItems.isEmpty()) {
            EmptySecureContentCard()
        } else {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.secureItems.forEach { item ->
                    SecureClipboardItemCard(
                        item = item,
                        onCopy = { onCopyItem(item) },
                        onDelete = { onDeleteItem(item) },
                        onTogglePin = { onTogglePin(item) },
                        onToggleSecure = { onToggleSecure(item) },
                    )
                }
            }
        }
    }
}
