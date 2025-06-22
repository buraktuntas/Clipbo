package com.bt.clipbo.presentation.ui.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bt.clipbo.data.preferences.UserPreferences
import com.bt.clipbo.data.service.ClipboardService
import com.bt.clipbo.presentation.ui.components.RatingDialog
import com.bt.clipbo.presentation.ui.components.RatingViewModel
import com.bt.clipbo.ui.theme.ClipboTheme
import com.bt.clipbo.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Screen {
    data object Navigation : Screen()

    data object History : Screen()

    data object Search : Screen()

    data object Tags : Screen()

    data object Secure : Screen()

    data object Settings : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val ratingViewModel: RatingViewModel by viewModels()
    @Inject lateinit var userPreferences: UserPreferences
    private var isPermissionCheckCompleted = false

    // Notification permission launcher
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted ->
            if (isGranted) {
                showToast("Bildirim izni verildi ✅")
                startClipboardService()
            } else {
                showToast("⚠️ Bildirim izni reddedildi. Ayarlardan izin verebilirsiniz.")
                // Ayarlara yönlendir
                openAppSettings()
            }
        }

    // Overlay permission launcher
    private val overlayPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) { result ->
            if (Settings.canDrawOverlays(this)) {
                showToast("Overlay izni verildi ✅")
                checkNotificationPermissionAndStart()
            } else {
                showToast("⚠️ Overlay izni gerekli. Clipboard dinleme için gerekli.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ClipboTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val ratingState by ratingViewModel.state.collectAsState()

                    // Rating dialog'u
                    if (ratingState.showDialog) {
                        RatingDialog(
                            onDismiss = { ratingViewModel.hideRatingDialog() },
                            onRate = { ratingViewModel.onRateNow(this@MainActivity) },
                            onLater = { ratingViewModel.onRateLater() },
                        )
                    }

                    ClipboApp(
                        onStartService = { checkAllPermissionsAndStartService() },
                        onStopService = { stopClipboardService() },
                    )
                }
            }
        }

        // Otomatik başlatma tercihini kontrol et (izin istemeden önce servis başlatma girişimi yok)
        /*
        lifecycleScope.launch {
            delay(500)
            if (!isPermissionCheckCompleted) {
                checkAllPermissionsAndStartService()
                isPermissionCheckCompleted = true

                if (userPreferences.autoStartService.first()) {
                    startClipboardService()
                }
            }
        }
        */
    }

    private fun checkAllPermissionsAndStartService() {
        try {
            // Android 6.0+ overlay izni kontrol et
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    showToast("📋 Clipboard dinleme için overlay izni gerekli")
                    requestOverlayPermission()
                    return
                }
            }

            // Notification izni kontrol et
            checkNotificationPermissionAndStart()
        } catch (e: Exception) {
            showToast("❌ İzin kontrolü hatası: ${e.message}")
        }
    }

    private fun requestOverlayPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent =
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName"),
                    )
                overlayPermissionLauncher.launch(intent)
            }
        } catch (e: Exception) {
            showToast("❌ Overlay izni isteği hatası: ${e.message}")
        }
    }

    private fun checkNotificationPermissionAndStart() {
        try {
            val permissionHelper = PermissionHelper(this)
            if (permissionHelper.hasNotificationPermission().isGranted) {
                startClipboardService()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    showToast("📔 Bildirim izni isteniyor...")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    startClipboardService()
                }
            }
        } catch (e: Exception) {
            showToast("❌ Bildirim izni kontrolü hatası: ${e.message}")
        }
    }

    private fun startClipboardService() {
        try {
            ClipboardService.startService(this)
            showToast("🚀 Clipboard servisi başlatıldı!")

            // Test için bir şey kopyalatmaya davet et
            lifecycleScope.launch {
                delay(2000) // 2 saniye bekle
                showToast("📋 Artık herhangi bir metni kopyalayabilirsiniz")
            }
        } catch (e: Exception) {
            showToast("❌ Servis başlatılamadı: ${e.message}")
        }
    }

    private fun stopClipboardService() {
        try {
            ClipboardService.stopService(this)
            showToast("⏹️ Clipboard servisi durduruldu")
        } catch (e: Exception) {
            showToast("❌ Servis durdurulamadı: ${e.message}")
        }
    }

    private fun openAppSettings() {
        try {
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
            startActivity(intent)
        } catch (e: Exception) {
            showToast("Ayarlar açılamadı")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
