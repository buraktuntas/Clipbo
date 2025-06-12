package com.bt.clipbo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ClipboApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }
}