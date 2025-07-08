package com.bt.clipbo

import android.app.Application
import android.util.Log
import com.bt.clipbo.widget.repository.WidgetRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClipboApplication : Application() {

    companion object {
        private const val TAG = "ClipboApplication"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "🚀 Clipbo Application starting...")

        try {
            // Initialize timber for logging in debug builds
            if (BuildConfig.DEBUG) {
                timber.log.Timber.plant(timber.log.Timber.DebugTree())
                Log.d(TAG, "🌲 Timber logging initialized")
            }

            Log.d(TAG, "🎉 Clipbo Application started successfully")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize application", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "🛑 Clipbo Application terminating...")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "⚠️ Low memory warning received")
    }
}