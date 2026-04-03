package com.app.douyin.pro

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DouyinApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
