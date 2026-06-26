package com.diary.ai

import android.app.Application
import com.diary.ai.di.AppContainer
import com.diary.ai.di.AppContainerImpl

class DiaryApplication : Application() {
    
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}
