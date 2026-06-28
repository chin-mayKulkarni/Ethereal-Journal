package com.diary.ai.presentation.dashboard

import com.diary.ai.domain.model.User
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.diary.ai.domain.model.MediaType
import com.diary.ai.domain.model.Note
import com.diary.ai.domain.model.SyncStatus
import com.diary.ai.domain.model.AISummary
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import com.diary.ai.presentation.auth.AuthenticationManager



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DiaryViewState,
    onIntent: (DiaryUserIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    
    var textNoteContent by remember { mutableStateOf("") }
    var searchPrompt by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthenticationManager(context) }

    val user = state.user ?: User("Guest", "guest@ethereal.journal", "")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(

        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDFCFB))
            ) {
                // Header Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { showSettingsModal = true }
                        ) {
                            if (user.avatar.startsWith("http")) {
                                AsyncImage(
                                    model = user.avatar,
                                    contentDescription = "Profile Picture",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .border(2.dp, Color(0xFFFFECEF), CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFFFECEF))
                                        .border(2.dp, Color(0xFFFEE2E2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEC4899),
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = buildAnnotatedString {
                                    when (state.activeTab) {
                                        "journal" -> {
                                            append("Today")
                                            pushStyle(SpanStyle(color = Color(0xFFF43F5E))) // rose-500
                                            append(".")
                                            pop()
                                        }
                                        "stats" -> {
                                            append("Stats")
                                            pushStyle(SpanStyle(color = Color(0xFF6366F1))) // indigo-500
                                            append(".")
                                            pop()
                                        }
                                        else -> {
                                            append("Confidant")
                                            pushStyle(SpanStyle(color = Color(0xFFA855F7))) // purple-500
                                            append(".")
                                            pop()
                                        }
                                    }
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 24.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "ETHEREAL JOURNAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                letterSpacing = 1.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick AI Summary consult shortcut button
                        if (state.activeTab != "summary") {
                            Button(
                                onClick = { onIntent(DiaryUserIntent.ChangeTab("summary")) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                        ),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "AI SUMMARY",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 1.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Settings trigger
                        IconButton(
                            onClick = { showSettingsModal = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                Divider(color = Color(0xFFF1F5F9))

            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab 1: Journal
                    val isJournal = state.activeTab == "journal"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onIntent(DiaryUserIntent.ChangeTab("journal")) }
                            .width(72.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isJournal) Color(0xFFF43F5E) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = "Journal",
                                tint = if (isJournal) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "JOURNAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (isJournal) Color(0xFFF43F5E) else Color(0xFF94A3B8)
                        )
                    }

                    // Tab 2: Stats
                    val isStats = state.activeTab == "stats"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onIntent(DiaryUserIntent.ChangeTab("stats")) }
                            .width(72.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isStats) Color(0xFF1E293B) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Stats",
                                tint = if (isStats) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "STATS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (isStats) Color(0xFF1E293B) else Color(0xFF94A3B8)
                        )
                    }

                    // Tab 3: Confidant
                    val isSummary = state.activeTab == "summary"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { 
                                onIntent(DiaryUserIntent.ChangeTab("summary"))
                                if (state.summary == null) {
                                    onIntent(DiaryUserIntent.RequestDailySummary())
                                }
                            }
                            .width(72.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 44.dp, height = 44.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSummary) Color(0xFF6366F1) else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Confidant",
                                tint = if (isSummary) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "CONFIDANT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = if (isSummary) Color(0xFF6366F1) else Color(0xFF94A3B8)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (state.activeTab == "journal") {
                ExpandableIngestionFab(
                    onTextClicked = { showAddNoteDialog = true },
                    onVoiceClicked = {
                        if (state.activeVoiceState == VoiceState.IDLE) {
                            onIntent(DiaryUserIntent.TriggerVoiceIngestion)
                        } else {
                            onIntent(DiaryUserIntent.CancelVoiceIngestion)
                        }
                    },
                    onCameraClicked = {
                        onIntent(
                            DiaryUserIntent.SaveOcrNote(
                                path = "mock_ocr_photo.jpg",
                                text = "Logged text OCR snapshot: Project layout verified with stakeholders.",
                                mood = state.selectedMood
                            )
                        )
                    },
                    activeVoiceState = state.activeVoiceState
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFAF8F5),
                            Color(0xFFF1EDE4)
                        )
                    )
                )
        ) {
            when (state.activeTab) {
                "journal" -> {
                    JournalTabView(
                        state = state,
                        onIntent = onIntent,
                        onSearchClicked = { showSearchDialog = true }
                    )
                }
                "stats" -> {
                    StatsTabView(
                        state = state,
                        onIntent = onIntent
                    )
                }
                "summary" -> {
                    SummaryTabView(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }

            // Voice Recording Dialog modal overlay
            if (state.activeVoiceState == VoiceState.RECORDING) {
                VoiceRecordingOverlay(
                    onStop = { voiceText ->
                        onIntent(
                            DiaryUserIntent.SaveAudioNote(
                                path = "voice_reflection.mp3",
                                text = voiceText,
                                mood = state.selectedMood
                            )
                        )
                        onIntent(DiaryUserIntent.CancelVoiceIngestion)
                    },
                    onCancel = {
                        onIntent(DiaryUserIntent.CancelVoiceIngestion)
                    }
                )
            }
        }
    }

    // Add note dialog
    if (showAddNoteDialog) {
        var textContent by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Log Daily Reflection", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Select current mood:")
                    MoodSelectorRow(
                        selectedMood = state.selectedMood,
                        onMoodSelected = { onIntent(DiaryUserIntent.SelectMood(it)) }
                    )
                    OutlinedTextField(
                        value = textContent,
                        onValueChange = { textContent = it },
                        placeholder = { Text("Write down your thoughts...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textContent.isNotBlank()) {
                            onIntent(DiaryUserIntent.SaveTextNote(textContent, state.selectedMood))
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Search Dialog
    if (showSearchDialog) {
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Conceptual Search", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = searchPrompt,
                        onValueChange = { searchPrompt = it },
                        placeholder = { Text("Ask anything about your logs...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    if (state.searchResult != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF1F5F9)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.searchResult,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (searchPrompt.isNotBlank()) {
                            onIntent(DiaryUserIntent.SearchHistory(searchPrompt))
                        }
                    }
                ) {
                    Text("Search")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSearchDialog = false
                        searchPrompt = ""
                    }
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Settings Modal Overlay Bottom Sheet Style
    AnimatedVisibility(
        visible = showSettingsModal,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable { showSettingsModal = false }
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Settings & Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B)
                        )
                        IconButton(onClick = { showSettingsModal = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.avatar.startsWith("http")) {
                            AsyncImage(
                                model = user.avatar,
                                contentDescription = "Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color(0xFFFFECEF), CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFECEF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEC4899),
                                    fontSize = 18.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                showSettingsModal = false
                                authManager.signOutUser(coroutineScope) {
                                    onIntent(DiaryUserIntent.SignOut)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFEF2F2),
                                contentColor = Color(0xFFEF4444)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Sign Out Account", fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ExitToApp, contentDescription = null)
                            }
                        }

                        Button(
                            onClick = {
                                showSettingsModal = false
                                authManager.signOutUser(coroutineScope) {
                                    onIntent(DiaryUserIntent.SignOut)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF475569)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Reset Application Storage", fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.Refresh, contentDescription = null)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Secured with Google Cloud Ingress",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
}




@Composable
fun MoodSelectorRow(
    selectedMood: String,
    onMoodSelected: (String) -> Unit
) {
    val moods = listOf("Focused", "Reflective", "Anxious", "Grateful", "Neutral")
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(moods) { mood ->
            val isSelected = mood == selectedMood
            FilterChip(
                selected = isSelected,
                onClick = { onMoodSelected(mood) },
                label = { Text(mood) }
            )
        }
    }
}

@Composable
fun JournalTabView(
    state: DiaryViewState,
    onIntent: (DiaryUserIntent) -> Unit,
    onSearchClicked: () -> Unit
) {
    var isVoiceActive by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var voiceTimer by remember { mutableStateOf(0) }
    var newText by remember { mutableStateOf("") }
    
    // Timer effect for voice simulation
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (true) {
                delay(1000)
                voiceTimer += 1
            }
        } else {
            voiceTimer = 0
        }
    }

    val formatTimer = {
        val m = (voiceTimer / 60).toString().padStart(2, '0')
        val s = (voiceTimer % 60).toString().padStart(2, '0')
        "$m:$s"
    }

    val stopRecording = {
        isRecording = false
        val voiceTranscripts = mapOf(
            "Focused" to "Thinking about the architecture proposal. I need to make sure the server-side API routes are perfectly isolated and secure, keeping all keys hidden from client exposure. Today's sessions went exceptionally well.",
            "Reflective" to "Reflecting on the recent group meeting. Navigated several highly complex interpersonal loops with key project stake-holders. Dialogue felt open and deeply aligned.",
            "Anxious" to "A bit worried about the timing of tomorrow's release. I should probably review the rendering layout in the morning to make sure there are no clipping anomalies.",
            "Grateful" to "Extremely appreciative of the quick response from chinmayrk01. The collaborative feedback loop was outstandingly warm and professional.",
            "Neutral" to "Just registering a standard mid-day alignment. All indicators are green and operational."
        )
        newText = voiceTranscripts[state.selectedMood] ?: "Spoken reflection compiled successfully."
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CalendarStrip(
                selectedDate = state.selectedDate,
                onDateSelected = { onIntent(DiaryUserIntent.LoadNotes(it)) }
            )
        }

        item {
            SearchBarSection(onSearchClicked = onSearchClicked)
        }

        // Inline Record Reflection Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                append("Record Reflection")
                                pushStyle(SpanStyle(color = Color(0xFFF43F5E)))
                                append(".")
                                pop()
                            },
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF1E293B)
                        )

                        // Voice Entry Switcher button
                        Button(
                            onClick = {
                                isVoiceActive = !isVoiceActive
                                if (!isVoiceActive) {
                                    isRecording = false
                                }
                                newText = ""
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVoiceActive) Color(0xFFF43F5E) else Color(0xFFF1F5F9),
                                contentColor = if (isVoiceActive) Color.White else Color(0xFF475569)
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text("Voice Entry", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!isVoiceActive) {
                        // Traditional Text Area Input
                        OutlinedTextField(
                            value = newText,
                            onValueChange = { newText = it },
                            placeholder = { Text("What is occupying your thoughts today?", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFDA4AF), // rose-300
                                unfocusedBorderColor = Color(0xFFF1F5F9),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            )
                        )

                        // Mood Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "CURRENT MOOD",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )

                            // Mood custom row
                            val moodsList = listOf(
                                Triple("Focused", Icons.Default.Edit, Color(0xFFEFF6FF) to Color(0xFF1D4ED8)),
                                Triple("Reflective", Icons.Default.Face, Color(0xFFEEF2FF) to Color(0xFF4338CA)),
                                Triple("Anxious", Icons.Default.Warning, Color(0xFFFFFBEB) to Color(0xFFB45309)),
                                Triple("Grateful", Icons.Default.Favorite, Color(0xFFFFF1F2) to Color(0xFFBE123C)),
                                Triple("Neutral", Icons.Default.Info, Color(0xFFF8FAFC) to Color(0xFF475569))
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(moodsList) { (mName, mIcon, colors) ->
                                    val isSel = state.selectedMood == mName
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSel) colors.first else Color.White)
                                            .border(
                                                width = 1.dp,
                                                color = if (isSel) colors.second else Color(0xFFE2E8F0),
                                                shape = CircleShape
                                            )
                                            .clickable { onIntent(DiaryUserIntent.SelectMood(mName)) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = mIcon,
                                                contentDescription = mName,
                                                tint = if (isSel) colors.second else Color(0xFF64748B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = mName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) colors.second else Color(0xFF64748B)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Save reflection button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    if (newText.isNotBlank()) {
                                        onIntent(DiaryUserIntent.SaveTextNote(newText, state.selectedMood))
                                        newText = ""
                                    }
                                },
                                enabled = newText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    disabledContainerColor = Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .background(
                                        if (newText.isNotBlank()) {
                                            Brush.linearGradient(colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899)))
                                        } else {
                                            Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("Save Entry", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    } else {
                        // Voice Recording Dictation simulation view
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isRecording) {
                                // Waveform animation representation
                                Row(
                                    modifier = Modifier.height(40.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val waveHeights = listOf(12.dp, 24.dp, 32.dp, 16.dp, 36.dp, 8.dp, 24.dp, 32.dp, 20.dp, 36.dp, 28.dp, 12.dp, 20.dp, 32.dp)
                                    waveHeights.forEach { h ->
                                        Box(
                                            modifier = Modifier
                                                .width(4.dp)
                                                .height(h)
                                                .background(Color(0xFFFDA4AF), CircleShape)
                                        )
                                    }
                                }

                                Text(
                                    text = formatTimer(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1E293B)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .clickable { stopRecording() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Stop Recording",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Text(
                                    text = "LISTENING TO VOCAL REFLECTIONS...",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF94A3B8),
                                    letterSpacing = 1.sp
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                            )
                                        )
                                        .clickable { isRecording = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start Recording",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Tap to dictate your diary",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "USING SOPHISTICATED VOICE ENGINE",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )
                                }

                                if (newText.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "\"$newText\"",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569)
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            if (newText.isNotBlank()) {
                                                onIntent(DiaryUserIntent.SaveTextNote(newText, state.selectedMood))
                                                newText = ""
                                                isVoiceActive = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent
                                        ),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier
                                            .height(36.dp)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(Color(0xFF6366F1), Color(0xFFEC4899))
                                                ),
                                                shape = RoundedCornerShape(20.dp)
                                            ),
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        Text("Use Transcript", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Reflections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFFF1F2))
                        .border(1.dp, Color(0xFFFFE4E6), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${state.notes.size} ${if (state.notes.size == 1) "ENTRY" else "ENTRIES"}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE11D48)
                    )
                }
            }
        }

        if (state.notes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "Empty canvas awaits",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = "No logs recorded for today. Express your thoughts above!",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(state.notes, key = { it.id }) { note ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "14:32",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                val moodColors = when (note.content.substringBefore(" ").trim()) {
                                    "Thinking" -> Triple("Focused", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
                                    "Reflecting" -> Triple("Reflective", Color(0xFFEEF2FF), Color(0xFF4338CA))
                                    "A" -> Triple("Anxious", Color(0xFFFFFBEB), Color(0xFFB45309))
                                    "Extremely" -> Triple("Grateful", Color(0xFFFFF1F2), Color(0xFFBE123C))
                                    else -> Triple("Neutral", Color(0xFFF8FAFC), Color(0xFF475569))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(moodColors.second)
                                        .border(1.dp, moodColors.third.copy(alpha = 0.3f), CircleShape)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = moodColors.first,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = moodColors.third
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFCBD5E1),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = note.content,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFF334155),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onIntent(DiaryUserIntent.ChangeTab("summary"))
                        onIntent(DiaryUserIntent.RequestDailySummary())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFFEC4899))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GENERATE CONFIDANT SUMMARY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun StatsTabView(
    state: DiaryViewState,
    onIntent: (DiaryUserIntent) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Sleek wooden/dark vector container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ethereal Vector",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Ethereal State",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color(0xFF1E293B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Energy radial circular progress ring
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clickable { onIntent(DiaryUserIntent.TapEnergyBoost) },
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = state.energyPercent / 100f,
                            strokeWidth = 12.dp,
                            color = Color(0xFF34D399),
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${state.energyPercent}%",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 32.sp
                            )
                            Text(
                                text = "ENERGY",
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "TAP TO BOOST",
                                color = Color(0xFF34D399),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Your energy level fluctuates in real-time based on the emotional frequencies registered in your diary logs.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    // Data pills for steps & sleep
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Steps card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("STEPS", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = state.simulatedSteps.toString(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { onIntent(DiaryUserIntent.AdjustSteps(500)) },
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    ) {
                                        Text("+500", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Button(
                                        onClick = { onIntent(DiaryUserIntent.AdjustSteps(-500)) },
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    ) {
                                        Text("-500", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }

                        // Sleep card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("SLEEP", color = Color(0xFF64748B), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(12.dp))
                                }
                                Text(
                                    text = "${state.simulatedSleepHours}h ${state.simulatedSleepMinutes}m",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { onIntent(DiaryUserIntent.AdjustSleep(15)) },
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    ) {
                                        Text("+15m", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Button(
                                        onClick = { onIntent(DiaryUserIntent.AdjustSleep(-15)) },
                                        contentPadding = PaddingValues(0.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(24.dp)
                                    ) {
                                        Text("-15m", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Analytics lists
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Journal Analytics",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1E293B)
                    )

                    // Total words metric card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFFFEDD5), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFFF97316))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Total Words Logged", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            val wordCount = state.notes.sumOf { it.content.split("\\s+".toRegex()).size }
                            Text("$wordCount words", fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                        }
                    }

                    // Mood frequency breakdown
                    Text(
                        text = "LOGGED FREQUENCY BY MOOD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 1.sp
                    )

                    val moodsList = listOf("Focused", "Reflective", "Anxious", "Grateful", "Neutral")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        moodsList.forEach { mName ->
                            val count = state.notes.count { it.content.contains(mName, ignoreCase = true) || state.selectedMood == mName }
                            val total = state.notes.size.coerceAtLeast(1)
                            val progress = count.toFloat() / total.toFloat()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569),
                                    modifier = Modifier.width(72.dp)
                                )
                                LinearProgressIndicator(
                                    progress = progress,
                                    color = Color(0xFF6366F1),
                                    trackColor = Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$count (${(progress * 100).toInt()}%)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8),
                                    modifier = Modifier.width(48.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryTabView(

    state: DiaryViewState,
    onIntent: (DiaryUserIntent) -> Unit
) {
    var refinementInput by remember { mutableStateOf("") }
    var activeRefinements by remember { mutableStateOf(emptyList<String>()) }
    var copied by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isSummaryLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFF6366F1)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "\"Consulting Ethereal Mind...\"",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "READING REFLECTIONS, MAPPING EMOTIONAL TRAJECTORIES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        } else if (state.summary == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFEEF2F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No summary generated yet",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Allow Gemini to read your current emotional patterns and produce milestones and actionable vectors.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onIntent(DiaryUserIntent.RequestDailySummary()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Generate Daily Insight", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val summary = state.summary
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Atmosphere Visual Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFFA855F7),
                                        Color(0xFFEC4899)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Confidant Reflection", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 9.sp)
                                Text("Daily emotional vectors stabilized", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("Updated: June 25, 2026", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                            }
                        }
                    }
                }

                // 1. Milestones
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row {
                            // Thick Left Border
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(120.dp) // arbitrary height matching content
                                    .background(Color(0xFF6366F1)) // Indigo-500
                            )
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF6366F1),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Milestones",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E293B),
                                        letterSpacing = 1.sp
                                    )
                                }
                                for (milestone in summary.milestones) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = milestone,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Actionable Vectors
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(120.dp)
                                    .background(Color(0xFFF43F5E)) // Rose-500
                            )
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF43F5E),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Actionable Vectors",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E293B),
                                        letterSpacing = 1.sp
                                    )
                                }
                                for (vector in summary.actionableVectors) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = Color(0xFFF43F5E), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = vector,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Emotional Tone
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 0.dp, bottomStart = 0.dp, topEnd = 16.dp, bottomEnd = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(120.dp)
                                    .background(Color(0xFFF59E0B)) // Amber-500
                            )
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Emotional Tone",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E293B),
                                        letterSpacing = 1.sp
                                    )
                                }
                                for (tone in summary.emotionalTone) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("•", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                                        Text(
                                            text = tone,
                                            fontSize = 12.sp,
                                            color = Color(0xFF475569),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Refinement History bubbles
                if (activeRefinements.isNotEmpty()) {
                    item {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "REFINEMENT HISTORY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 1.sp
                            )
                            activeRefinements.forEach { ref ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp, topEnd = 0.dp))
                                            .background(Color(0xFFFFF1F2))
                                            .border(1.dp, Color(0xFFFFE4E6), RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp, topEnd = 0.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = ref,
                                            fontSize = 12.sp,
                                            color = Color(0xFFE11D48),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFFECEF)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "U",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFEC4899),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Copy & Share buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                copied = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFEEF2FF),
                                contentColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (copied) Icons.Default.Check else Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(if (copied) "Copied" else "Copy Insight", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = { /* Share action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF1F5F9),
                                contentColor = Color(0xFF475569)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("Share Log", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Refinement Chat Form input
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = refinementInput,
                            onValueChange = { refinementInput = it },
                            placeholder = { Text("Ask Gemini to refine (e.g. 'make it warm')", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFC7D2FE),
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )
                        IconButton(
                            onClick = {
                                if (refinementInput.isNotBlank()) {
                                    val input = refinementInput.trim()
                                    activeRefinements = activeRefinements + input
                                    onIntent(DiaryUserIntent.RequestDailySummary(input))
                                    refinementInput = ""
                                }
                            },
                            enabled = refinementInput.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    if (refinementInput.isNotBlank()) Color(0xFF8B5CF6) else Color(0xFFE2E8F0),
                                    CircleShape
                                )
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Refine", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CalendarStrip(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    // Days mapping for June 22 - June 28, 2026
    val daysList = listOf(
        Triple("Mon", "22", "2026-06-22"),
        Triple("Tue", "23", "2026-06-23"),
        Triple("Wed", "24", "2026-06-24"),
        Triple("Thu", "25", "2026-06-25"),
        Triple("Fri", "26", "2026-06-26"),
        Triple("Sat", "27", "2026-06-27"),
        Triple("Sun", "28", "2026-06-28")
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "June 2026",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Week 26",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysList.forEach { (dayName, dateNum, fullDate) ->
                    val isSelected = fullDate == selectedDate
                    
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF6366F1), // indigo-500
                                            Color(0xFFA855F7), // purple-500
                                            Color(0xFFEC4899)  // rose-500
                                        )
                                    )
                                } else {
                                    Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
                                }
                            )
                            .clickable { onDateSelected(fullDate) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = dayName.uppercase(),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                            Text(
                                text = dateNum,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SearchBarSection(onSearchClicked: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSearchClicked() },
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Search logs & synthesize summaries...",
                color = Color(0xFF64748B),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun AISummaryCard(
    summary: AISummary?,
    isLoading: Boolean,
    error: String?,
    onRefinementSubmit: (String) -> Unit,
    onGenerateClicked: () -> Unit
) {
    var refinementText by remember { mutableStateOf("") }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cognitive Intelligence Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            if (summary != null) {
                // Milestones
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Milestones",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFF6366F1),
                        letterSpacing = 1.sp
                    )
                    for (milestone in summary.milestones) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                            Text(text = milestone, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
                        }
                    }
                }

                // Actionable vectors
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Actionable Suggestions",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFF8B5CF6),
                        letterSpacing = 1.sp
                    )
                    for (vector in summary.actionableVectors) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", color = Color(0xFF8B5CF6), fontWeight = FontWeight.Bold)
                            Text(text = vector, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
                        }
                    }
                }

                // Emotional tone
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Emotional Insights",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color(0xFFEC4899),
                        letterSpacing = 1.sp
                    )
                    for (tone in summary.emotionalTone) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("•", color = Color(0xFFEC4899), fontWeight = FontWeight.Bold)
                            Text(text = tone, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF334155))
                        }
                    }
                }

                Divider(color = Color(0xFFF1F5F9))

                // Refinement input field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = refinementText,
                        onValueChange = { refinementText = it },
                        placeholder = { Text("Refine summary...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(
                        onClick = {
                            if (refinementText.isNotBlank()) {
                                onRefinementSubmit(refinementText)
                                refinementText = ""
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF6366F1), CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                Text("No intelligence summary compiled for today.", color = Color(0xFF64748B))
                Button(onClick = onGenerateClicked) {
                    Text("Generate AI Analysis")
                }
            }
        }
    }
}

@Composable
fun NoteListItem(note: Note) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val icon = when (note.mediaType) {
                MediaType.TEXT -> Icons.Default.Edit
                MediaType.VOICE -> Icons.Default.PlayArrow
                MediaType.PHOTO_OCR -> Icons.Default.List
            }
            
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFFEEF2F6),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = note.mediaType.name,
                    tint = Color(0xFF6366F1)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1E293B)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = note.mediaType.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (note.syncStatus == SyncStatus.SYNCHRONIZED) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                shape = CircleShape
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (note.syncStatus == SyncStatus.SYNCHRONIZED) "Synced" else "Pending",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (note.syncStatus == SyncStatus.SYNCHRONIZED) Color(0xFF15803D) else Color(0xFFB91C1C)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStatePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Empty",
            tint = Color(0xFFCBD5E1),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "Your journal is clear. Add an entry to begin.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ExpandableIngestionFab(
    onTextClicked: () -> Unit,
    onVoiceClicked: () -> Unit,
    onCameraClicked: () -> Unit,
    activeVoiceState: VoiceState
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (expanded) {
            SmallFloatingActionButton(
                onClick = {
                    onTextClicked()
                    expanded = false
                },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Text Note")
            }
            SmallFloatingActionButton(
                onClick = {
                    onVoiceClicked()
                    expanded = false
                },
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (activeVoiceState == VoiceState.RECORDING) Icons.Default.Close else Icons.Default.PlayArrow,
                    contentDescription = "Voice dictation"
                )
            }
            SmallFloatingActionButton(
                onClick = {
                    onCameraClicked()
                    expanded = false
                },
                shape = CircleShape
            ) {
                Icon(Icons.Default.List, contentDescription = "Camera OCR")
            }
        }

        FloatingActionButton(
            onClick = { expanded = !expanded },
            shape = CircleShape,
            containerColor = Color(0xFF6366F1),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Add journal"
            )
        }
    }
}

@Composable
fun VoiceRecordingOverlay(
    onStop: (String) -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Streaming Voice Dictation...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 4.dp,
                    color = Color(0xFF6366F1)
                )
                
                Text(
                    text = "Say: 'Reviewing android architecture patterns and resolving synchronization tasks.'",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF64748B)
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = {
                            onStop("Reviewing android architecture patterns and resolving synchronization tasks.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
