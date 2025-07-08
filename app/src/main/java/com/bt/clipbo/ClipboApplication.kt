package com.bt.clipbo

import android.app.Application
import android.util.Log
import com.bt.clipbo.widget.repository.WidgetRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClipboApplication : Application() {

    // DÜZELT: Directly inject WidgetRepository instead of initializer
    @Inject
    lateinit var widgetRepository: WidgetRepository

    companion object {
        private const val TAG = "ClipboApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "🚀 Clipbo Application starting...")

        try {
            // DÜZELT: Force widget repository initialization by accessing it
            val isInitialized = ::widgetRepository.isInitialized
            if (isInitialized) {
                // Access the repository to trigger Hilt initialization
                val stats = widgetRepository.getCacheInfo()
                Log.d(TAG, "✅ Widget repository initialized: $stats")
            }

            // Initialize timber for logging in debug builds
            if (BuildConfig.DEBUG) {
                timber.log.Timber.plant(timber.log.Timber.DebugTree())
                Log.d(TAG, "🌲 Timber logging initialized")
            }

            Log.d(TAG, "🎉 Clipbo Application started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize application", e)
            // Application can continue without widget functionality
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "🛑 Clipbo Application terminating...")

        try {
            // Cleanup widget repository
            if (::widgetRepository.isInitialized) {
                widgetRepository.clearCache()
                WidgetRepository.cleanup()
                Log.d(TAG, "🧹 Widget repository cleaned up")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error during cleanup", e)
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "⚠️ Low memory warning received")

        try {
            if (::widgetRepository.isInitialized) {
                widgetRepository.clearCache()
                Log.d(TAG, "🧹 Widget cache cleared due to low memory")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling low memory", e)
        }
    }
}