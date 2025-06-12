package com.bt.clipbo.presentation.ui.secure

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "ClipboSecureKey"
        private const val TRANSFORMATION = "AES/CBC/PKCS7Padding"
        private const val IV_SEPARATOR = ":"
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(false)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }

    fun encrypt(data: String): String? {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())

            val iv = cipher.iv
            val encryptedData = cipher.doFinal(data.toByteArray())

            val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
            val encryptedString = Base64.encodeToString(encryptedData, Base64.DEFAULT)

            "$ivString$IV_SEPARATOR$encryptedString"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decrypt(encryptedData: String): String? {
        return try {
            val parts = encryptedData.split(IV_SEPARATOR)
            if (parts.size != 2) return null

            val iv = Base64.decode(parts[0], Base64.DEFAULT)
            val encrypted = Base64.decode(parts[1], Base64.DEFAULT)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), IvParameterSpec(iv))

            val decryptedData = cipher.doFinal(encrypted)
            String(decryptedData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isSecureContentType(type: String): Boolean {
        return type in listOf("PASSWORD", "PIN", "IBAN", "SECURE")
    }

    fun encryptIfNeeded(content: String, type: String): String {
        return if (isSecureContentType(type)) {
            encrypt(content) ?: content
        } else {
            content
        }
    }

    fun decryptIfNeeded(content: String, type: String): String {
        return if (isSecureContentType(type)) {
            decrypt(content) ?: content
        } else {
            content
        }
    }
}