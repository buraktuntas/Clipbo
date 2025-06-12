package com.bt.clipbo.data.service

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.bt.clipbo.data.service.ClipboardService
import kotlinx.coroutines.*

class ClipboAccessibilityService : AccessibilityService() {

    private lateinit var clipboardManager: ClipboardManager
    private var serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var lastClipboardContent = ""

    companion object {
        private const val TAG = "ClipboAccessibility"
        private var instance: ClipboAccessibilityService? = null

        fun isRunning(): Boolean = instance != null

        fun requestPermission(context: Context) {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        Log.d(TAG, "♿ Accessibility Service connected")

        clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Clipboard listener ekle
        clipboardManager.addPrimaryClipChangedListener(clipboardListener)

        // İlk clipboard içeriğini al
        val initialClip = clipboardManager.primaryClip
        if (initialClip != null && initialClip.itemCount > 0) {
            val initialText = initialClip.getItemAt(0).text?.toString()
            if (!initialText.isNullOrEmpty()) {
                lastClipboardContent = initialText
                Log.d(TAG, "📋 İlk clipboard içeriği alındı")
            }
        }

        // Normal clipboard service'i de başlat
        ClipboardService.startService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null

        try {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener)
        } catch (e: Exception) {
            Log.e(TAG, "Clipboard listener kaldırma hatası: ${e.message}")
        }

        serviceScope.cancel()
        Log.d(TAG, "♿ Accessibility Service destroyed")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Bu method'u clipboard monitoring için kullanmıyoruz
        // Sadece required olduğu için implement ediyoruz
    }

    override fun onInterrupt() {
        Log.d(TAG, "♿ Accessibility Service interrupted")
    }

    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        Log.d(TAG, "📋 Clipboard değişikliği algılandı (Accessibility)")

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
                    Log.d(TAG, "Yeni clipboard içeriği: ${clipText.take(30)}...")

                    // Clipboard service'e bildir
                    serviceScope.launch {
                        try {
                            val intent = Intent(this@ClipboAccessibilityService, ClipboardService::class.java)
                            intent.putExtra("clipboard_content", clipText)
                            intent.putExtra("source", "accessibility")
                            startService(intent)
                        } catch (e: Exception) {
                            Log.e(TAG, "Clipboard service'e bildirim hatası: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Accessibility clipboard dinleme hatası: ${e.message}", e)
        }
    }
}