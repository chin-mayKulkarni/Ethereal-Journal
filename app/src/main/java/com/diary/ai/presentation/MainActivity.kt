package com.diary.ai.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.diary.ai.DiaryApplication
import com.diary.ai.data.ai.GeminiClient
import com.diary.ai.data.repository.DiaryRepositoryImpl
import com.diary.ai.data.sync.SyncSchedulerImpl
import com.diary.ai.domain.usecase.GenerateDailySummaryUseCase
import com.diary.ai.domain.usecase.GetNotesByDateUseCase
import com.diary.ai.domain.usecase.PerformSemanticSearchUseCase
import com.diary.ai.domain.usecase.SaveNoteUseCase
import com.diary.ai.presentation.dashboard.DashboardScreen
import com.diary.ai.presentation.dashboard.DiaryViewModel
import com.diary.ai.presentation.dashboard.DiaryUserIntent


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Dependency Injection Setup (Manual service construction)
        val app = application as DiaryApplication
        val database = app.database
        val noteDao = database.noteDao()

        // Setup Gemini flash client helper
        val geminiClient = GeminiClient(apiKey = "MOCK_GEMINI_KEY")

        val repository = DiaryRepositoryImpl(noteDao, geminiClient)
        val syncScheduler = SyncSchedulerImpl(this)

        val getNotesByDateUseCase = GetNotesByDateUseCase(repository)
        val saveNoteUseCase = SaveNoteUseCase(repository, syncScheduler)
        val generateDailySummaryUseCase = GenerateDailySummaryUseCase(repository)
        val performSemanticSearchUseCase = PerformSemanticSearchUseCase(repository)

        val viewModel = DiaryViewModel(
            getNotesByDateUseCase,
            saveNoteUseCase,
            generateDailySummaryUseCase,
            performSemanticSearchUseCase
        )

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state = viewModel.viewState.collectAsState()
                    val user = state.value.user
                    if (user == null) {
                        com.diary.ai.presentation.auth.OnboardingScreen(
                            onSignIn = { signedInUser ->
                                viewModel.processIntent(DiaryUserIntent.SignIn(signedInUser))
                            }
                        )
                    } else {
                        DashboardScreen(
                            state = state.value,
                            onIntent = { intent -> viewModel.processIntent(intent) }
                        )
                    }
                }
            }
        }
    }
}
