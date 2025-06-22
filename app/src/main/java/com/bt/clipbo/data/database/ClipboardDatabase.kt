package com.bt.clipbo.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ClipboardEntity::class,
        TagEntity::class,
        UserPreferenceEntity::class,
        UsageAnalyticsEntity::class,
    ],
    version = 6, // VERSION ARTTIRILDI - RESET İÇİN
    exportSchema = false, // Geliştirme aşamasında false
    autoMigrations = [],
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {
    abstract fun clipboardDao(): ClipboardDao

    abstract fun tagDao(): TagDao

    abstract fun userPreferenceDao(): UserPreferenceDao

    abstract fun usageAnalyticsDao(): UsageAnalyticsDao

    companion object {
        private const val TAG = "ClipboardDatabase"

        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext,
                        ClipboardDatabase::class.java,
                        "clipbo_database",
                    )
                        .fallbackToDestructiveMigration() // EMERGENCY FIX: Destructive migration
                        .enableMultiInstanceInvalidation()
                        .addCallback(
                            object : RoomDatabase.Callback() {
                                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                    super.onCreate(db)
                                    Log.d(TAG, "Database onCreate called - inserting defaults")
                                    // Şimdi kesinlikle doğru schema var
                                    insertDefaultPreferencesEmergency(db)
                                }

                                override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                                    super.onOpen(db)
                                    Log.d(TAG, "Database onOpen called")
                                    // Database açıldığında preferences var mı kontrol et
                                    ensureDefaultPreferences(db)
                                }
                            },
                        )
                        .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * EMERGENCY: Basit default preferences ekleme - hiç hata vermesin
         */
        private fun insertDefaultPreferencesEmergency(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            try {
                val defaultPrefs =
                    mapOf(
                        "max_history_items" to "100",
                        "enable_secure_mode" to "true",
                        "auto_start_service" to "true",
                        "dark_theme" to "false",
                        "backup_enabled" to "false",
                        "analytics_enabled" to "true",
                    )

                val timestamp = System.currentTimeMillis()

                defaultPrefs.forEach { (key, value) ->
                    try {
                        // Direkt SQL ile ekle - schema kesinlikle doğru
                        db.execSQL(
                            "INSERT OR IGNORE INTO user_preferences (key, value, type, updated_at) VALUES (?, ?, ?, ?)",
                            arrayOf(key, value, "STRING", timestamp),
                        )
                        Log.d(TAG, "Emergency: Inserted $key")
                    } catch (e: Exception) {
                        Log.e(TAG, "Emergency: Failed to insert $key: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Emergency insertDefaultPreferences failed", e)
            }
        }

        /**
         * Database açıldığında preferences kontrolü
         */
        private fun ensureDefaultPreferences(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            try {
                // Preferences var mı kontrol et
                val cursor = db.query("SELECT COUNT(*) FROM user_preferences")
                val hasData = if (cursor.moveToFirst()) cursor.getInt(0) > 0 else false
                cursor.close()

                if (!hasData) {
                    Log.d(TAG, "No preferences found, inserting defaults")
                    insertDefaultPreferencesEmergency(db)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking preferences", e)
            }
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }

        /**
         * Manuel database reset (geliştirme amaçlı)
         */
        fun resetDatabaseForDevelopment(context: Context) {
            try {
                destroyInstance()
                context.deleteDatabase("clipbo_database")
                context.deleteDatabase("clipbo_database-shm")
                context.deleteDatabase("clipbo_database-wal")
                Log.d(TAG, "Database files deleted for reset")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reset database", e)
            }
        }
    }
}

// Type Converters
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
