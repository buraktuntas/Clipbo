package com.bt.clipbo.di

import android.content.Context
import com.bt.clipbo.data.database.*
import com.bt.clipbo.data.repository.ClipboardRepository
import com.bt.clipbo.data.repository.TagRepository
import com.bt.clipbo.utils.BiometricHelper
import com.bt.clipbo.widget.repository.WidgetRepository
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
     * Thread-safe database instance - Production ready
     */
    @Provides
    @Singleton
    fun provideClipboardDatabase(
        @ApplicationContext context: Context
    ): ClipboardDatabase {
        return ClipboardDatabase.buildDatabase(context)
    }

    /**
     * DAO Providers - Thread-safe injection
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
     * Widget Repository - DÜZELT: Simple singleton with auto-initialization
     */
    @Provides
    @Singleton
    fun provideWidgetRepository(
        @ApplicationContext context: Context,
        clipboardDao: ClipboardDao
    ): WidgetRepository {
        val repository = WidgetRepository(context, clipboardDao)
        // Repository will self-initialize in its init block
        return repository
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