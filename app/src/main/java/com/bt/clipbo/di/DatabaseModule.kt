package com.bt.clipbo.di

import android.content.Context
import com.bt.clipbo.data.database.ClipboardDao
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.data.database.TagDao
import com.bt.clipbo.data.database.UserPreferenceDao
import com.bt.clipbo.data.database.UsageAnalyticsDao
import com.bt.clipbo.data.repository.ClipboardRepository
import com.bt.clipbo.data.repository.TagRepository
import com.bt.clipbo.utils.BiometricHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Database instance - Hilt best practices
     * Static factory method yerine doğrudan Room.databaseBuilder kullanımı
     */
    @Provides
    @Singleton
    fun provideClipboardDatabase(
        @ApplicationContext context: Context
    ): ClipboardDatabase {
        return ClipboardDatabase.buildDatabase(context)
    }

    /**
     * DAO Providers - Database instance'dan türetilir
     */
    @Provides
    fun provideClipboardDao(database: ClipboardDatabase): ClipboardDao {
        return database.clipboardDao()
    }

    @Provides
    fun provideTagDao(database: ClipboardDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    fun provideUserPreferenceDao(database: ClipboardDatabase): UserPreferenceDao {
        return database.userPreferenceDao()
    }

    @Provides
    fun provideUsageAnalyticsDao(database: ClipboardDatabase): UsageAnalyticsDao {
        return database.usageAnalyticsDao()
    }

    /**
     * Repository Providers - Clean Architecture
     * Repository'ler artık doğrudan DAO'lardan inject edilir
     */
    @Provides
    @Singleton
    fun provideClipboardRepository(clipboardDao: ClipboardDao): ClipboardRepository {
        return ClipboardRepository(clipboardDao)
    }

    @Provides
    @Singleton
    fun provideTagRepository(tagDao: TagDao): TagRepository {
        return TagRepository(tagDao)
    }

    /**
     * Utility Providers
     */
    @Provides
    @Singleton
    fun provideBiometricHelper(): BiometricHelper {
        return BiometricHelper()
    }
}