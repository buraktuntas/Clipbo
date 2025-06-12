package com.bt.clipbo.data.service

import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bt.clipbo.R
import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.widget.WidgetUtils
import com.bt.clipbo.widget.repository.WidgetRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ClipboardService : Service() {

    @Inject
    lateinit var clipboardDao: ClipboardDao

    private lateinit var clipboardManager: ClipboardManager
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastClipboardContent = ""

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "clipboard_service_channel"
        private const val TAG = "ClipboardService"

        fun startService(context: Context) {
            val intent = Intent(context, ClipboardService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, ClipboardService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "🚀 ClipboardService onCreate() çağrıldı")

        try {
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            Log.d(TAG, "✅ ClipboardManager alındı")

            createNotificationChannel()
            Log.d(TAG, "✅ Notification channel oluşturuldu")

            startForeground(NOTIFICATION_ID, createNotification())
            Log.d(TAG, "✅ Foreground service başlatıldı")

            // Clipboard listener ekle
            clipboardManager.addPrimaryClipChangedListener(clipboardListener)
            Log.d(TAG, "✅ Clipboard listener eklendi")

            // İlk clipboard içeriğini al
            val initialClip = clipboardManager.primaryClip
            if (initialClip != null && initialClip.itemCount > 0) {
                val initialText = initialClip.getItemAt(0).text?.toString()
                if (!initialText.isNullOrEmpty()) {
                    lastClipboardContent = initialText
                    Log.d(TAG, "📋 İlk clipboard içeriği: ${initialText.take(30)}...")
                }
            }

            // Widget repository status'unu güncelle
            updateWidgetServiceStatus(true)

            Log.d(TAG, "🎉 ClipboardService tamamen başlatıldı!")

            // Ana thread'de toast göster
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@ClipboardService, "📋 Clipboard dinleme aktif!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ ClipboardService başlatma hatası: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🛑 ClipboardService durduruluyor...")

        try {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener)
            Log.d(TAG, "✅ Clipboard listener kaldırıldı")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Listener kaldırma hatası: ${e.message}")
        }

        // Widget repository status'unu güncelle
        updateWidgetServiceStatus(false)

        // Widget'ları güncelle
        updateAllWidgets()

        serviceScope.cancel()
        Log.d(TAG, "✅ Service scope iptal edildi")

        // Ana thread'de toast göster
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(this@ClipboardService, "⏹️ Clipboard servisi durduruldu", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        Log.d(TAG, "📋 Clipboard değişikliği algılandı")

        try {
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val clipText = clipData.getItemAt(0).text?.toString()
                val source = clipData.description.label?.toString()

                // Uygulama içi kopyalamaları kontrol et
                if (source == "Clipbo") {
                    Log.d(TAG, "Uygulama içi kopyalama, yeni kayıt oluşturulmayacak")
                    return@OnPrimaryClipChangedListener
                }

                if (!clipText.isNullOrBlank() && clipText != lastClipboardContent) {
                    lastClipboardContent = clipText
                    Log.d(TAG, "Yeni clipboard içeriği: ${clipText.take(50)}...")

                    // Arka planda veritabanına kaydet
                    serviceScope.launch {
                        try {
                            saveClipboardItem(clipText)
                            Log.d(TAG, "✅ Clipboard başarıyla kaydedildi")
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Clipboard kaydedilirken hata oluştu: ${e.message}", e)
                        }
                    }
                } else {
                    Log.d(TAG, "Clipboard içeriği boş veya son içerikle aynı")
                }
            } else {
                Log.d(TAG, "ClipData null veya boş")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Clipboard dinleme sırasında hata: ${e.message}", e)
        }
    }

    private suspend fun saveClipboardItem(content: String) {
        try {
            Log.d(TAG, "💾 Veritabanına kaydediliyor: ${content.take(30)}...")

            val clipboardType = detectContentType(content)
            val isSecureContent = isSecureContent(content)

            val clipboardEntity = ClipboardEntity(
                content = content,
                timestamp = System.currentTimeMillis(),
                type = clipboardType,
                isPinned = false,
                isSecure = isSecureContent,
                tags = "",
                preview = content.take(100)
            )

            val insertedId = clipboardDao.insertItem(clipboardEntity)
            Log.d(TAG, "✅ Veritabanına kaydedildi! ID: $insertedId, Tip: $clipboardType")

            // Eski kayıtları temizle (son 100 kaydı tut)
            val itemCount = clipboardDao.getItemCount()
            if (itemCount > 100) {
                clipboardDao.keepOnlyLatest(100)
                Log.d(TAG, "🧹 Eski kayıtlar temizlendi (toplam: $itemCount)")
            }

            // Widget'ları güncelle
            updateAllWidgets()

            // Ana thread'de başarı mesajı
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@ClipboardService, "✅ Kaydedildi!", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Veritabanı kaydetme hatası: ${e.message}", e)
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@ClipboardService, "❌ Kayıt hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun detectContentType(content: String): String {
        return when {
            Patterns.WEB_URL.matcher(content).matches() -> "URL"
            Patterns.EMAIL_ADDRESS.matcher(content).matches() -> "EMAIL"
            Patterns.PHONE.matcher(content).matches() -> "PHONE"
            content.matches(Regex("^TR\\d{2}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{2}$")) -> "IBAN"
            content.length <= 4 && content.all { it.isDigit() } -> "PIN"
            isPasswordLike(content) -> "PASSWORD"
            content.contains("\n") && content.split("\n").size >= 3 -> "ADDRESS"
            else -> "TEXT"
        }
    }

    private fun isPasswordLike(content: String): Boolean {
        // Gelişmiş parola algılama
        if (content.length < 6 || content.length > 50) return false

        val hasUpper = content.any { it.isUpperCase() }
        val hasLower = content.any { it.isLowerCase() }
        val hasDigit = content.any { it.isDigit() }
        val hasSpecial = content.any { !it.isLetterOrDigit() }

        // En az 3 kriter karşılanmalı
        val criteriaCount = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }

        return criteriaCount >= 3 ||
                (criteriaCount >= 2 && content.length >= 8) ||
                content.contains("password", ignoreCase = true) ||
                content.contains("parola", ignoreCase = true) ||
                content.contains("şifre", ignoreCase = true)
    }

    private fun isSecureContent(content: String): Boolean {
        val type = detectContentType(content)

        // Hassas kelimeler
        val sensitiveKeywords = listOf(
            "password", "parola", "şifre", "pass", "pin", "kod",
            "iban", "hesap", "kart", "cvv", "cvc", "otp", "token",
            "secret", "gizli", "private", "özel"
        )

        val contentLower = content.lowercase()
        val containsSensitiveKeyword = sensitiveKeywords.any {
            contentLower.contains(it)
        }

        return type in listOf("PASSWORD", "PIN", "IBAN") ||
                containsSensitiveKeyword ||
                (content.length in 4..6 && content.all { it.isDigit() }) // Kısa PIN'ler
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName("Clipboard Servisi")
            .setDescription("Clipboard geçmişi yakalama servisi")
            .build()

        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("Clipbo Aktif 📋")
        .setContentText("Clipboard geçmişi kaydediliyor...")
        .setSmallIcon(android.R.drawable.ic_menu_edit) // Geçici icon
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    // Widget ile ilgili yardımcı methodlar
    private fun updateWidgetServiceStatus(isRunning: Boolean) {
        try {
            val widgetRepository = WidgetRepository.getInstance()
            widgetRepository.updateServiceStatus(isRunning)
            Log.d(TAG, "✅ Widget servis durumu güncellendi: $isRunning")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ Widget repository bulunamadı: ${e.message}")

            // Fallback: SharedPreferences kullan
            val prefs = getSharedPreferences("clipbo_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("service_running", isRunning).apply()
            Log.d(TAG, "✅ Fallback ile servis durumu güncellendi: $isRunning")
        }
    }

    private fun updateAllWidgets() {
        // Ana thread'de widget güncellemesi yap
        CoroutineScope(Dispatchers.Main).launch {
            try {
                WidgetUtils.updateAllWidgets(this@ClipboardService)
                Log.d(TAG, "✅ Widget'lar güncellendi")
            } catch (e: Exception) {
                Log.w(TAG, "⚠️ Widget güncelleme hatası: ${e.message}")
            }
        }
    }
}