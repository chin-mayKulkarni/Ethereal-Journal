package com.diary.ai.presentation.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Timeout behavior to transition to main app flow
    LaunchedEffect(Unit) {
        delay(3500)
        onTimeout()
    }

    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "SplashAnimations")
    
    // Shimmer effect for status text
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    // Loading progress bar movement
    val progressShift by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "ProgressShift"
    )

    // Subtle scale pulsing for the background glowing spots
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E1A)), // #0a0e1a background
        contentAlignment = Alignment.Center
    ) {
        // Atmospheric Background Glow Elements (Ambient Light Spills)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.4f)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .blur(80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x147D3FCF), // primary/5
                            Color.Transparent
                        )
                    )
                )
        )

        // Pulsing background ambient light
        Box(
            modifier = Modifier
                .size(380.dp)
                .align(Alignment.Center)
                .blur(100.dp)
                .graphicsLayer {
                    scaleX = glowPulse
                    scaleY = glowPulse
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x0C7DD3FC), // #7dd3fc primary tint 5%
                            Color.Transparent
                        )
                    )
                )
        )

        // Main Splash Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Glassmorphism Icon card (Rotated 3 degrees)
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0x990F1524) // 0.6 opacity of #0f1524
                ),
                border = BorderStroke(1.dp, Color(0x267DD3FC)), // 15% opacity of #7dd3fc
                modifier = Modifier
                    .size(128.dp)
                    .rotate(3f)
                    .padding(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = "Soul Journal Book Icon",
                        tint = Color(0xFF7DD3FC), // Primary color #7dd3fc
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Brand Name Title
            Text(
                text = "SOULJOURNAL",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp,
                color = Color(0xFFE0E8F0) // #e0e8f0
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Divider Line
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(Color(0x4D7DD3FC)) // #7dd3fc with 30% opacity
            )
        }

        // Footer Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            // Shimmering Status Message
            val textBrush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFA0B4C4),
                    Color(0xFFE0E8F0),
                    Color(0xFFA0B4C4)
                ),
                start = Offset(shimmerTranslate, 0f),
                end = Offset(shimmerTranslate + 200f, 0f)
            )

            Text(
                text = "Initializing your digital sanctuary...",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp,
                style = androidx.compose.ui.text.TextStyle(brush = textBrush)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Loading Bar Container
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(2.dp)
                    .clip(CircleShape)
                    .background(Color(0x1A7DD3FC)) // rgba(125, 211, 252, 0.1)
            ) {
                // Moving loading progress bar
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.4f)
                        .offset(x = (120 * progressShift).dp)
                        .background(Color(0xFF7DD3FC)) // #7dd3fc
                )
            }
        }
    }
}
