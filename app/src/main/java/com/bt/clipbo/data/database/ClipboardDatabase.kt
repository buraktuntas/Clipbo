package com.bt.clipbo.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context

@Database(
    entities = [
        ClipboardEntity::class,
        TagEntity::class,
        UserPreferenceEntity::class,
        UsageAnalyticsEntity::class
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(Converters::class)
abstract class ClipboardDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun tagDao(): TagDao
    abstract fun userPreferenceDao(): UserPreferenceDao
    abstract fun usageAnalyticsDao(): UsageAnalyticsDao

    companion object {
        @Volatile
        private var INSTANCE: ClipboardDatabase? = null

        fun getDatabase(context: Context): ClipboardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClipboardDatabase::class.java,
                    "clipbo_database"
                )
                    .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                    .fallbackToDestructiveMigration() // Son çare olarak
                    .enableMultiInstanceInvalidation() // Multi-process desteği
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // İlk kurulumda default değerler
                            insertDefaultPreferences(db)
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private fun insertDefaultPreferences(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            val timestamp = System.currentTimeMillis()

            // Default preferences
            val defaultPrefs = mapOf(
                "max_history_items" to "100",
                "enable_secure_mode" to "true",
                "auto_start_service" to "true",
                "dark_theme" to "false",
                "backup_enabled" to "false",
                "analytics_enabled" to "true"
            )

            defaultPrefs.forEach { (key, value) ->
                db.execSQL("""
                    INSERT OR IGNORE INTO user_preferences (key, value, type, updated_at) 
                    VALUES (?, ?, 'STRING', ?)
                """.trimIndent(), arrayOf(key, value, timestamp))
            }
        }

        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
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

// Yeni Entity'ler
@androidx.room.Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @androidx.room.PrimaryKey
    val key: String,
    val value: String,
    val type: String, // STRING, INT, BOOLEAN, LONG
    val updatedAt: Long
)

@androidx.room.Entity(tableName = "usage_analytics")
data class UsageAnalyticsEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventType: String,
    val eventData: String? = null,
    val timestamp: Long,
    val sessionId: String
)

// DAO'lar
@androidx.room.Dao
interface UserPreferenceDao {
    @androidx.room.Query("SELECT * FROM user_preferences WHERE key = :key")
    suspend fun getPreference(key: String): UserPreferenceEntity?

    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun setPreference(preference: UserPreferenceEntity)

    @androidx.room.Query("DELETE FROM user_preferences WHERE key = :key")
    suspend fun deletePreference(key: String)

    @androidx.room.Query("SELECT * FROM user_preferences")
    fun getAllPreferences(): kotlinx.coroutines.flow.Flow<List<UserPreferenceEntity>>
}

@androidx.room.Dao
interface UsageAnalyticsDao {
    @androidx.room.Insert
    suspend fun insertEvent(event: UsageAnalyticsEntity)

    @androidx.room.Query("SELECT * FROM usage_analytics WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getEventsAfter(startTime: Long): List<UsageAnalyticsEntity>

    @androidx.room.Query("DELETE FROM usage_analytics WHERE timestamp < :cutoffTime")
    suspend fun deleteOldEvents(cutoffTime: Long)

    @androidx.room.Query("SELECT COUNT(*) FROM usage_analytics WHERE eventType = :eventType AND timestamp >= :startTime")
    suspend fun getEventCount(eventType: String, startTime: Long): Int
}