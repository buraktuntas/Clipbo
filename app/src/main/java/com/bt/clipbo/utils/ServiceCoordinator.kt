package com.bt.clipbo.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.bt.clipbo.data.service.ClipboardService
import com.bt.clipbo.data.service.ClipboAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class ServiceStatus(
    val isClipboardServiceRunning: Boolean = false,
    val isAccessibilityServiceRunning: Boolean = false,
    val hasAllPermissions: Boolean = false,
    val errorMessage: String? = null
)

@Singleton
class ServiceCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionHelper: PermissionHelper
) {

    companion object {
        private const val TAG = "ServiceCoordinator"
        private const val PERMISSION_CHECK_DELAY = 2000L
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    private val coordinatorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _serviceStatus = MutableStateFlow(ServiceStatus())
    val serviceStatus: StateFlow<ServiceStatus> = _serviceStatus.asStateFlow()

    private var retryAttempts = 0

    init {
        // Initial status check
        coordinatorScope.launch {
            checkServiceStatus()
        }
    }

    /**
     * Ana service başlatma methodu - Proper coordination
     */
    suspend fun startServices(): Result<String> {
        Log.d(TAG, "Starting service coordination...")

        return try {
            // 1. İzin kontrolü
            if (!checkAllPermissions()) {
                return Result.failure(Exception("Required permissions not granted"))
            }

            // 2. Service'leri sıralı olarak başlat
            val accessibilityResult = startAccessibilityServiceIfNeeded()
            if (accessibilityResult.isFailure) {
                return accessibilityResult
            }

            // 3. Accessibility service'in aktif olmasını bekle
            delay(PERMISSION_CHECK_DELAY)

            // 4. Clipboard service'i başlat
            val clipboardResult = startClipboardService()
            if (clipboardResult.isFailure) {
                return clipboardResult
            }

            // 5. Final status check
            checkServiceStatus()

            retryAttempts = 0
            Result.success("Services started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start services", e)
            _serviceStatus.value = _serviceStatus.value.copy(
                errorMessage = "Service başlatma hatası: ${e.message}"
            )
            Result.failure(e)
        }
    }

    /**
     * Service'leri durdur
     */
    suspend fun stopServices(): Result<String> {
        Log.d(TAG, "Stopping services...")

        return try {
            // Clipboard service'i durdur
            ClipboardService.stopService(context)

            // Status güncelle
            delay(1000)
            checkServiceStatus()

            Result.success("Services stopped successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop services", e)
            Result.failure(e)
        }
    }

    /**
     * İzin kontrolü - Koordinasyonlu
     */
    private fun checkAllPermissions(): Boolean {
        val permissions = permissionHelper.getAllPermissionStatuses()
        val hasAll = permissions.values.all { it.isGranted }

        _serviceStatus.value = _serviceStatus.value.copy(
            hasAllPermissions = hasAll
        )

        return hasAll
    }

    /**
     * Accessibility service başlatma
     */
    private suspend fun startAccessibilityServiceIfNeeded(): Result<String> {
        if (!permissionHelper.hasAccessibilityPermission().isGranted) {
            Log.w(TAG, "Accessibility permission not granted")
            return Result.failure(Exception("Accessibility permission required"))
        }

        // Service zaten çalışıyor mu kontrol et
        if (ClipboAccessibilityService.isRunning()) {
            Log.d(TAG, "Accessibility service already running")
            return Result.success("Already running")
        }

        // Permission var ama service çalışmıyor - kullanıcıyı yönlendir
        Log.d(TAG, "Redirecting user to accessibility settings")
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return Result.success("Redirected to settings")
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /**
     * Clipboard service başlatma
     */
    private suspend fun startClipboardService(): Result<String> {
        return try {
            if (!ClipboardService.isServiceRunning(context)) {
                ClipboardService.startService(context)
                Log.d(TAG, "Clipboard service started")
            } else {
                Log.d(TAG, "Clipboard service already running")
            }
            Result.success("Clipboard service active")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start clipboard service", e)
            Result.failure(e)
        }
    }

    /**
     * Service durumlarını kontrol et
     */
    private suspend fun checkServiceStatus() {
        try {
            val isClipboardRunning = ClipboardService.isServiceRunning(context)
            val isAccessibilityRunning = ClipboAccessibilityService.isRunning()
            val hasPermissions = checkAllPermissions()

            _serviceStatus.value = ServiceStatus(
                isClipboardServiceRunning = isClipboardRunning,
                isAccessibilityServiceRunning = isAccessibilityRunning,
                hasAllPermissions = hasPermissions,
                errorMessage = null
            )

            Log.d(TAG, "Service status updated: Clipboard=$isClipboardRunning, " +
                    "Accessibility=$isAccessibilityRunning, Permissions=$hasPermissions")

        } catch (e: Exception) {
            Log.e(TAG, "Failed to check service status", e)
            _serviceStatus.value = _serviceStatus.value.copy(
                errorMessage = "Status kontrol hatası: ${e.message}"
            )
        }
    }

    /**
     * Retry mechanism
     */
    suspend fun retryServiceStart(): Result<String> {
        if (retryAttempts >= MAX_RETRY_ATTEMPTS) {
            return Result.failure(Exception("Maximum retry attempts reached"))
        }

        retryAttempts++
        Log.d(TAG, "Retrying service start (attempt $retryAttempts/$MAX_RETRY_ATTEMPTS)")

        delay(1000 * retryAttempts.toLong())
        return startServices()
    }

    /**
     * Service health check - Background monitoring
     */
    fun startHealthCheck() {
        coordinatorScope.launch {
            while (true) {
                delay(30000) // 30 saniyede bir kontrol
                checkServiceStatus()

                // Auto-recovery deneme
                val status = _serviceStatus.value
                if (status.hasAllPermissions &&
                    !status.isClipboardServiceRunning &&
                    retryAttempts < MAX_RETRY_ATTEMPTS) {

                    Log.w(TAG, "Clipboard service down, attempting auto-recovery")
                    startClipboardService()
                }
            }
        }
    }

    /**
     * Overlay permission için özel handling
     */
    fun requestOverlayPermission(): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissionHelper.getOverlayPermissionIntent()
        } else null
    }

    /**
     * Notification permission için özel handling
     */
    fun requestNotificationPermission(): Boolean {
        return permissionHelper.hasNotificationPermission().isGranted
    }

    /**
     * Service status özeti
     */
    fun getServiceSummary(): String {
        val status = _serviceStatus.value
        return buildString {
            appendLine("🔧 Service Status Summary")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📋 Clipboard Service: ${if (status.isClipboardServiceRunning) "✅ Running" else "❌ Stopped"}")
            appendLine("♿ Accessibility Service: ${if (status.isAccessibilityServiceRunning) "✅ Running" else "❌ Stopped"}")
            appendLine("🔑 Permissions: ${if (status.hasAllPermissions) "✅ Granted" else "❌ Missing"}")

            if (status.errorMessage != null) {
                appendLine("⚠️ Error: ${status.errorMessage}")
            }

            val overallStatus = when {
                status.isClipboardServiceRunning && status.isAccessibilityServiceRunning -> "🟢 Fully Operational"
                status.isClipboardServiceRunning || status.isAccessibilityServiceRunning -> "🟡 Partially Running"
                else -> "🔴 Not Running"
            }

            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Overall Status: $overallStatus")
        }
    }

    /**
     * Resource cleanup
     */
    fun cleanup() {
        coordinatorScope.launch {
            stopServices()
        }
    }
}