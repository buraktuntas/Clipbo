package com.bt.clipbo.presentation.ui.settings

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bt.clipbo.presentation.ui.components.BackupProgressDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // File picker launchers
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importBackup(it) }
    }

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

            // Veri Yönetimi - BACKUP/RESTORE BÖLÜMÜ
            SettingsSection(title = "💾 Veri Yönetimi") {
                SettingsItem(
                    title = "Verileri Dışa Aktar",
                    description = "Clipboard geçmişini JSON olarak dışa aktar",
                    icon = Icons.Default.FileUpload,
                    onClick = {
                        exportLauncher.launch("clipbo_backup_${System.currentTimeMillis()}.json")
                    },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                SettingsItem(
                    title = "Verileri İçe Aktar",
                    description = "Önceki yedekten geri yükle",
                    icon = Icons.Default.FileDownload,
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    },
                    trailing = {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )

                SettingsItem(
                    title = "Yerel Yedekler",
                    description = "Otomatik oluşturulan yedekleri görüntüle",
                    icon = Icons.Default.Folder,
                    onClick = { viewModel.showLocalBackupsDialog() },
                    trailing = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (uiState.localBackupCount > 0) {
                                Text(
                                    text = "${uiState.localBackupCount}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )

                SettingsItem(
                    title = "Otomatik Yedekleme",
                    description = if (uiState.autoBackupEnabled) "Günlük otomatik yedek oluşturuluyor"
                    else "Otomatik yedekleme kapalı",
                    icon = Icons.Default.Schedule,
                    trailing = {
                        Switch(
                            checked = uiState.autoBackupEnabled,
                            onCheckedChange = { viewModel.toggleAutoBackup() }
                        )
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                SettingsItem(
                    title = "Tüm Verileri Sil",
                    description = "Tüm clipboard geçmişini kalıcı olarak sil",
                    icon = Icons.Default.DeleteForever,
                    onClick = { viewModel.showClearAllDataDialog() },
                    textColor = MaterialTheme.colorScheme.error,
                    trailing = {
                        Icon(
                            Icons.Default.Warning,
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
                                    text = "Sınırsız etiket, cloud sync ve daha fazlası",
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
                    description = "Clipbo v${uiState.appVersion}",
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

    // Dialogs
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

    if (uiState.showBackupProgress) {
        BackupProgressDialog(
            backupRestoreManager = viewModel.getBackupRestoreManager(),
            onDismiss = { viewModel.hideBackupProgress() }
        )
    }

    if (uiState.showLocalBackupsDialog) {
        LocalBackupsDialog(
            localBackups = uiState.localBackups,
            onDismiss = { viewModel.hideLocalBackupsDialog() },
            onRestoreBackup = { backupInfo ->
                viewModel.restoreLocalBackup(backupInfo)
            },
            onDeleteBackup = { backupInfo ->
                viewModel.deleteLocalBackup(backupInfo)
            }
        )
    }

    if (uiState.showClearAllDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearAllDataDialog() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("⚠️ Tüm Verileri Sil") },
            text = {
                Text(
                    "Bu işlem geri alınamaz!\n\nTüm clipboard geçmişi, etiketler ve ayarlar kalıcı olarak silinecek.\n\nDevam etmek istediğinizden emin misiniz?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        viewModel.hideClearAllDataDialog()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Evet, Tümünü Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearAllDataDialog() }) {
                    Text("İptal")
                }
            }
        )
    }

    // Toast messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearToastMessage()
        }
    }
}