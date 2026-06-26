package com.diary.ai.di

import android.content.Context
import com.diary.ai.data.ai.GeminiClient
import com.diary.ai.data.local.NoteDatabase
import com.diary.ai.data.repository.AuthRepositoryImpl
import com.diary.ai.data.repository.DiaryRepositoryImpl
import com.diary.ai.data.sync.SyncSchedulerImpl
import com.diary.ai.domain.repository.AuthRepository
import com.diary.ai.domain.repository.DiaryRepository
import com.diary.ai.domain.repository.SyncScheduler
import com.diary.ai.domain.usecase.*

interface AppContainer {
    val diaryRepository: DiaryRepository
    val authRepository: AuthRepository
    val syncScheduler: SyncScheduler
    
    val getNotesByDateUseCase: GetNotesByDateUseCase
    val saveNoteUseCase: SaveNoteUseCase
    val generateDailySummaryUseCase: GenerateDailySummaryUseCase
    val performSemanticSearchUseCase: PerformSemanticSearchUseCase
    
    val getActiveUserUseCase: GetActiveUserUseCase
    val signInUseCase: SignInUseCase
    val signOutUseCase: SignOutUseCase
}

class AppContainerImpl(private val context: Context) : AppContainer {

    private val database: NoteDatabase by lazy {
        NoteDatabase.getDatabase(context)
    }

    private val geminiClient: GeminiClient by lazy {
        GeminiClient(apiKey = "MOCK_GEMINI_KEY")
    }

    override val diaryRepository: DiaryRepository by lazy {
        DiaryRepositoryImpl(database.noteDao(), geminiClient)
    }

    override val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl()
    }

    override val syncScheduler: SyncScheduler by lazy {
        SyncSchedulerImpl(context)
    }

    override val getNotesByDateUseCase: GetNotesByDateUseCase by lazy {
        GetNotesByDateUseCase(diaryRepository)
    }

    override val saveNoteUseCase: SaveNoteUseCase by lazy {
        SaveNoteUseCase(diaryRepository, syncScheduler)
    }

    override val generateDailySummaryUseCase: GenerateDailySummaryUseCase by lazy {
        GenerateDailySummaryUseCase(diaryRepository)
    }

    override val performSemanticSearchUseCase: PerformSemanticSearchUseCase by lazy {
        PerformSemanticSearchUseCase(diaryRepository)
    }

    override val getActiveUserUseCase: GetActiveUserUseCase by lazy {
        GetActiveUserUseCase(authRepository)
    }

    override val signInUseCase: SignInUseCase by lazy {
        SignInUseCase(authRepository)
    }

    override val signOutUseCase: SignOutUseCase by lazy {
        SignOutUseCase(authRepository)
    }
}
