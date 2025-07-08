package com.bt.clipbo.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ClipboardEntity::class,
        TagEntity::class,
        UserPreferenceEntity::class,
        UsageAnalyticsEntity::class,
    ],
    version = 6,
    exportSchema = true, // Production için true yapıldı
    autoMigrations = [
        // Mümkün olan otomatik migration'ları buraya ekle
    ],
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao
    abstract fun tagDao(): TagDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun usageAnalyticsDao(): UsageAnalyticsDao

    companion object {
        private const val TAG = "ClipboardDatabase"
        private const val DATABASE_NAME = "clipbo_database"

        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        // Manuel migration'lar - Production için kritik
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating database from version 1 to 2")
                try {
                    // Tags tablosu ekleme
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `tags` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `name` TEXT NOT NULL,
                            `color` TEXT NOT NULL,
                            `usage_count` INTEGER NOT NULL DEFAULT 0,
                            `created_at` INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
                    )

                    // Clipboard items tablosuna tags kolonu ekleme
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN tags TEXT NOT NULL DEFAULT ''")

                    Log.d(TAG, "Migration 1->2 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 1->2 failed", e)
                    throw e
                }
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating database from version 2 to 3")
                try {
                    // Encryption support
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN encrypted_content TEXT")
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN is_encrypted INTEGER NOT NULL DEFAULT 0")

                    // Performance indexes
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_clipboard_items_timestamp ON clipboard_items(timestamp)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_clipboard_items_type ON clipboard_items(type)")

                    Log.d(TAG, "Migration 2->3 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 2->3 failed", e)
                    throw e
                }
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating database from version 3 to 4")
                try {
                    // User preferences tablosu
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `user_preferences` (
                            `key` TEXT PRIMARY KEY NOT NULL,
                            `value` TEXT NOT NULL,
                            `type` TEXT NOT NULL,
                            `updated_at` INTEGER NOT NULL DEFAULT 0
                        )
                        """.trimIndent()
                    )

                    // Sync fields
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN sync_id TEXT")
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN last_modified INTEGER NOT NULL DEFAULT 0")

                    Log.d(TAG, "Migration 3->4 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 3->4 failed", e)
                    throw e
                }
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating database from version 4 to 5")
                try {
                    // Analytics tablosu
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `usage_analytics` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `event_type` TEXT NOT NULL,
                            `event_data` TEXT,
                            `timestamp` INTEGER NOT NULL,
                            `session_id` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )

                    // Favorites
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")

                    Log.d(TAG, "Migration 4->5 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 4->5 failed", e)
                    throw e
                }
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                Log.d(TAG, "Migrating database from version 5 to 6")
                try {
                    // Preview kolonu ekleme
                    database.execSQL("ALTER TABLE clipboard_items ADD COLUMN preview TEXT NOT NULL DEFAULT ''")

                    // Existing data için preview değerlerini güncelle
                    database.execSQL("UPDATE clipboard_items SET preview = SUBSTR(content, 1, 100) WHERE preview = ''")

                    // Additional indexes for performance
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_clipboard_items_is_pinned ON clipboard_items(is_pinned)")
                    database.execSQL("CREATE INDEX IF NOT EXISTS index_clipboard_items_is_secure ON clipboard_items(is_secure)")

                    Log.d(TAG, "Migration 5->6 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 5->6 failed", e)
                    throw e
                }
            }
        }

        /**
         * Bu method artık kullanılmayacak - Hilt tarafından yönetilecek
         * @deprecated Use Hilt injection instead
         */
        @Deprecated("Use Hilt injection instead")
        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        /**
         * Database builder method - Hilt module tarafından kullanılacak
         */
        internal fun buildDatabase(context: Context): ClipboardDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                ClipboardDatabase::class.java,
                DATABASE_NAME
            )
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4,
                    MIGRATION_4_5,
                    MIGRATION_5_6
                )
                .enableMultiInstanceInvalidation()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(TAG, "Database created - inserting default preferences")
                        insertDefaultPreferences(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d(TAG, "Database opened")
                        ensureDefaultPreferences(db)
                    }
                })
                .build()
        }

        private fun insertDefaultPreferences(db: SupportSQLiteDatabase) {
            try {
                val defaultPrefs = mapOf(
                    "max_history_items" to "100",
                    "enable_secure_mode" to "true",
                    "auto_start_service" to "true",
                    "dark_theme" to "false",
                    "backup_enabled" to "false",
                    "analytics_enabled" to "true"
                )

                val timestamp = System.currentTimeMillis()
                defaultPrefs.forEach { (key, value) ->
                    db.execSQL(
                        "INSERT OR IGNORE INTO user_preferences (key, value, type, updated_at) VALUES (?, ?, ?, ?)",
                        arrayOf(key, value, "STRING", timestamp)
                    )
                }
                Log.d(TAG, "Default preferences inserted")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert default preferences", e)
            }
        }

        private fun ensureDefaultPreferences(db: SupportSQLiteDatabase) {
            try {
                val cursor = db.query("SELECT COUNT(*) FROM user_preferences")
                val hasData = if (cursor.moveToFirst()) cursor.getInt(0) > 0 else false
                cursor.close()

                if (!hasData) {
                    insertDefaultPreferences(db)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking preferences", e)
            }
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}

// Type Converters - Değişiklik yok
class Converters {
    @androidx.room.TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @androidx.room.TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }
}

// FIXED Entity - updated_at kolonu kesinlikle var
@androidx.room.Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @androidx.room.PrimaryKey
    val key: String,
    val value: String,
    val type: String,
    @androidx.room.ColumnInfo(name = "updated_at", defaultValue = "0")
    val updatedAt: Long = System.currentTimeMillis(),
)

@androidx.room.Entity(tableName = "usage_analytics")
data class UsageAnalyticsEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @androidx.room.ColumnInfo(name = "event_type")
    val eventType: String,
    @androidx.room.ColumnInfo(name = "event_data")
    val eventData: String? = null,
    val timestamp: Long,
    @androidx.room.ColumnInfo(name = "session_id")
    val sessionId: String,
)

// DAO'lar - FIXED: key keyword sorunu
@androidx.room.Dao
interface UserPreferenceDao {
    @androidx.room.Query("SELECT * FROM user_preferences WHERE `key` = :prefKey")
    suspend fun getPreference(prefKey: String): UserPreferenceEntity?

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: UserPreferenceEntity)

    @androidx.room.Query("DELETE FROM user_preferences WHERE `key` = :prefKey")
    suspend fun deletePreference(prefKey: String)

    @androidx.room.Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): kotlinx.coroutines.flow.Flow<List<UserPreferenceEntity>>

    @androidx.room.Query("SELECT * FROM user_preferences WHERE `key` IN (:keys)")
    suspend fun getPreferences(keys: List<String>): List<UserPreferenceEntity>

    @androidx.room.Query("UPDATE user_preferences SET value = :value, updated_at = :timestamp WHERE `key` = :prefKey")
    suspend fun updatePreference(
        prefKey: String,
        value: String,
        timestamp: Long = System.currentTimeMillis(),
    )
}

@androidx.room.Dao
interface UsageAnalyticsDao {
    @androidx.room.Insert
    suspend fun insertEvent(event: UsageAnalyticsEntity)

    @androidx.room.Query("SELECT * FROM usage_analytics WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getEventsAfter(startTime: Long): List<UsageAnalyticsEntity>

    @androidx.room.Query("DELETE FROM usage_analytics WHERE timestamp < :cutoffTime")
    suspend fun deleteOldEvents(cutoffTime: Long)

    @androidx.room.Query("SELECT COUNT(*) FROM usage_analytics WHERE event_type = :eventType AND timestamp >= :startTime")
    suspend fun getEventCount(
        eventType: String,
        startTime: Long,
    ): Int

    @androidx.room.Query("DELETE FROM usage_analytics")
    suspend fun clearAllAnalytics()
}
