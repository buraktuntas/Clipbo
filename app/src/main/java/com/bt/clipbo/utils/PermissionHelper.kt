package com.bt.clipbo.utils

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import com.bt.clipbo.data.service.ClipboAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionHelper
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        data class PermissionStatus(
            val isGranted: Boolean,
            val shouldShowRationale: Boolean = false,
            val message: String = "",
            val actionRequired: String = "",
        )

        companion object {
            const val REQUEST_OVERLAY_PERMISSION = 1001
            const val REQUEST_ACCESSIBILITY_PERMISSION = 1002
            const val REQUEST_NOTIFICATION_PERMISSION = 1003
        }

        /**
         * Bildirim izni kontrolü
         */
        fun hasNotificationPermission(): PermissionStatus {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val isGranted =
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED

                PermissionStatus(
                    isGranted = isGranted,
                    message = if (isGranted) "Bildirim izni verildi" else "Bildirim izni gerekli",
                    actionRequired = if (!isGranted) "POST_NOTIFICATIONS izni gerekli" else "",
                )
            } else {
                PermissionStatus(
                    isGranted = true,
                    message = "Android 13 öncesi otomatik izinli",
                )
            }
        }

        /**
         * Overlay (Diğer uygulamaların üzerine çizme) izni kontrolü
         */
        fun hasOverlayPermission(): PermissionStatus {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val isGranted = Settings.canDrawOverlays(context)
                PermissionStatus(
                    isGranted = isGranted,
                    message = if (isGranted) "Overlay izni verildi" else "Overlay izni clipboard dinleme için gerekli",
                    actionRequired = if (!isGranted) "Diğer uygulamaların üzerine çizme izni gerekli" else "",
                )
            } else {
                PermissionStatus(
                    isGranted = true,
                    message = "Android 6.0 öncesi otomatik izinli",
                )
            }
        }

        /**
         * Erişilebilirlik servisi izni kontrolü
         */
        fun hasAccessibilityPermission(): PermissionStatus {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

            val isGranted =
                enabledServices.any { service ->
                    service.resolveInfo.serviceInfo.packageName == context.packageName &&
                        service.resolveInfo.serviceInfo.name == ClipboAccessibilityService::class.java.name
                }

            return PermissionStatus(
                isGranted = isGranted,
                message = if (isGranted) "Erişilebilirlik izni verildi" else "Clipboard erişimi için erişilebilirlik izni gerekli",
                actionRequired = if (!isGranted) "Erişilebilirlik ayarlarından Clipbo'yu etkinleştirin" else "",
            )
        }

        /**
         * Tüm izinlerin durumunu döndürür
         */
        fun getAllPermissionStatuses(): Map<String, PermissionStatus> {
            return mapOf(
                "notification" to hasNotificationPermission(),
                "overlay" to hasOverlayPermission(),
                "accessibility" to hasAccessibilityPermission(),
            )
        }

        /**
         * Bildirim izni intent'i (Android 13+)
         */
        fun getNotificationPermissionIntent(): Intent? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Bu intent MainActivity'de ActivityResultContracts.RequestPermission() ile kullanılır
                null // ActivityResultLauncher gerekli
            } else {
                null
            }
        }

        /**
         * Overlay izni için ayarlar intent'i
         */
        fun getOverlayPermissionIntent(): Intent? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}"),
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                null
            }
        }

        /**
         * Erişilebilirlik ayarları intent'i
         */
        fun getAccessibilityPermissionIntent(): Intent {
            return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        /**
         * Uygulama detay ayarları intent'i
         */
        fun getAppSettingsIntent(): Intent {
            return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        /**
         * Runtime'da istenmesi gereken izinler listesi
         */
        fun getRequiredRuntimePermissions(): List<String> {
            val permissions = mutableListOf<String>()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            return permissions
        }

        /**
         * Tüm izinler verilmiş mi kontrol et
         */
        fun areAllPermissionsGranted(): Boolean {
            val statuses = getAllPermissionStatuses()
            return statuses.values.all { it.isGranted }
        }

        /**
         * Eksik izinlerin listesini döndürür
         */
        fun getMissingPermissions(): List<String> {
            val statuses = getAllPermissionStatuses()
            return statuses.filter { !it.value.isGranted }.keys.toList()
        }

        /**
         * İzin durumu açıklaması
         */
        fun getPermissionExplanation(permissionType: String): String {
            return when (permissionType) {
                "notification" ->
                    """
                    📢 Bildirim İzni
                    
                    Bu izin Clipbo'nun arka planda çalışırken size bildirim gösterebilmesi için gereklidir.
                    
                    Kullanım alanları:
                    • Clipboard servisi aktif durumu bildirimi
                    • Yeni kopyalanan içerik bildirimleri
                    • Hata durumu uyarıları
                    
                    Android 13+ cihazlarda zorunludur.
                    """.trimIndent()

                "overlay" ->
                    """
                    🔄 Overlay İzni
                    
                    Bu izin Clipbo'nun diğer uygulamaların üzerine küçük pencereler çizebilmesi için gereklidir.
                    
                    Kullanım alanları:
                    • Clipboard geçmişini hızlı erişim penceresi
                    • Kopyalanan içeriği anında gösterme
                    • Floating widget desteği
                    
                    Clipboard izleme için önemlidir.
                    """.trimIndent()

                "accessibility" ->
                    """
                    ♿ Erişilebilirlik İzni
                    
                    Bu izin Clipbo'nun sistem clipboard'una erişebilmesi için gereklidir.
                    
                    Kullanım alanları:
                    • Kopyalanan metinleri otomatik yakalama
                    • Clipboard değişikliklerini izleme
                    • Uygulama dışı clipboard erişimi
                    
                    Ana fonksiyon için zorunludur.
                    
                    ⚠️ Bu izin sadece clipboard erişimi için kullanılır, 
                    diğer uygulamalardaki verilerinize erişmez.
                    """.trimIndent()

                else -> "Bilinmeyen izin türü"
            }
        }

        /**
         * İzin verme adımları rehberi
         */
        fun getPermissionSteps(permissionType: String): List<String> {
            return when (permissionType) {
                "notification" ->
                    listOf(
                        "1. 'İzin Ver' butonuna basın",
                        "2. Açılan pencerede 'İzin Ver'i seçin",
                        "3. Clipbo'ya dönün",
                    )

                "overlay" ->
                    listOf(
                        "1. 'Ayarlara Git' butonuna basın",
                        "2. 'Diğer uygulamaların üzerine çizme' seçeneğini bulun",
                        "3. Clipbo'yu listeden bulun ve etkinleştirin",
                        "4. Clipbo'ya dönün",
                    )

                "accessibility" ->
                    listOf(
                        "1. 'Erişilebilirlik Ayarları'na gidin",
                        "2. 'İndirilen uygulamalar' bölümünü bulun",
                        "3. 'Clipbo' servisini bulun ve dokunun",
                        "4. 'Kullan' düğmesini etkinleştirin",
                        "5. Uyarıyı onaylayın ve Clipbo'ya dönün",
                    )

                else -> listOf("Bilinmeyen izin türü")
            }
        }

        /**
         * İzin durumu emoji'si
         */
        fun getPermissionStatusEmoji(permissionType: String): String {
            val status =
                when (permissionType) {
                    "notification" -> hasNotificationPermission()
                    "overlay" -> hasOverlayPermission()
                    "accessibility" -> hasAccessibilityPermission()
                    else -> PermissionStatus(false)
                }

            return if (status.isGranted) "✅" else "❌"
        }

        /**
         * Kritik izinler (uygulama çalışması için gerekli)
         */
        fun getCriticalPermissions(): List<String> {
            return listOf("accessibility") // Clipboard erişimi için kritik
        }

        /**
         * Opsiyonel izinler (uygulama çalışabilir ama özellikler kısıtlı)
         */
        fun getOptionalPermissions(): List<String> {
            return listOf("notification", "overlay")
        }

        /**
         * İzin durumu raporu
         */
        fun generatePermissionReport(): String {
            val statuses = getAllPermissionStatuses()
            val report = StringBuilder()

            report.appendLine("📋 Clipbo İzin Durumu Raporu")
            report.appendLine("=" * 40)

            statuses.forEach { (type, status) ->
                val emoji = getPermissionStatusEmoji(type)
                val typeName =
                    when (type) {
                        "notification" -> "Bildirim"
                        "overlay" -> "Overlay"
                        "accessibility" -> "Erişilebilirlik"
                        else -> type
                    }

                report.appendLine("$emoji $typeName: ${if (status.isGranted) "VERİLDİ" else "EKSİK"}")
                if (!status.isGranted && status.actionRequired.isNotEmpty()) {
                    report.appendLine("   → ${status.actionRequired}")
                }
            }

            report.appendLine("=" * 40)
            val grantedCount = statuses.values.count { it.isGranted }
            val totalCount = statuses.size
            report.appendLine("Toplam: $grantedCount/$totalCount izin verildi")

            if (areAllPermissionsGranted()) {
                report.appendLine("🎉 Tüm izinler tamam! Clipbo kullanıma hazır.")
            } else {
                report.appendLine("⚠️ Eksik izinler var. Tam fonksiyonalite için tamamlayın.")
            }

            return report.toString()
        }
    }

// Extension function for String repeat (Kotlin doesn't have it built-in)
private operator fun String.times(count: Int): String {
    return this.repeat(count)
}
