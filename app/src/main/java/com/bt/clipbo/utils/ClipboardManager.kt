package com.bt.clipbo.utils

import android.content.ClipData
import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.bt.clipbo.domain.model.ClipboardItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.ClipboardManager as AndroidClipboardManager

@Singleton
class ClipboardManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val androidClipboardManager: AndroidClipboardManager by lazy {
            context.getSystemService(Context.CLIPBOARD_SERVICE) as AndroidClipboardManager
        }

        /**
         * Copy content to system clipboard
         */
        suspend fun copyToClipboard(
            content: String,
            label: String = "Clipbo",
        ): Result<Unit> =
            withContext(Dispatchers.Main) {
                try {
                    val clipData = ClipData.newPlainText(label, content)
                    androidClipboardManager.setPrimaryClip(clipData)
                    Result.success(Unit)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Get current clipboard content
         */
        suspend fun getCurrentClipboardContent(): Result<String?> =
            withContext(Dispatchers.Main) {
                try {
                    val clipData = androidClipboardManager.primaryClip
                    val content =
                        if (clipData != null && clipData.itemCount > 0) {
                            clipData.getItemAt(0).text?.toString()
                        } else {
                            null
                        }
                    Result.success(content)
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }

        /**
         * Check if clipboard has content
         */
        fun hasClipboardContent(): Boolean {
            return try {
                val clipData = androidClipboardManager.primaryClip
                clipData != null && clipData.itemCount > 0 &&
                    !clipData.getItemAt(0).text.isNullOrBlank()
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Detect content type from clipboard text
         */
        fun detectContentType(content: String): ClipboardItem.ClipboardType {
            if (content.isBlank()) return ClipboardItem.ClipboardType.TEXT

            return when {
                // URL detection
                Patterns.WEB_URL.matcher(content).matches() ->
                    ClipboardItem.ClipboardType.URL

                // Email detection
                Patterns.EMAIL_ADDRESS.matcher(content).matches() ->
                    ClipboardItem.ClipboardType.EMAIL

                // Phone detection
                Patterns.PHONE.matcher(content).matches() ->
                    ClipboardItem.ClipboardType.PHONE

                // IBAN detection (Turkish IBAN format)
                content.matches(Regex("^TR\\d{2}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{4}\\s?\\d{2}$")) ->
                    ClipboardItem.ClipboardType.IBAN

                // PIN detection (4-6 digits)
                content.matches(Regex("^\\d{4,6}$")) ->
                    ClipboardItem.ClipboardType.PIN

                // Password detection (heuristic)
                isPasswordLike(content) ->
                    ClipboardItem.ClipboardType.PASSWORD

                // Address detection (multiple lines)
                content.contains("\n") && content.split("\n").size >= 3 ->
                    ClipboardItem.ClipboardType.ADDRESS

                // Default to text
                else -> ClipboardItem.ClipboardType.TEXT
            }
        }

        /**
         * Check if content looks like a password
         */
        private fun isPasswordLike(content: String): Boolean {
            if (content.length < 6 || content.length > 50) return false

            val hasUpper = content.any { it.isUpperCase() }
            val hasLower = content.any { it.isLowerCase() }
            val hasDigit = content.any { it.isDigit() }
            val hasSpecial = content.any { !it.isLetterOrDigit() }

            // Check for password-like criteria
            val criteriaCount = listOf(hasUpper, hasLower, hasDigit, hasSpecial).count { it }

            return criteriaCount >= 3 ||
                (criteriaCount >= 2 && content.length >= 8) ||
                content.contains("password", ignoreCase = true) ||
                content.contains("parola", ignoreCase = true) ||
                content.contains("şifre", ignoreCase = true)
        }

        /**
         * Check if content should be treated as secure
         */
        fun isSecureContent(
            content: String,
            type: ClipboardItem.ClipboardType,
        ): Boolean {
            val sensitiveKeywords =
                listOf(
                    "password", "parola", "şifre", "pass", "pin", "kod",
                    "iban", "hesap", "kart", "cvv", "cvc", "otp", "token",
                    "secret", "gizli", "private", "özel",
                )

            val contentLower = content.lowercase()
            val containsSensitiveKeyword =
                sensitiveKeywords.any {
                    contentLower.contains(it)
                }

            return type in
                listOf(
                    ClipboardItem.ClipboardType.PASSWORD,
                    ClipboardItem.ClipboardType.PIN,
                    ClipboardItem.ClipboardType.IBAN,
                ) || containsSensitiveKeyword
        }

        /**
         * Generate preview text for clipboard item
         */
        fun generatePreview(
            content: String,
            maxLength: Int = 100,
        ): String {
            return if (content.length <= maxLength) {
                content
            } else {
                content.take(maxLength - 3) + "..."
            }
        }

        /**
         * Add clipboard change listener
         */
        fun addClipboardListener(listener: AndroidClipboardManager.OnPrimaryClipChangedListener) {
            try {
                androidClipboardManager.addPrimaryClipChangedListener(listener)
            } catch (e: Exception) {
                // Handle exception silently
            }
        }

        /**
         * Remove clipboard change listener
         */
        fun removeClipboardListener(listener: AndroidClipboardManager.OnPrimaryClipChangedListener) {
            try {
                androidClipboardManager.removePrimaryClipChangedListener(listener)
            } catch (e: Exception) {
                // Handle exception silently
            }
        }
    }

@Composable
fun rememberClipboardManager(): ClipboardManager {
    val context = LocalContext.current
    return remember { ClipboardManager(context) }
}
