package com.bt.clipbo.data.database

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Tags tablosu ekleme
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `tags` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `color` TEXT NOT NULL,
                    `usage_count` INTEGER NOT NULL DEFAULT 0,
                    `created_at` INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent())

            // Clipboard items tablosuna tags kolonu ekleme
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Encryption support için encrypted_content kolonu ekleme
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN encrypted_content TEXT")
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN is_encrypted INTEGER NOT NULL DEFAULT 0")

            // Performance için index'ler ekleme
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_clipboard_items_timestamp` ON `clipboard_items` (`timestamp`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_clipboard_items_type` ON `clipboard_items` (`type`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_clipboard_items_is_pinned` ON `clipboard_items` (`is_pinned`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_clipboard_items_is_secure` ON `clipboard_items` (`is_secure`)")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Clipboard tablosuna yeni alanlar
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN sync_id TEXT")
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN last_modified INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN is_deleted INTEGER NOT NULL DEFAULT 0")

            // user_preferences tablosu zaten varsa updated_at olmayabilir
            try {
                database.execSQL("ALTER TABLE user_preferences ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
            } catch (e: Exception) {
                Log.d("MIGRATION_3_4", "Migration error: $e")
            }
        }
    }


    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Analytics ve usage tracking
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS `usage_analytics` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `event_type` TEXT NOT NULL,
                    `event_data` TEXT,
                    `timestamp` INTEGER NOT NULL,
                    `session_id` TEXT NOT NULL
                )
            """.trimIndent())

            // Favorites kategorisi
            database.execSQL("ALTER TABLE clipboard_items ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_clipboard_items_is_favorite` ON `clipboard_items` (`is_favorite`)")
        }
    }

    // Tüm migration'ları bir arada
    val ALL_MIGRATIONS = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
        MIGRATION_3_4,
        MIGRATION_4_5
    )
}