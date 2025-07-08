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
import com.bt.clipbo.presentation.ui.components.RatingDialog
import com.bt.clipbo.presentation.ui.components.RatingViewModel
import com.bt.clipbo.ui.theme.ClipboTheme
import com.bt.clipbo.utils.ServiceCoordinator
import com.bt.clipbo.utils.PermissionHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
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

    // Hilt injection
    @Inject lateinit var serviceCoordinator: ServiceCoordinator
    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var permissionHelper: PermissionHelper

    private val ratingViewModel: RatingViewModel by viewModels()
    private var isInitialized = false

    // Permission launchers
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handleNotificationPermissionResult(isGranted)
    }

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleOverlayPermissionResult()
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
                    val serviceStatus by serviceCoordinator.serviceStatus.collectAsState()

                    // Rating dialog
                    if (ratingState.showDialog) {
                        RatingDialog(
                            onDismiss = { ratingViewModel.hideRatingDialog() },
                            onRate = { ratingViewModel.onRateNow(this@MainActivity) },
                            onLater = { ratingViewModel.onRateLater() },
                        )
                    }

                    // Service status monitoring
                    LaunchedEffect(serviceStatus) {
                        if (serviceStatus.errorMessage != null) {
                            showToast("⚠️ ${serviceStatus.errorMessage}")
                        }
                    }

                    ClipboApp(
                        onStartService = {
                            startServicesWithCoordination()
                        },
                        onStopService = {
                            stopServicesWithCoordination()
                        },
                    )
                }
            }
        }

        // Initialize services
        initializeApp()
    }

    override fun onResume() {
        super.onResume()
        // Service status'unu tekrar kontrol et
        if (isInitialized) {
            lifecycleScope.launch {
                delay(500) // UI settling time
                checkAndUpdateServiceStatus()
            }
        }
    }

    private fun initializeApp() {
        lifecycleScope.launch {
            try {
                // Health check başlat
                serviceCoordinator.startHealthCheck()

                // Auto-start kontrolü
                if (userPreferences.autoStartService.first()) {
                    delay(1000) // UI'nin tamamen yüklenmesini bekle
                    startServicesWithCoordination()
                }

                isInitialized = true
                showToast("📋 Clipbo hazır!")

            } catch (e: Exception) {
                showToast("❌ Başlatma hatası: ${e.message}")
            }
        }
    }

    /**
     * Koordinasyonlu service başlatma
     */
    private fun startServicesWithCoordination() {
        lifecycleScope.launch {
            try {
                showToast("🚀 Servisler başlatılıyor...")

                // Permission kontrolü
                if (!checkAllPermissions()) {
                    requestMissingPermissions()
                    return@launch
                }

                // Service'leri koordinasyonlu başlat
                val result = serviceCoordinator.startServices()

                if (result.isSuccess) {
                    showToast("✅ ${result.getOrNull()}")

                    // Service status debug
                    delay(2000)
                    showToast(serviceCoordinator.getServiceSummary())

                } else {
                    val error = result.exceptionOrNull()
                    showToast("❌ Başlatma hatası: ${error?.message}")

                    // Retry option
                    showRetryOption()
                }

            } catch (e: Exception) {
                showToast("❌ Kritik hata: ${e.message}")
            }
        }
    }

    /**
     * Service durdurma
     */
    private fun stopServicesWithCoordination() {
        lifecycleScope.launch {
            try {
                val result = serviceCoordinator.stopServices()

                if (result.isSuccess) {
                    showToast("⏹️ Servisler durduruldu")
                } else {
                    showToast("❌ Durdurma hatası: ${result.exceptionOrNull()?.message}")
                }

            } catch (e: Exception) {
                showToast("❌ Durdurma hatası: ${e.message}")
            }
        }
    }

    /**
     * Tüm izinleri kontrol et
     */
    private fun checkAllPermissions(): Boolean {
        val statuses = permissionHelper.getAllPermissionStatuses()
        return statuses.values.all { it.isGranted }
    }

    /**
     * Eksik izinleri iste
     */
    private fun requestMissingPermissions() {
        lifecycleScope.launch {
            try {
                // Notification permission (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (!permissionHelper.hasNotificationPermission().isGranted) {
                        showToast("📢 Bildirim izni isteniyor...")
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        return@launch
                    }
                }

                // Overlay permission
                if (!permissionHelper.hasOverlayPermission().isGranted) {
                    showToast("🔄 Overlay izni isteniyor...")
                    requestOverlayPermission()
                    return@launch
                }

                // Accessibility permission
                if (!permissionHelper.hasAccessibilityPermission().isGranted) {
                    showToast("♿ Erişilebilirlik izni gerekli")
                    showAccessibilityPermissionDialog()
                    return@launch
                }

            } catch (e: Exception) {
                showToast("❌ İzin kontrolü hatası: ${e.message}")
            }
        }
    }

    /**
     * Notification permission sonucu
     */
    private fun handleNotificationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            showToast("✅ Bildirim izni verildi")
            // Bir sonraki izni kontrol et
            requestMissingPermissions()
        } else {
            showToast("⚠️ Bildirim izni reddedildi")
            showPermissionDeniedDialog("bildirim")
        }
    }

    /**
     * Overlay permission isteği
     */
    private fun requestOverlayPermission() {
        try {
            val intent = serviceCoordinator.requestOverlayPermission()
            if (intent != null) {
                overlayPermissionLauncher.launch(intent)
            } else {
                showToast("❌ Overlay izni desteklenmiyor")
            }
        } catch (e: Exception) {
            showToast("❌ Overlay izni hatası: ${e.message}")
        }
    }

    /**
     * Overlay permission sonucu
     */
    private fun handleOverlayPermissionResult() {
        if (Settings.canDrawOverlays(this)) {
            showToast("✅ Overlay izni verildi")
            // Bir sonraki izni kontrol et
            requestMissingPermissions()
        } else {
            showToast("⚠️ Overlay izni reddedildi")
            showPermissionDeniedDialog("overlay")
        }
    }

    /**
     * Accessibility permission dialog
     */
    private fun showAccessibilityPermissionDialog() {
        // Bu method UI'de bir dialog gösterebilir
        // Şimdilik basit toast ile
        showToast("♿ Erişilebilirlik ayarlarına yönlendirileceksiniz")

        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            showToast("❌ Ayarlar açılamadı: ${e.message}")
        }
    }

    /**
     * İzin reddedildi dialog
     */
    private fun showPermissionDeniedDialog(permissionType: String) {
        showToast("⚠️ $permissionType izni olmadan tam işlevsellik sağlanamaz")

        // Kullanıcıyı app settings'e yönlendir
        lifecycleScope.launch {
            delay(2000)
            try {
                val intent = permissionHelper.getAppSettingsIntent()
                startActivity(intent)
            } catch (e: Exception) {
                showToast("❌ Ayarlar açılamadı")
            }
        }
    }

    /**
     * Retry option göster
     */
    private fun showRetryOption() {
        showToast("🔄 5 saniye sonra tekrar denenecek...")

        lifecycleScope.launch {
            delay(5000)

            val retryResult = serviceCoordinator.retryServiceStart()
            if (retryResult.isSuccess) {
                showToast("✅ Tekrar deneme başarılı!")
            } else {
                showToast("❌ Tekrar deneme başarısız: ${retryResult.exceptionOrNull()?.message}")
            }
        }
    }

    /**
     * Service status kontrolü ve güncelleme
     */
    private fun checkAndUpdateServiceStatus() {
        lifecycleScope.launch {
            try {
                // Manual status check
                val summary = serviceCoordinator.getServiceSummary()

                // Debug için log (production'da kaldırılabilir)
                if (com.bt.clipbo.BuildConfig.DEBUG) {
                    println(summary)
                }

            } catch (e: Exception) {
                showToast("⚠️ Status kontrol hatası: ${e.message}")
            }
        }
    }

    /**
     * Toast helper
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Long toast helper
     */
    private fun showLongToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup
        if (isInitialized) {
            serviceCoordinator.cleanup()
        }
    }
}