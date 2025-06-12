package com.bt.clipbo.presentation.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bt.clipbo.utils.TimeFilter
import kotlin.collections.*

@Composable
fun FilterSection(
    uiState: SearchUiState,
    onTypeToggle: (String) -> Unit,
    onTimeFilterChange: (TimeFilter) -> Unit,
    onTogglePinned: () -> Unit,
    onToggleSecure: () -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎛️ Filtreler",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onClearFilters) {
                    Text("Temizle")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // İçerik türü filtreleri
            Text(
                text = "İçerik Türü",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            val contentTypes = listOf("TEXT", "URL", "EMAIL", "PHONE", "PASSWORD", "IBAN", "PIN")

            // Manuel olarak 3'erli gruplar yapıyoruz
            // İlk satır: TEXT, URL, EMAIL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onTypeToggle("TEXT") },
                    label = { Text("TEXT") },
                    selected = uiState.selectedTypes.contains("TEXT"),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTypeToggle("URL") },
                    label = { Text("URL") },
                    selected = uiState.selectedTypes.contains("URL"),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTypeToggle("EMAIL") },
                    label = { Text("EMAIL") },
                    selected = uiState.selectedTypes.contains("EMAIL"),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // İkinci satır: PHONE, PASSWORD, IBAN
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onTypeToggle("PHONE") },
                    label = { Text("PHONE") },
                    selected = uiState.selectedTypes.contains("PHONE"),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTypeToggle("PASSWORD") },
                    label = { Text("PASSWORD") },
                    selected = uiState.selectedTypes.contains("PASSWORD"),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTypeToggle("IBAN") },
                    label = { Text("IBAN") },
                    selected = uiState.selectedTypes.contains("IBAN"),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Üçüncü satır: PIN + boş alanlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onTypeToggle("PIN") },
                    label = { Text("PIN") },
                    selected = uiState.selectedTypes.contains("PIN"),
                    modifier = Modifier.weight(1f)
                )

                // Boş alanlar
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Zaman filtresi
            Text(
                text = "Zaman",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // İlk satır: Tümü, Son 1 Saat, Bugün
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onTimeFilterChange(TimeFilter.ALL) },
                    label = { Text("Tümü") },
                    selected = uiState.selectedTimeFilter == TimeFilter.ALL,
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTimeFilterChange(TimeFilter.LAST_HOUR) },
                    label = { Text("Son 1 Saat") },
                    selected = uiState.selectedTimeFilter == TimeFilter.LAST_HOUR,
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTimeFilterChange(TimeFilter.TODAY) },
                    label = { Text("Bugün") },
                    selected = uiState.selectedTimeFilter == TimeFilter.TODAY,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // İkinci satır: Son Hafta, Son Ay + boş alan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onTimeFilterChange(TimeFilter.LAST_WEEK) },
                    label = { Text("Son Hafta") },
                    selected = uiState.selectedTimeFilter == TimeFilter.LAST_WEEK,
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = { onTimeFilterChange(TimeFilter.LAST_MONTH) },
                    label = { Text("Son Ay") },
                    selected = uiState.selectedTimeFilter == TimeFilter.LAST_MONTH,
                    modifier = Modifier.weight(1f)
                )

                // Boş alan
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Özel filtreler
            Text(
                text = "Özel Filtreler",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = onTogglePinned,
                    label = { Text("📌 Sabitli") },
                    selected = uiState.showPinnedOnly,
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    onClick = onToggleSecure,
                    label = { Text("🔒 Güvenli") },
                    selected = uiState.showSecureOnly,
                    modifier = Modifier.weight(1f)
                )

                // Boş alan
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}
