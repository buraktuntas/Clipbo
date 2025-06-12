package com.bt.clipbo.presentation.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.data.service.ClipboardService
import com.bt.clipbo.domain.usecase.ClipboardUseCase
import com.bt.clipbo.presentation.ui.components.RatingDialog
import com.bt.clipbo.presentation.ui.components.RatingViewModel
import com.bt.clipbo.ui.theme.ClipboTheme
import com.bt.clipbo.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class Screen {
    object Navigation : Screen()
    object History : Screen()
    object Search : Screen()
    object Tags : Screen()
    object Secure : Screen()
    object Settings : Screen()
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val ratingViewModel: RatingViewModel by viewModels()

    // Notification permission launcher
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
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
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
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

        // Servisi otomatik başlat
        checkAllPermissionsAndStartService()

        setContent {
            ClipboTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val ratingState by ratingViewModel.state.collectAsState()

                    // Rating dialog'u
                    if (ratingState.showDialog) {
                        RatingDialog(
                            onDismiss = { ratingViewModel.hideRatingDialog() },
                            onRate = { ratingViewModel.onRateNow(this) },
                            onLater = { ratingViewModel.onRateLater() }
                        )
                    }

                    ClipboApp(
                        onStartService = { checkAllPermissionsAndStartService() },
                        onStopService = { stopClipboardService() }
                    )
                }
            }
        }
    }

    private fun checkAllPermissionsAndStartService() {
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
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        }
    }

    private fun checkNotificationPermissionAndStart() {
        if (PermissionHelper(this).hasNotificationPermission().isGranted) {
            startClipboardService()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showToast("📔 Bildirim izni isteniyor...")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startClipboardService()
            }
        }
    }

    private fun startClipboardService() {
        try {
            ClipboardService.startService(this)
            showToast("🚀 Clipboard servisi başlatıldı!")

            // Test için bir şey kopyalatmaya davet et
            showToast("📋 Artık herhangi bir metni kopyalayabilirsiniz")
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
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
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
