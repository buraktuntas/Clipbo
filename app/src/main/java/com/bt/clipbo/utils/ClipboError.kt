package com.bt.clipbo.utils

import android.content.Context
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

sealed class ClipboError(
    val code: String,
    val userMessage: String,
    val technicalMessage: String,
    val isCritical: Boolean = false,
) {
    // Database Errors
    object DatabaseConnectionError : ClipboError(
        "DB_001",
        "Veritabanı bağlantı hatası",
        "Database connection failed",
    )

    object DatabaseCorruptionError : ClipboError(
        "DB_002",
        "Veritabanı bozuldu, yeniden oluşturuluyor",
        "Database corruption detected",
        true,
    )

    object DatabaseMigrationError : ClipboError(
        "DB_003",
        "Veritabanı güncelleme hatası",
        "Database migration failed",
    )

    // Clipboard Errors
    object ClipboardAccessError : ClipboError(
        "CLIP_001",
        "Clipboard erişim hatası",
        "Clipboard access denied",
    )

    object ClipboardServiceError : ClipboError(
        "CLIP_002",
        "Clipboard servisi başlatılamadı",
        "Clipboard service failed to start",
    )

    // Encryption Errors
    object EncryptionError : ClipboError(
        "ENC_001",
        "Şifreleme hatası",
        "Encryption/decryption failed",
    )

    object KeyStoreError : ClipboError(
        "ENC_002",
        "Güvenlik anahtarı hatası",
        "KeyStore operation failed",
    )

    // Permission Errors
    object PermissionDeniedError : ClipboError(
        "PERM_001",
        "İzin reddedildi",
        "Required permission denied",
    )

    object AccessibilityServiceError : ClipboError(
        "PERM_002",
        "Erişilebilirlik servisi aktif değil",
        "Accessibility service not enabled",
    )

    // Network Errors
    object NetworkError : ClipboError(
        "NET_001",
        "İnternet bağlantısı yok",
        "Network connection failed",
    )

    object TimeoutError : ClipboError(
        "NET_002",
        "İşlem zaman aşımına uğradı",
        "Request timeout",
    )

    // Generic Errors
    class UnknownError(technicalMessage: String) : ClipboError(
        "GEN_001",
        "Bilinmeyen hata oluştu",
        technicalMessage,
    )

    class CustomError(userMessage: String, technicalMessage: String) : ClipboError(
        "CUSTOM",
        userMessage,
        technicalMessage,
    )
}

@Singleton
class ErrorHandler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val _errorFlow = MutableSharedFlow<ClipboError>()
        val errorFlow: SharedFlow<ClipboError> = _errorFlow.asSharedFlow()

        companion object {
            private const val TAG = "ErrorHandler"
        }

        fun handleError(throwable: Throwable): ClipboError {
            val error = mapThrowableToError(throwable)

            // Log the error
            if (error.isCritical) {
                Log.e(TAG, "Critical Error [${error.code}]: ${error.technicalMessage}", throwable)
            } else {
                Log.w(TAG, "Error [${error.code}]: ${error.technicalMessage}", throwable)
            }

            // Emit error to flow
            try {
                _errorFlow.tryEmit(error)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to emit error to flow", e)
            }

            return error
        }

        fun handleError(
            error: ClipboError,
            cause: Throwable? = null,
        ) {
            // Log the error
            if (error.isCritical) {
                Log.e(TAG, "Critical Error [${error.code}]: ${error.technicalMessage}", cause)
            } else {
                Log.w(TAG, "Error [${error.code}]: ${error.technicalMessage}", cause)
            }

            // Emit error to flow
            try {
                _errorFlow.tryEmit(error)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to emit error to flow", e)
            }
        }

        private fun mapThrowableToError(throwable: Throwable): ClipboError {
            return when (throwable) {
                is IOException ->
                    when (throwable) {
                        is ConnectException, is UnknownHostException -> ClipboError.NetworkError
                        is SocketTimeoutException -> ClipboError.TimeoutError
                        else -> ClipboError.UnknownError(throwable.message ?: "IO Exception")
                    }

                is SecurityException -> ClipboError.PermissionDeniedError

                is IllegalStateException ->
                    when {
                        throwable.message?.contains("database", true) == true ->
                            ClipboError.DatabaseConnectionError
                        throwable.message?.contains("keystore", true) == true ->
                            ClipboError.KeyStoreError
                        else -> ClipboError.UnknownError(throwable.message ?: "Illegal State")
                    }

                is RuntimeException ->
                    when {
                        throwable.message?.contains("clipboard", true) == true ->
                            ClipboError.ClipboardAccessError
                        throwable.message?.contains("encryption", true) == true ->
                            ClipboError.EncryptionError
                        else -> ClipboError.UnknownError(throwable.message ?: "Runtime Exception")
                    }

                else -> ClipboError.UnknownError(throwable.message ?: throwable::class.simpleName ?: "Unknown")
            }
        }

        fun clearErrors() {
            // Clear any cached errors if needed
        }
    }

// Composable helper for error handling
@Composable
fun ErrorSnackbarHandler(
    errorHandler: ErrorHandler,
    snackbarHostState: SnackbarHostState,
) {
    LaunchedEffect(errorHandler) {
        errorHandler.errorFlow.collect { error ->
            val duration =
                if (error.isCritical) {
                    SnackbarDuration.Long
                } else {
                    SnackbarDuration.Short
                }

            snackbarHostState.showSnackbar(
                message = error.userMessage,
                duration = duration,
                actionLabel = if (error.isCritical) "Tamam" else null,
            )
        }
    }
}

// Extension functions for easier error handling
suspend fun <T> runCatchingWithHandler(
    errorHandler: ErrorHandler,
    block: suspend () -> T,
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        val error = errorHandler.handleError(e)
        Result.failure(e)
    }
}

fun <T> Result<T>.onErrorHandled(errorHandler: ErrorHandler): Result<T> {
    if (isFailure) {
        exceptionOrNull()?.let { errorHandler.handleError(it) }
    }
    return this
}
