package com.bt.clipbo.data.database
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [ClipboardEntity::class, TagEntity::class],
    version = 2, // Versiyon artırıldı
    exportSchema = false
)
abstract class ClipboardDatabase : RoomDatabase() {

    abstract fun clipboardDao(): ClipboardDao
    abstract fun tagDao(): TagDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}