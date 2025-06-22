package com.bt.clipbo.presentation.ui.main

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.presentation.ui.components.ClipboardItemCard
import com.bt.clipbo.ui.theme.ClipboTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onStartService: () -> Unit = {},
    onStopService: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 Clipbo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
        ) {
            // Servis kontrol butonları
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = onStartService,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Servisi Başlat")
                }

                OutlinedButton(
                    onClick = onStopService,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Servisi Durdur")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clipboard geçmişi başlığı
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "📋 Clipboard Geçmişi",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        text = "${uiState.clipboardItems.size} öğe",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Clipboard öğeleri listesi
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.clipboardItems.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "📝",
                            style = MaterialTheme.typography.displayMedium,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Henüz kopyalanan öğe yok",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = "Herhangi bir metni kopyaladığınızda burada görünecek",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // ScrollView ile basit liste (LazyColumn yerine)
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    uiState.clipboardItems.forEach { item ->
                        ClipboardItemCard(
                            item = item,
                            onCopy = {
                                viewModel.copyToClipboard(item.content)
                                Toast.makeText(context, "Panoya kopyalandı", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { viewModel.deleteItem(item) },
                            onTogglePin = { viewModel.togglePin(item) },
                            onAssignTags = {},
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    ClipboTheme {
        // Preview için mock data
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ClipboardItemCard(
                    item =
                        ClipboardEntity(
                            id = 1,
                            content = "https://github.com/clipbo/android",
                            timestamp = System.currentTimeMillis() - 300_000,
                            type = "URL",
                            isPinned = true,
                            isSecure = false,
                            preview = "https://github.com/clipbo/android",
                        ),
                    onCopy = {},
                    onDelete = {},
                    onTogglePin = {},
                    onAssignTags = {},
                )

                ClipboardItemCard(
                    item =
                        ClipboardEntity(
                            id = 2,
                            content = "MySecretPassword123!",
                            timestamp = System.currentTimeMillis() - 600_000,
                            type = "PASSWORD",
                            isPinned = false,
                            isSecure = true,
                            preview = "MySecretPassword123!",
                        ),
                    onCopy = {},
                    onDelete = {},
                    onTogglePin = {},
                    onAssignTags = {},
                )
            }
        }
    }
}
