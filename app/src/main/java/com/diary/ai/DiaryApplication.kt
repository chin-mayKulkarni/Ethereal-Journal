package com.diary.ai

import android.app.Application
import com.diary.ai.data.local.NoteDatabase

class DiaryApplication : Application() {
    
    val database: NoteDatabase by lazy { NoteDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
