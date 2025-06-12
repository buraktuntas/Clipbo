package com.bt.clipbo.di

import com.bt.clipbo.data.repository.ClipboardRepository
import com.bt.clipbo.data.database.ClipboardDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideClipboardRepository(
        clipboardDao: ClipboardDao
    ): ClipboardRepository {
        return ClipboardRepository(clipboardDao)
    }
}