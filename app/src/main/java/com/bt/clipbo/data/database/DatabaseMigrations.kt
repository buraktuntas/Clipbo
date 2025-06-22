package com.bt.clipbo.data.database

import android.annotation.SuppressLint
import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    private const val TAG = "DatabaseMigrations"

    val MIGRATION_1_2 =
        object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.d(TAG, "Starting migration 1 -> 2")

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
                        """.trimIndent(),
                    )

                    // Clipboard items tablosuna tags kolonu ekleme
                    addColumnIfNotExists(database, "clipboard_items", "tags", "TEXT NOT NULL DEFAULT ''")

                    Log.d(TAG, "Migration 1 -> 2 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 1 -> 2 failed", e)
                    throw e
                }
            }
        }

    val MIGRATION_2_3 =
        object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.d(TAG, "Starting migration 2 -> 3")

                    // Encryption support için kolunlar ekleme
                    addColumnIfNotExists(database, "clipboard_items", "encrypted_content", "TEXT")
                    addColumnIfNotExists(database, "clipboard_items", "is_encrypted", "INTEGER NOT NULL DEFAULT 0")

                    // Performance için index'ler ekleme
                    createIndexIfNotExists(database, "index_clipboard_items_timestamp", "clipboard_items", "timestamp")
                    createIndexIfNotExists(database, "index_clipboard_items_type", "clipboard_items", "type")
                    createIndexIfNotExists(database, "index_clipboard_items_is_pinned", "clipboard_items", "is_pinned")
                    createIndexIfNotExists(database, "index_clipboard_items_is_secure", "clipboard_items", "is_secure")

                    Log.d(TAG, "Migration 2 -> 3 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 2 -> 3 failed", e)
                    throw e
                }
            }
        }

    val MIGRATION_3_4 =
        object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.d(TAG, "Starting migration 3 -> 4")

                    // Clipboard tablosuna yeni alanlar
                    addColumnIfNotExists(database, "clipboard_items", "sync_id", "TEXT")
                    addColumnIfNotExists(database, "clipboard_items", "last_modified", "INTEGER NOT NULL DEFAULT 0")
                    addColumnIfNotExists(database, "clipboard_items", "is_deleted", "INTEGER NOT NULL DEFAULT 0")

                    // User preferences tablosunu kontrol et ve oluştur
                    createUserPreferencesTableIfNotExists(database)

                    Log.d(TAG, "Migration 3 -> 4 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 3 -> 4 failed", e)
                    throw e
                }
            }
        }

    val MIGRATION_4_5 =
        object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    Log.d(TAG, "Starting migration 4 -> 5")

                    // Analytics tablosu oluştur
                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `usage_analytics` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `event_type` TEXT NOT NULL,
                            `event_data` TEXT,
                            `timestamp` INTEGER NOT NULL,
                            `session_id` TEXT NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Favorites kategorisi
                    addColumnIfNotExists(database, "clipboard_items", "is_favorite", "INTEGER NOT NULL DEFAULT 0")
                    createIndexIfNotExists(database, "index_clipboard_items_is_favorite", "clipboard_items", "is_favorite")

                    // Eğer user_preferences tablosu varsa updated_at kolunu ekle
                    if (tableExists(database, "user_preferences")) {
                        addColumnIfNotExists(database, "user_preferences", "updated_at", "INTEGER NOT NULL DEFAULT 0")
                    }

                    Log.d(TAG, "Migration 4 -> 5 completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 4 -> 5 failed", e)
                    throw e
                }
            }
        }

    // Tüm migration'ları bir arada
    val ALL_MIGRATIONS =
        arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
        )

    /**
     * Tablo var mı kontrol et
     */
    private fun tableExists(
        database: SupportSQLiteDatabase,
        tableName: String,
    ): Boolean {
        return try {
            val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'")
            val exists = cursor.moveToFirst()
            cursor.close()
            Log.d(TAG, "Table $tableName exists: $exists")
            exists
        } catch (e: Exception) {
            Log.e(TAG, "Error checking table existence for $tableName", e)
            false
        }
    }

    /**
     * Güvenli kolon ekleme - Kolon varsa ekleme yapmaz
     */
    @SuppressLint("Range")
    private fun addColumnIfNotExists(
        database: SupportSQLiteDatabase,
        tableName: String,
        columnName: String,
        columnDefinition: String,
    ) {
        try {
            // Önce tablo var mı kontrol et
            if (!tableExists(database, tableName)) {
                Log.w(TAG, "Table $tableName does not exist, skipping column addition")
                return
            }

            // Kolon var mı kontrol et
            val cursor = database.query("PRAGMA table_info($tableName)")
            var columnExists = false

            while (cursor.moveToNext()) {
                val existingColumnName = cursor.getString(cursor.getColumnIndex("name"))
                if (existingColumnName == columnName) {
                    columnExists = true
                    break
                }
            }
            cursor.close()

            if (!columnExists) {
                database.execSQL("ALTER TABLE $tableName ADD COLUMN $columnName $columnDefinition")
                Log.d(TAG, "Column $columnName added to $tableName")
            } else {
                Log.d(TAG, "Column $columnName already exists in $tableName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add column $columnName to $tableName", e)
            throw e
        }
    }

    /**
     * Güvenli index oluşturma - Index varsa oluşturmaz
     */
    private fun createIndexIfNotExists(
        database: SupportSQLiteDatabase,
        indexName: String,
        tableName: String,
        columnName: String,
    ) {
        try {
            // Index var mı kontrol et
            val cursor = database.query("SELECT name FROM sqlite_master WHERE type='index' AND name='$indexName'")
            val indexExists = cursor.moveToFirst()
            cursor.close()

            if (!indexExists) {
                database.execSQL("CREATE INDEX IF NOT EXISTS `$indexName` ON `$tableName` (`$columnName`)")
                Log.d(TAG, "Index $indexName created on $tableName($columnName)")
            } else {
                Log.d(TAG, "Index $indexName already exists")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create index $indexName", e)
            throw e
        }
    }

    /**
     * User preferences tablosunu güvenli şekilde oluştur - FIXED VERSION
     */
    private fun createUserPreferencesTableIfNotExists(database: SupportSQLiteDatabase) {
        try {
            if (!tableExists(database, "user_preferences")) {
                // Tablo yoksa doğru schema ile oluştur
                database.execSQL(
                    """
                    CREATE TABLE `user_preferences` (
                        `key` TEXT PRIMARY KEY NOT NULL,
                        `value` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `updated_at` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent(),
                )

                Log.d(TAG, "user_preferences table created with correct schema")

                // Default değerleri ekle - bu sefer doğru schema ile
                insertDefaultPreferencesForMigration(database)
            } else {
                Log.d(TAG, "user_preferences table already exists")
                // Tablo varsa updated_at kolonu var mı kontrol et
                addColumnIfNotExists(database, "user_preferences", "updated_at", "INTEGER NOT NULL DEFAULT 0")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create user_preferences table", e)
            throw e
        }
    }

    /**
     * Migration sırasında default preferences ekle - FIXED
     */
    private fun insertDefaultPreferencesForMigration(database: SupportSQLiteDatabase) {
        try {
            val timestamp = System.currentTimeMillis()
            val defaultPrefs =
                mapOf(
                    "max_history_items" to "100",
                    "enable_secure_mode" to "true",
                    "auto_start_service" to "true",
                    "dark_theme" to "false",
                    "backup_enabled" to "false",
                    "analytics_enabled" to "true",
                )

            defaultPrefs.forEach { (key, value) ->
                try {
                    // Migration sırasında kesinlikle updated_at kolonu var
                    database.execSQL(
                        """
                        INSERT OR IGNORE INTO user_preferences (key, value, type, updated_at) 
                        VALUES (?, ?, 'STRING', ?)
                        """.trimIndent(),
                        arrayOf(key, value, timestamp),
                    )

                    Log.d(TAG, "Migration: Inserted preference $key = $value")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration: Failed to insert preference $key", e)
                    // Individual preference failure shouldn't stop migration
                }
            }

            Log.d(TAG, "Migration: Default preferences inserted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Migration: Failed to insert default preferences", e)
            // Don't throw - not critical for migration
        }
    }

    /**
     * Migration testi
     */
    fun testMigration(
        database: SupportSQLiteDatabase,
        fromVersion: Int,
        toVersion: Int,
    ): Boolean {
        return try {
            Log.d(TAG, "Testing migration from $fromVersion to $toVersion")

            when (fromVersion to toVersion) {
                1 to 2 -> MIGRATION_1_2.migrate(database)
                2 to 3 -> MIGRATION_2_3.migrate(database)
                3 to 4 -> MIGRATION_3_4.migrate(database)
                4 to 5 -> MIGRATION_4_5.migrate(database)
                else -> {
                    Log.w(TAG, "No migration test available for $fromVersion -> $toVersion")
                    return false
                }
            }

            Log.d(TAG, "Migration test successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Migration test failed", e)
            false
        }
    }

    /**
     * Emergency database reset
     */
    fun resetDatabase(database: SupportSQLiteDatabase) {
        try {
            Log.w(TAG, "Performing emergency database reset")

            // Get all table names
            val cursor = database.query("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")
            val tableNames = mutableListOf<String>()

            while (cursor.moveToNext()) {
                tableNames.add(cursor.getString(0))
            }
            cursor.close()

            // Drop all tables
            tableNames.forEach { tableName ->
                database.execSQL("DROP TABLE IF EXISTS `$tableName`")
                Log.d(TAG, "Dropped table: $tableName")
            }

            Log.d(TAG, "Database reset completed")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to reset database", e)
        }
    }
}
