package com.bt.clipbo.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardEntity
import com.bt.clipbo.data.database.TagDao
import com.bt.clipbo.data.database.TagEntity
import com.bt.clipbo.presentation.ui.secure.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val version: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val appVersion: String,
    val deviceInfo: String,
    val clipboardItems: List<BackupClipboardItem>,
    val tags: List<BackupTag>,
    val preferences: Map<String, String> = emptyMap(),
    val metadata: BackupMetadata,
)

@Serializable
data class BackupClipboardItem(
    val id: Long,
    val content: String,
    val timestamp: Long,
    val type: String,
    val isPinned: Boolean,
    val isSecure: Boolean,
    val tags: String,
    val preview: String,
    val isEncrypted: Boolean = false,
    val originalLength: Int = 0,
)

@Serializable
data class BackupTag(
    val id: Long,
    val name: String,
    val color: String,
    val usageCount: Int,
    val createdAt: Long,
)

@Serializable
data class BackupMetadata(
    val totalItems: Int,
    val secureItems: Int,
    val pinnedItems: Int,
    val totalTags: Int,
    val backupSize: Long,
    val deviceModel: String,
    val androidVersion: String,
    val appVersionCode: Int,
    val checksum: String,
    val language: String = "tr",
)

data class BackupProgress(
    val stage: BackupStage,
    val progress: Int, // 0-100
    val currentItem: Int = 0,
    val totalItems: Int = 0,
    val message: String,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val estimatedTimeLeft: Long = 0L, // milliseconds
)

enum class BackupStage(val displayName: String) {
    PREPARING("Hazırlanıyor..."),
    READING_DATA("Veriler okunuyor..."),
    PROCESSING_ITEMS("Öğeler işleniyor..."),
    ENCRYPTING("Şifreleniyor..."),
    COMPRESSING("Sıkıştırılıyor..."),
    WRITING_FILE("Dosya yazılıyor..."),
    COMPLETED("Tamamlandı!"),
    RESTORING("Geri yükleniyor..."),
    VALIDATING("Doğrulanıyor..."),
    DECRYPTING("Şifre çözülüyor..."),
    WRITING_DATABASE("Veritabanına yazılıyor..."),
    CLEANUP("Temizleniyor..."),
}

data class BackupInfo(
    val fileName: String,
    val filePath: String,
    val size: Long,
    val createdAt: Long,
    val itemCount: Int,
    val tagCount: Int,
    val isEncrypted: Boolean,
    val appVersion: String,
    val deviceInfo: String,
)

@Singleton
class BackupRestoreManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val clipboardDao: ClipboardDao,
        private val tagDao: TagDao,
        private val encryptionManager: EncryptionManager,
        private val errorHandler: ErrorHandler,
    ) {
        companion object {
            private const val BACKUP_VERSION = 2
            private const val BACKUP_FILE_EXTENSION = ".clipbo"
            private const val TAG = "BackupRestore"
            private const val MAX_AUTO_BACKUPS = 5
            private const val BACKUP_BUFFER_SIZE = 8192
        }

        private val _backupProgress =
            MutableStateFlow(
                BackupProgress(BackupStage.PREPARING, 0, message = "Hazırlanıyor..."),
            )
        val backupProgress: StateFlow<BackupProgress> = _backupProgress.asStateFlow()

        private val json =
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            }

        private var startTime = 0L

        /**
         * Verileri dosyaya dışa aktar
         */
        suspend fun exportToFile(
            uri: Uri,
            includeSecureItems: Boolean = true,
            encryptBackup: Boolean = false,
        ): Result<String> =
            withContext(Dispatchers.IO) {
                startTime = System.currentTimeMillis()

                try {
                    updateProgress(BackupStage.PREPARING, 0, "Yedekleme hazırlanıyor...")

                    // Veritabanından verileri oku
                    updateProgress(BackupStage.READING_DATA, 10, "Veriler okunuyor...")
                    val clipboardItems = clipboardDao.getAllItems().first()
                    val tags = tagDao.getAllTags().first()

                    val filteredItems =
                        if (includeSecureItems) {
                            clipboardItems
                        } else {
                            clipboardItems.filter { !it.isSecure }
                        }

                    updateProgress(
                        BackupStage.PROCESSING_ITEMS,
                        20,
                        "Öğeler işleniyor...",
                        currentItem = 0,
                        totalItems = filteredItems.size,
                    )

                    // Backup formatına dönüştür
                    val backupItems =
                        filteredItems.mapIndexed { index, entity ->
                            updateProgress(
                                BackupStage.PROCESSING_ITEMS,
                                20 + (index * 30 / filteredItems.size),
                                "Öğe işleniyor: ${index + 1}/${filteredItems.size}",
                                currentItem = index + 1,
                                totalItems = filteredItems.size,
                            )

                            val contentToStore =
                                if (entity.isSecure && encryptBackup) {
                                    encryptionManager.encrypt(entity.content) ?: entity.content
                                } else {
                                    entity.content
                                }

                            BackupClipboardItem(
                                id = entity.id,
                                content = contentToStore,
                                timestamp = entity.timestamp,
                                type = entity.type,
                                isPinned = entity.isPinned,
                                isSecure = entity.isSecure,
                                tags = entity.tags,
                                preview = entity.preview,
                                isEncrypted = entity.isSecure && encryptBackup,
                                originalLength = entity.content.length,
                            )
                        }

                    updateProgress(BackupStage.PROCESSING_ITEMS, 50, "Etiketler işleniyor...")

                    val backupTags =
                        tags.map { tag ->
                            BackupTag(
                                id = tag.id,
                                name = tag.name,
                                color = tag.color,
                                usageCount = tag.usageCount,
                                createdAt = tag.createdAt,
                            )
                        }

                    // Metadata oluştur
                    updateProgress(BackupStage.ENCRYPTING, 60, "Metadata oluşturuluyor...")

                    val metadata =
                        BackupMetadata(
                            totalItems = backupItems.size,
                            secureItems = backupItems.count { it.isSecure },
                            pinnedItems = backupItems.count { it.isPinned },
                            totalTags = backupTags.size,
                            backupSize = 0L, // Sonra hesaplanacak
                            deviceModel = android.os.Build.MODEL,
                            androidVersion = android.os.Build.VERSION.RELEASE,
                            appVersionCode = getAppVersionCode(),
                            checksum = generateChecksum(backupItems, backupTags),
                            language = getCurrentLanguage(),
                        )

                    // Backup data oluştur
                    val backupData =
                        BackupData(
                            appVersion = getAppVersion(),
                            deviceInfo = getDeviceInfo(),
                            clipboardItems = backupItems,
                            tags = backupTags,
                            preferences = getAppPreferences(),
                            metadata = metadata,
                        )

                    // JSON'a dönüştür
                    updateProgress(BackupStage.COMPRESSING, 70, "JSON oluşturuluyor...")
                    val jsonString = json.encodeToString(backupData)

                    // Dosyaya yaz
                    updateProgress(BackupStage.WRITING_FILE, 80, "Dosya yazılıyor...")
                    var totalBytes = 0L

                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)
                        totalBytes = jsonBytes.size.toLong()

                        outputStream.write(jsonBytes)
                        outputStream.flush()
                    } ?: throw Exception("Dosya yazılamadı")

                    // Metadata'yı güncellenmiş boyutla tekrar oluştur
                    val finalMetadata = metadata.copy(backupSize = totalBytes)
                    val finalBackupData = backupData.copy(metadata = finalMetadata)
                    val finalJsonString = json.encodeToString(finalBackupData)

                    // Son halini tekrar yaz
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(finalJsonString.toByteArray(Charsets.UTF_8))
                        outputStream.flush()
                    }

                    updateProgress(BackupStage.COMPLETED, 100, "Yedekleme tamamlandı!", isCompleted = true)

                    val fileName = generateBackupFileName()
                    val duration = System.currentTimeMillis() - startTime

                    Log.d(TAG, "Backup completed: $fileName, Duration: ${duration}ms, Size: ${totalBytes}b")

                    Result.success(
                        "Yedekleme başarıyla tamamlandı!\n\nDosya: $fileName\nBoyut: ${formatFileSize(
                            totalBytes,
                        )}\nSüre: ${formatDuration(duration)}\nÖğe: ${backupItems.size}\nEtiket: ${backupTags.size}",
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Backup failed", e)
                    val error = errorHandler.handleError(e)
                    updateProgress(BackupStage.COMPLETED, 0, "Hata: ${error.userMessage}", false, error.userMessage)
                    Result.failure(e)
                }
            }

        /**
         * Dosyadan verileri içe aktar
         */
        suspend fun importFromFile(
            uri: Uri,
            overwriteExisting: Boolean = false,
            restoreSecureItems: Boolean = true,
        ): Result<String> =
            withContext(Dispatchers.IO) {
                startTime = System.currentTimeMillis()

                try {
                    updateProgress(BackupStage.RESTORING, 0, "Geri yükleme başlıyor...")

                    // Dosyayı oku
                    updateProgress(BackupStage.READING_DATA, 10, "Dosya okunuyor...")
                    val jsonString =
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.readBytes().toString(Charsets.UTF_8)
                        } ?: throw Exception("Dosya okunamadı")

                    // JSON'ı parse et
                    updateProgress(BackupStage.VALIDATING, 20, "Veriler çözümleniyor...")
                    val backupData = json.decodeFromString<BackupData>(jsonString)

                    // Backup doğrulama
                    validateBackup(backupData)

                    updateProgress(BackupStage.VALIDATING, 30, "Veri bütünlüğü kontrol ediliyor...")

                    // Checksum doğrulama
                    val calculatedChecksum =
                        generateChecksum(
                            backupData.clipboardItems,
                            backupData.tags,
                        )

                    if (calculatedChecksum != backupData.metadata.checksum) {
                        throw Exception("Yedek dosyası bozulmuş olabilir (checksum uyumsuzluğu)")
                    }

                    // Mevcut verileri temizle (isteğe bağlı)
                    if (overwriteExisting) {
                        updateProgress(BackupStage.CLEANUP, 40, "Mevcut veriler temizleniyor...")
                        // Sadece sabitlenmemiş öğeleri sil
                        clipboardDao.deleteAllUnpinned()
                    }

                    // Etiketleri geri yükle
                    updateProgress(BackupStage.WRITING_DATABASE, 50, "Etiketler geri yükleniyor...")
                    var restoredTags = 0

                    backupData.tags.forEach { backupTag ->
                        try {
                            val tagEntity =
                                TagEntity(
                                    name = backupTag.name,
                                    color = backupTag.color,
                                    usageCount = backupTag.usageCount,
                                    createdAt = backupTag.createdAt,
                                )
                            tagDao.insertTag(tagEntity)
                            restoredTags++
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to restore tag: ${backupTag.name}", e)
                        }
                    }

                    // Clipboard öğelerini geri yükle
                    updateProgress(BackupStage.WRITING_DATABASE, 60, "Clipboard öğeleri geri yükleniyor...")
                    var restoredItems = 0
                    val totalItems = backupData.clipboardItems.size

                    backupData.clipboardItems.forEachIndexed { index, backupItem ->
                        updateProgress(
                            BackupStage.WRITING_DATABASE,
                            60 + (index * 30 / totalItems),
                            "Öğe geri yükleniyor: ${index + 1}/$totalItems",
                            currentItem = index + 1,
                            totalItems = totalItems,
                        )

                        try {
                            // Güvenli öğeleri geri yükleme kontrolü
                            if (backupItem.isSecure && !restoreSecureItems) {
                                return@forEachIndexed
                            }

                            val content =
                                if (backupItem.isEncrypted) {
                                    updateProgress(
                                        BackupStage.DECRYPTING,
                                        60 + (index * 30 / totalItems),
                                        "Şifre çözülüyor: ${index + 1}/$totalItems",
                                    )
                                    encryptionManager.decrypt(backupItem.content) ?: backupItem.content
                                } else {
                                    backupItem.content
                                }

                            val clipboardEntity =
                                ClipboardEntity(
                                    content = content,
                                    timestamp = backupItem.timestamp,
                                    type = backupItem.type,
                                    isPinned = backupItem.isPinned,
                                    isSecure = backupItem.isSecure,
                                    tags = backupItem.tags,
                                    preview = backupItem.preview,
                                )

                            clipboardDao.insertItem(clipboardEntity)
                            restoredItems++
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to restore item ${backupItem.id}", e)
                        }
                    }

                    updateProgress(BackupStage.COMPLETED, 100, "Geri yükleme tamamlandı!", isCompleted = true)

                    val duration = System.currentTimeMillis() - startTime
                    Log.d(TAG, "Restore completed: Items=$restoredItems, Tags=$restoredTags, Duration=${duration}ms")

                    Result.success(
                        "Geri yükleme başarıyla tamamlandı!\n\nÖğe: $restoredItems/$totalItems\nEtiket: $restoredTags/${backupData.tags.size}\nSüre: ${formatDuration(
                            duration,
                        )}",
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Restore failed", e)
                    val error = errorHandler.handleError(e)
                    updateProgress(BackupStage.COMPLETED, 0, "Hata: ${error.userMessage}", false, error.userMessage)
                    Result.failure(e)
                }
            }

        /**
         * Otomatik yedek oluştur
         */
        suspend fun createAutoBackup(): Result<File> =
            withContext(Dispatchers.IO) {
                try {
                    val backupDir =
                        File(context.filesDir, "backups").apply {
                            if (!exists()) mkdirs()
                        }

                    val backupFile = File(backupDir, generateBackupFileName())

                    // Verileri al
                    val clipboardItems = clipboardDao.getAllItems().first()
                    val tags = tagDao.getAllTags().first()

                    // Backup data oluştur
                    val backupItems =
                        clipboardItems.map { entity ->
                            BackupClipboardItem(
                                id = entity.id,
                                content =
                                    if (entity.isSecure) {
                                        encryptionManager.encrypt(entity.content) ?: entity.content
                                    } else {
                                        entity.content
                                    },
                                timestamp = entity.timestamp,
                                type = entity.type,
                                isPinned = entity.isPinned,
                                isSecure = entity.isSecure,
                                tags = entity.tags,
                                preview = entity.preview,
                                isEncrypted = entity.isSecure,
                                originalLength = entity.content.length,
                            )
                        }

                    val backupTags =
                        tags.map { tag ->
                            BackupTag(
                                id = tag.id,
                                name = tag.name,
                                color = tag.color,
                                usageCount = tag.usageCount,
                                createdAt = tag.createdAt,
                            )
                        }

                    val metadata =
                        BackupMetadata(
                            totalItems = backupItems.size,
                            secureItems = backupItems.count { it.isSecure },
                            pinnedItems = backupItems.count { it.isPinned },
                            totalTags = backupTags.size,
                            backupSize = 0L,
                            deviceModel = android.os.Build.MODEL,
                            androidVersion = android.os.Build.VERSION.RELEASE,
                            appVersionCode = getAppVersionCode(),
                            checksum = generateChecksum(backupItems, backupTags),
                            language = getCurrentLanguage(),
                        )

                    val backupData =
                        BackupData(
                            appVersion = getAppVersion(),
                            deviceInfo = getDeviceInfo(),
                            clipboardItems = backupItems,
                            tags = backupTags,
                            preferences = getAppPreferences(),
                            metadata = metadata,
                        )

                    val jsonString = json.encodeToString(backupData)

                    FileOutputStream(backupFile).use { outputStream ->
                        outputStream.write(jsonString.toByteArray(Charsets.UTF_8))
                    }

                    // Eski otomatik yedekleri temizle
                    cleanOldAutoBackups(backupDir)

                    Log.d(TAG, "Auto backup created: ${backupFile.name}")
                    Result.success(backupFile)
                } catch (e: Exception) {
                    Log.e(TAG, "Auto backup failed", e)
                    errorHandler.handleError(e)
                    Result.failure(e)
                }
            }

        /**
         * Yedek dosyası bilgilerini al
         */
        suspend fun getBackupInfo(uri: Uri): Result<BackupInfo> =
            withContext(Dispatchers.IO) {
                try {
                    val jsonString =
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            inputStream.readBytes().toString(Charsets.UTF_8)
                        } ?: throw Exception("Dosya okunamadı")

                    val backupData = json.decodeFromString<BackupData>(jsonString)

                    val backupInfo =
                        BackupInfo(
                            fileName = getFileNameFromUri(uri),
                            filePath = uri.toString(),
                            size = jsonString.toByteArray().size.toLong(),
                            createdAt = backupData.createdAt,
                            itemCount = backupData.metadata.totalItems,
                            tagCount = backupData.metadata.totalTags,
                            isEncrypted = backupData.clipboardItems.any { it.isEncrypted },
                            appVersion = backupData.appVersion,
                            deviceInfo = backupData.deviceInfo,
                        )

                    Result.success(backupInfo)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get backup info", e)
                    errorHandler.handleError(e)
                    Result.failure(e)
                }
            }

        /**
         * Yerel yedek dosyalarını listele
         */
        suspend fun getLocalBackups(): List<BackupInfo> =
            withContext(Dispatchers.IO) {
                try {
                    val backupDir = File(context.filesDir, "backups")
                    if (!backupDir.exists()) return@withContext emptyList()

                    val backupFiles =
                        backupDir.listFiles { file ->
                            file.name.endsWith(BACKUP_FILE_EXTENSION)
                        }?.sortedByDescending { it.lastModified() } ?: return@withContext emptyList()

                    backupFiles.mapNotNull { file ->
                        try {
                            val jsonString = file.readText(Charsets.UTF_8)
                            val backupData = json.decodeFromString<BackupData>(jsonString)

                            BackupInfo(
                                fileName = file.name,
                                filePath = file.absolutePath,
                                size = file.length(),
                                createdAt = backupData.createdAt,
                                itemCount = backupData.metadata.totalItems,
                                tagCount = backupData.metadata.totalTags,
                                isEncrypted = backupData.clipboardItems.any { it.isEncrypted },
                                appVersion = backupData.appVersion,
                                deviceInfo = backupData.deviceInfo,
                            )
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse backup file: ${file.name}", e)
                            null
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to list local backups", e)
                    emptyList()
                }
            }

        // Private helper methods
        private fun updateProgress(
            stage: BackupStage,
            progress: Int,
            message: String,
            isCompleted: Boolean = false,
            error: String? = null,
            currentItem: Int = 0,
            totalItems: Int = 0,
        ) {
            val estimatedTimeLeft =
                if (progress > 0 && !isCompleted) {
                    val elapsed = System.currentTimeMillis() - startTime
                    val remaining = (elapsed * (100 - progress)) / progress
                    remaining
                } else {
                    0L
                }

            _backupProgress.value =
                BackupProgress(
                    stage = stage,
                    progress = progress,
                    currentItem = currentItem,
                    totalItems = totalItems,
                    message = message,
                    isCompleted = isCompleted,
                    error = error,
                    estimatedTimeLeft = estimatedTimeLeft,
                )
        }

        private fun validateBackup(backupData: BackupData) {
            if (backupData.version > BACKUP_VERSION) {
                throw Exception("Bu yedek dosyası daha yeni bir uygulama sürümünde oluşturulmuş (v${backupData.version})")
            }

            if (backupData.clipboardItems.isEmpty() && backupData.tags.isEmpty()) {
                throw Exception("Yedek dosyası boş")
            }

            if (backupData.metadata.totalItems != backupData.clipboardItems.size) {
                throw Exception("Yedek dosyası tutarsız (metadata uyumsuzluğu)")
            }
        }

        private fun generateChecksum(
            items: List<BackupClipboardItem>,
            tags: List<BackupTag>,
        ): String {
            val content = "${items.size}_${tags.size}_${items.sumOf { it.content.hashCode() }}_${tags.sumOf { it.name.hashCode() }}"
            return content.hashCode().toString()
        }

        private fun generateBackupFileName(): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            return "clipbo_backup_${dateFormat.format(Date())}$BACKUP_FILE_EXTENSION"
        }

        private fun getAppVersion(): String {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionName ?: "1.0.0"
            } catch (e: Exception) {
                "1.0.0"
            }
        }

        private fun getAppVersionCode(): Int {
            return try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                packageInfo.versionCode
            } catch (e: Exception) {
                1
            }
        }

        private fun getDeviceInfo(): String {
            return "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})"
        }

        private fun getCurrentLanguage(): String {
            return Locale.getDefault().language
        }

        private fun getAppPreferences(): Map<String, String> {
            // Uygulama tercihlerini al (implementasyon gerekirse)
            return emptyMap()
        }

        private fun cleanOldAutoBackups(backupDir: File) {
            try {
                val backupFiles =
                    backupDir.listFiles { file ->
                        file.name.endsWith(BACKUP_FILE_EXTENSION)
                    }?.sortedByDescending { it.lastModified() }

                if (backupFiles != null && backupFiles.size > MAX_AUTO_BACKUPS) {
                    backupFiles.drop(MAX_AUTO_BACKUPS).forEach { file ->
                        if (file.delete()) {
                            Log.d(TAG, "Deleted old backup: ${file.name}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to clean old backups", e)
            }
        }

        private fun getFileNameFromUri(uri: Uri): String {
            return uri.lastPathSegment ?: "unknown_backup$BACKUP_FILE_EXTENSION"
        }

        private fun formatFileSize(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                else -> "${bytes / (1024 * 1024)} MB"
            }
        }

        private fun formatDuration(milliseconds: Long): String {
            val seconds = milliseconds / 1000
            return when {
                seconds < 60 -> "${seconds}s"
                seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
                else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
            }
        }
    }
