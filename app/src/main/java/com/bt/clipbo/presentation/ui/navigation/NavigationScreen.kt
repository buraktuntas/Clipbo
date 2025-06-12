package com.bt.clipbo.presentation.ui.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToTags: () -> Unit = {},
    onNavigateToSecure: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onStartService: () -> Unit = {},
    onStopService: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {}
) {
    var isServiceRunning by remember { mutableStateOf(false) }
    val statisticsViewModel: StatisticsViewModel = hiltViewModel()
    val statisticsState by statisticsViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clipbo")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Servis durumu kartı
            ServiceStatusCard(
                isServiceRunning = isServiceRunning,
                onToggleService = {
                    if (isServiceRunning) {
                        onStopService()
                    } else {
                        onStartService()
                    }
                    isServiceRunning = !isServiceRunning
                }
            )

            // İstatistik kartı
            StatisticsCard(
                todayCount = statisticsState.todayCount,
                weekCount = statisticsState.weekCount,
                totalCount = statisticsState.totalCount,
                onViewAllStats = onNavigateToStatistics
            )

            // Ana özellikler başlığı
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚀",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ana Özellikler",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF4A4A4A)
                )
            }

            // Özellik kartları - 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Geçmiş",
                    description = "Tüm kopyaladıklarınızı görün",
                    icon = "📋",
                    onClick = onNavigateToHistory,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF3E5F5)
                )

                FeatureCard(
                    title = "Arama",
                    description = "İçeriklerinizi filtreleyin",
                    icon = "🔍",
                    onClick = onNavigateToSearch,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF3E5F5)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureCard(
                    title = "Etiketler",
                    description = "Düzenlemek için etiket ekleyin",
                    icon = "🏷️",
                    onClick = onNavigateToTags,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF3E5F5)
                )

                FeatureCard(
                    title = "Güvenli Pano",
                    description = "Hassas verilerinizi koruyun",
                    icon = "🔒",
                    onClick = onNavigateToSecure,
                    modifier = Modifier.weight(1f),
                    backgroundColor = Color(0xFFF3E5F5)
                )
            }

            // Ayarlar butonu
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF7B4397)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFF7B4397)
                )
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ayarlar")
            }
        }
    }
}