package com.diary.ai.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.diary.ai.presentation.dashboard.DashboardScreen
import com.diary.ai.presentation.dashboard.DiaryViewModel
import com.diary.ai.presentation.dashboard.DiaryUserIntent


class MainActivity : ComponentActivity() {

    private val viewModel: DiaryViewModel by viewModels { DiaryViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
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
