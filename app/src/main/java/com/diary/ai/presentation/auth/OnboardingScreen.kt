package com.diary.ai.presentation.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.diary.ai.presentation.dashboard.User
import coil.compose.AsyncImage
import kotlinx.coroutines.delay


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onSignIn: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    val taglines = listOf("Write your mind.", "Speak your day.", "Scan your journal.")
    var taglineIndex by remember { mutableStateOf(0) }
    var showAccountSelector by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3200)
            taglineIndex = (taglineIndex + 1) % taglines.size
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAF8F5),
                        Color(0xFFF0EBE1)
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        // Glowing background blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopStart)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(Color(0x0C6366F1), shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 100.dp, y = 100.dp)
                .background(Color(0x08EC4899), shape = CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Branding Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vibrant Palette Icon Box
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFFA855F7),
                                    Color(0xFFEC4899)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Journal Icon",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = buildAnnotatedString {
                        append("SoulJournal")
                        pushStyle(SpanStyle(color = Color(0xFFEC4899)))
                        append(".")
                        pop()
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tagline Switcher
                AnimatedContent(
                    targetState = taglines[taglineIndex],
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() with
                                slideOutVertically { height -> -height } + fadeOut()
                    },
                    label = "TaglineAnimation"
                ) { tagline ->
                    Text(
                        text = tagline.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // Sophisticated Tabletop Journal Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10f)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE2E8F0).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1CsSZm7r6iRef0gbQlJetD4zf-dY4dC_Znklv_ysIEux-tsUljg_2ilKrf8N3P3786CaUbnu0azf-sy6m8obfQkuG-_H0_rVe-f7WeQpwb_qXOFDV1hhWPaGz2Nc-oC9tF-hL0kjB3juTHpzR6qu8xP4JgCsOmZs74AmNNNHqjVRPJjvq2yQl66j4ZKT0Xw4SonZgHbk0L72lok6u8Jns7g-0FauTRXtYepQxz3TB1Q5PbVSYNjb1tWAE1czXD5w8Z2UXlEZb2aJU",
                    contentDescription = "Tabletop with journal and fountain pen",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Bottom gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x66312E81) // indigo-900/30
                                )
                            )
                        )
                )
            }


            // Authentication options
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 160.dp),

                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Sign In with Google Button
                Button(
                    onClick = { showAccountSelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(28.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    ),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sign in with Google",
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                }

                Text(
                    text = "Your data is securely locked to your Google ID and stored in your personal cloud repository.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Footer version
            Text(
                text = "SOULJOURNAL V1.0.4",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8).copy(alpha = 0.6f),
                letterSpacing = 1.5.sp
            )
        }
    }

    // Google Account Selector Dialog Mock
    if (showAccountSelector) {
        Dialog(onDismissRequest = { showAccountSelector = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GoogleLogoIcon(modifier = Modifier.size(32.dp))
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Sign in with Google",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "to continue to Ethereal Journal",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    Divider(color = Color(0xFFF1F5F9))

                    // Accounts list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Account 1: Chinmay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    showAccountSelector = false
                                    onSignIn(
                                        User(
                                            name = "Chinmay",
                                            email = "chinmayrk01@gmail.com",
                                            avatar = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1CsSZm7r6iRef0gbQlJetD4zf-dY4dC_Znklv_ysIEux-tsUljg_2ilKrf8N3P3786CaUbnu0azf-sy6m8obfQkuG-_H0_rVe-f7WeQpwb_qXOFDV1hhWPaGz2Nc-oC9tF-hL0jcRx8I06lvfu7_6pJG27ZT3EB10JXu61rhK5tFYoImKj37jzzTTxYQCXbh5uK1q8vbo_2aewsdGt8A39k6doLBRw2X_5IwsT2TyuzpnkItgi4VLngYOv3uK4-x-acJhtuYSygLEzMCMOQkkFulgs9K-C-TuFxG"
                                        )
                                    )
                                }
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD1CsSZm7r6iRef0gbQlJetD4zf-dY4dC_Znklv_ysIEux-tsUljg_2ilKrf8N3P3786CaUbnu0azf-sy6m8obfQkuG-_H0_rVe-f7WeQpwb_qXOFDV1hhWPaGz2Nc-oC9tF-hL0jcRx8I06lvfu7_6pJG27ZT3EB10JXu61rhK5tFYoImKj37jzzTTxYQCXbh5uK1q8vbo_2aewsdGt8A39k6doLBRw2X_5IwsT2TyuzpnkItgi4VLngYOv3uK4-x-acJhtuYSygLEzMCMOQkkFulgs9K-C-TuFxG",
                                contentDescription = "Chinmay Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Chinmay",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "chinmayrk01@gmail.com",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        // Account 2: Guest Mindful Journaler
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    showAccountSelector = false
                                    onSignIn(
                                        User(
                                            name = "Mindful Journaler",
                                            email = "guest@ethereal.journal",
                                            avatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=150&h=150"
                                        )
                                    )
                                }
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=150&h=150",
                                contentDescription = "Mindful Journaler Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Mindful Journaler",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                                Text(
                                    text = "guest@ethereal.journal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                    }

                    TextButton(
                        onClick = { showAccountSelector = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    // Custom drawing representative Google G logo in pure Canvas/Paths or standard Text represent
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            color = Color(0xFF4285F4),
            textAlign = TextAlign.Center
        )
    }
}
