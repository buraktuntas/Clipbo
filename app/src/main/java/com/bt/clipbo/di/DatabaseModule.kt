package com.bt.clipbo.di

import android.content.Context
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.data.database.ClipboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.bt.clipbo.data.database.TagDao
import com.bt.clipbo.data.repository.TagRepository
import com.bt.clipbo.utils.BiometricHelper


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideClipboardDatabase(
        @ApplicationContext context: Context
    ): ClipboardDatabase {
        return ClipboardDatabase.getDatabase(context)
    }

    @Provides
    fun provideClipboardDao(database: ClipboardDatabase): ClipboardDao {
        return database.clipboardDao()
    }

    @Provides
    fun provideTagDao(database: ClipboardDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideTagRepository(
        tagDao: TagDao
    ): TagRepository {
        return TagRepository(tagDao)
    }

    @Provides
    @Singleton
    fun provideBiometricHelper(): BiometricHelper {
        return BiometricHelper()
    }
}