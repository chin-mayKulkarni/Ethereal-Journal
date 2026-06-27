package com.diary.ai.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.diary.ai.presentation.auth.SplashScreen
import com.diary.ai.presentation.dashboard.DashboardScreen
import com.diary.ai.presentation.dashboard.DiaryViewModel
import com.diary.ai.presentation.dashboard.DiaryUserIntent


class MainActivity : ComponentActivity() {

    private val viewModel: DiaryViewModel by viewModels { DiaryViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Hide status bar and navigation bar by default, showing them on swipe
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    
                    if (showSplash) {
                        SplashScreen(
                            onTimeout = { showSplash = false }
                        )
                    } else {
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
}
