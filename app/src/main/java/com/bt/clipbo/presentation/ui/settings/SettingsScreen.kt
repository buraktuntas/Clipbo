package com.bt.clipbo.presentation.ui.settings


import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Support
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Switch
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
import com.bt.clipbo.ui.theme.ClipboTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Genel Ayarlar
            SettingsSection(title = "🎛️ Genel Ayarlar") {
                SettingsItem(
                    title = "Koyu Tema",
                    description = "Gece modu için koyu renk teması kullan",
                    icon = Icons.Default.DarkMode,
                    trailing = {
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme() }
                        )
                    }
                )

                SettingsItem(
                    title = "Otomatik Servis Başlatma",
                    description = "Uygulama açıldığında clipboard servisini otomatik başlat",
                    icon = Icons.Default.PlayArrow,
                    trailing = {
                        Switch(
                            checked = uiState.autoStartService,
                            onCheckedChange = { viewModel.toggleAutoStartService() }
                        )
                    }
                )

                SettingsItem(
                    title = "Maksimum Geçmiş Öğesi",
                    description = "${uiState.maxHistoryItems} öğe saklanacak",
                    icon = Icons.Default.Storage,
                    onClick = { viewModel.showMaxItemsDialog() },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // Güvenlik Ayarları
            SettingsSection(title = "🔒 Güvenlik") {
                SettingsItem(
                    title = "Güvenli Mod",
                    description = "Hassas verileri otomatik algıla ve şifrele",
                    icon = Icons.Default.Security,
                    trailing = {
                        Switch(
                            checked = uiState.enableSecureMode,
                            onCheckedChange = { viewModel.toggleSecureMode() }
                        )
                    }
                )

                SettingsItem(
                    title = "Biyometrik Doğrulama",
                    description = "Güvenli pano için parmak izi/yüz tanıma",
                    icon = Icons.Default.Fingerprint,
                    onClick = { /* Biometric ayarları */ },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            // Veri Yönetimi
            SettingsSection(title = "💾 Veri Yönetimi") {
                SettingsItem(
                    title = "Verileri Dışa Aktar",
                    description = "Clipboard geçmişini JSON olarak dışa aktar",
                    icon = Icons.Default.FileUpload,
                    onClick = { /* Export işlemi */ },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                SettingsItem(
                    title = "Verileri İçe Aktar",
                    description = "Önceki yedekten geri yükle",
                    icon = Icons.Default.FileDownload,
                    onClick = { /* Import işlemi */ },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                SettingsItem(
                    title = "Tüm Verileri Sil",
                    description = "Tüm clipboard geçmişini kalıcı olarak sil",
                    icon = Icons.Default.DeleteForever,
                    onClick = { /* Clear all data */ },
                    textColor = MaterialTheme.colorScheme.error,
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }

            // Premium
            SettingsSection(title = "⭐ Premium") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⭐",
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Premium'a Geçin",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )

                                Text(
                                    text = "Sınırsız etiket, tema seçenekleri ve daha fazlası",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { /* Premium satın alma */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("₺40 - Premium Satın Al")
                        }
                    }
                }
            }

            // Hakkında
            SettingsSection(title = "ℹ️ Hakkında") {
                SettingsItem(
                    title = "Sürüm",
                    description = "Clipbo v1.0 (1)",
                    icon = Icons.Default.Info,
                    onClick = { /* Version info */ }
                )

                SettingsItem(
                    title = "Gizlilik Politikası",
                    description = "Verilerinizi nasıl koruduğumuzu öğrenin",
                    icon = Icons.Default.PrivacyTip,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://clipbo.app/privacy"))
                        context.startActivity(intent)
                    },
                    trailing = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                SettingsItem(
                    title = "Kullanım Koşulları",
                    description = "Hizmet şartlarımızı inceleyin",
                    icon = Icons.Default.Gavel,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://clipbo.app/terms"))
                        context.startActivity(intent)
                    },
                    trailing = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                SettingsItem(
                    title = "Destek",
                    description = "Yardım almak için bize yazın",
                    icon = Icons.Default.Support,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@clipbo.app")
                            putExtra(Intent.EXTRA_SUBJECT, "Clipbo Destek")
                        }
                        context.startActivity(intent)
                    },
                    trailing = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                SettingsItem(
                    title = "Uygulamamızı Değerlendirin",
                    description = "Google Play'de 5 yıldız verin",
                    icon = Icons.Default.Star,
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                        context.startActivity(intent)
                    },
                    trailing = {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }

    // Maksimum öğe sayısı dialog'u
    if (uiState.showMaxItemsDialog) {
        MaxItemsDialog(
            currentValue = uiState.maxHistoryItems,
            onDismiss = { viewModel.hideMaxItemsDialog() },
            onConfirm = { newValue ->
                viewModel.setMaxHistoryItems(newValue)
                viewModel.hideMaxItemsDialog()
            }
        )
    }
}