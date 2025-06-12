package com.bt.clipbo

import android.app.Application
import com.bt.clipbo.data.database.ClipboardDatabase
import com.bt.clipbo.widget.repository.WidgetRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClipboApplication : Application() {

    @Inject
    lateinit var widgetRepository: WidgetRepository

    override fun onCreate() {
        super.onCreate()

        // Widget repository'yi initialize et
        try {
            WidgetRepository.initialize(widgetRepository)
        } catch (e: Exception) {
            // Fallback initialization
            val database = ClipboardDatabase.getDatabase(this)
            val fallbackRepository = WidgetRepository(this, database.clipboardDao())
            WidgetRepository.initialize(fallbackRepository)
        }
    }
}