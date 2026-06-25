package com.diary.ai.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diary.ai.domain.model.MediaType
import com.diary.ai.domain.model.Note
import com.diary.ai.domain.model.SyncStatus
import com.diary.ai.domain.usecase.GenerateDailySummaryUseCase
import com.diary.ai.domain.usecase.GetNotesByDateUseCase
import com.diary.ai.domain.usecase.PerformSemanticSearchUseCase
import com.diary.ai.domain.usecase.SaveNoteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class DiaryViewModel(
    private val getNotesByDateUseCase: GetNotesByDateUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val generateDailySummaryUseCase: GenerateDailySummaryUseCase,
    private val performSemanticSearchUseCase: PerformSemanticSearchUseCase
) : ViewModel() {

    private val _viewState = MutableStateFlow(DiaryViewState())
    val viewState: StateFlow<DiaryViewState> = _viewState.asStateFlow()
    private var notesJob: Job? = null

    private fun getActiveUserId(): String {
        return _viewState.value.user?.email ?: "guest@ethereal.journal"
    }

    fun processIntent(intent: DiaryUserIntent) {
        when (intent) {
            is DiaryUserIntent.LoadNotes -> {
                _viewState.update { it.copy(selectedDate = intent.dateString, isLoading = true) }
                notesJob?.cancel()
                notesJob = viewModelScope.launch {
                    getNotesByDateUseCase(intent.dateString, getActiveUserId()).collect { notesList ->
                        _viewState.update { it.copy(notes = notesList, isLoading = false) }
                    }
                }
            }
            is DiaryUserIntent.SaveTextNote -> {
                viewModelScope.launch {
                    val note = Note(
                        id = UUID.randomUUID().toString(),
                        userId = getActiveUserId(),
                        dateString = _viewState.value.selectedDate,
                        content = intent.content,
                        mediaType = MediaType.TEXT,
                        mediaPath = null,
                        syncStatus = SyncStatus.PENDING_INSERT,
                        lastModified = System.currentTimeMillis()
                    )
                    saveNoteUseCase(note)
                    _viewState.update { 
                        it.copy(
                            energyPercent = Math.min(100, it.energyPercent + 4),
                            simulatedSteps = it.simulatedSteps + 120
                        )
                    }
                }
            }
            is DiaryUserIntent.SaveAudioNote -> {
                viewModelScope.launch {
                    val note = Note(
                        id = UUID.randomUUID().toString(),
                        userId = getActiveUserId(),
                        dateString = _viewState.value.selectedDate,
                        content = intent.text,
                        mediaType = MediaType.VOICE,
                        mediaPath = intent.path,
                        syncStatus = SyncStatus.PENDING_INSERT,
                        lastModified = System.currentTimeMillis()
                    )
                    saveNoteUseCase(note)
                    _viewState.update { 
                        it.copy(
                            energyPercent = Math.min(100, it.energyPercent + 4),
                            simulatedSteps = it.simulatedSteps + 120
                        )
                    }
                }
            }
            is DiaryUserIntent.SaveOcrNote -> {
                viewModelScope.launch {
                    val note = Note(
                        id = UUID.randomUUID().toString(),
                        userId = getActiveUserId(),
                        dateString = _viewState.value.selectedDate,
                        content = intent.text,
                        mediaType = MediaType.PHOTO_OCR,
                        mediaPath = intent.path,
                        syncStatus = SyncStatus.PENDING_INSERT,
                        lastModified = System.currentTimeMillis()
                    )
                    saveNoteUseCase(note)
                    _viewState.update { 
                        it.copy(
                            energyPercent = Math.min(100, it.energyPercent + 4),
                            simulatedSteps = it.simulatedSteps + 120
                        )
                    }
                }
            }
            is DiaryUserIntent.RequestDailySummary -> {
                _viewState.update { it.copy(isSummaryLoading = true, error = null) }
                viewModelScope.launch {
                    try {
                        val summaryResult = generateDailySummaryUseCase(
                            dateString = _viewState.value.selectedDate,
                            userId = getActiveUserId(),
                            refinement = intent.refinement
                        )
                        _viewState.update { it.copy(summary = summaryResult, isSummaryLoading = false) }
                    } catch (e: Exception) {
                        _viewState.update { it.copy(isSummaryLoading = false, error = "Failed to fetch AI summary: ${e.message}") }
                    }
                }
            }
            is DiaryUserIntent.SearchHistory -> {
                _viewState.update { it.copy(isLoading = true) }
                viewModelScope.launch {
                    try {
                        val result = performSemanticSearchUseCase(intent.query, getActiveUserId())
                        _viewState.update { it.copy(searchResult = result, isLoading = false) }
                    } catch (e: Exception) {
                        _viewState.update { it.copy(error = "Search failed: ${e.message}", isLoading = false) }
                    }
                }
            }
            DiaryUserIntent.TriggerVoiceIngestion -> {
                _viewState.update { it.copy(activeVoiceState = VoiceState.RECORDING) }
            }
            DiaryUserIntent.CancelVoiceIngestion -> {
                _viewState.update { it.copy(activeVoiceState = VoiceState.IDLE) }
            }
            is DiaryUserIntent.SignIn -> {
                _viewState.update { it.copy(user = intent.user, activeTab = "journal") }
                processIntent(DiaryUserIntent.LoadNotes(_viewState.value.selectedDate))
            }
            DiaryUserIntent.SignOut -> {
                notesJob?.cancel()
                _viewState.update { 
                    DiaryViewState() // Reset back to default initial state (user = null)
                }
            }
            is DiaryUserIntent.ChangeTab -> {
                _viewState.update { it.copy(activeTab = intent.tab) }
            }
            is DiaryUserIntent.SelectMood -> {
                _viewState.update { it.copy(selectedMood = intent.mood) }
            }
            DiaryUserIntent.TapEnergyBoost -> {
                _viewState.update { it.copy(energyPercent = Math.min(100, it.energyPercent + 8)) }
            }
            is DiaryUserIntent.AdjustSteps -> {
                _viewState.update { it.copy(simulatedSteps = Math.max(0, it.simulatedSteps + intent.amount)) }
            }
            is DiaryUserIntent.AdjustSleep -> {
                _viewState.update {
                    var mins = it.simulatedSleepMinutes + intent.minutesAmount
                    var hrs = it.simulatedSleepHours
                    if (mins >= 60) {
                        mins = 0
                        hrs += 1
                    } else if (mins < 0) {
                        mins = 45
                        hrs = Math.max(0, hrs - 1)
                    }
                    it.copy(simulatedSleepHours = hrs, simulatedSleepMinutes = mins)
                }
            }
        }
    }
}
