package com.diary.ai.presentation.dashboard

import com.diary.ai.domain.model.Note
import com.diary.ai.domain.model.AISummary
import com.diary.ai.domain.model.User

data class DiaryViewState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val selectedDate: String = "2026-06-25",
    val notes: List<Note> = emptyList(),
    val summary: AISummary? = null,
    val isSummaryLoading: Boolean = false,
    val searchResult: String? = null,
    val error: String? = null,
    val activeVoiceState: VoiceState = VoiceState.IDLE,
    val activeTab: String = "journal", // "journal", "stats", "summary"
    val selectedMood: String = "Focused",
    val energyPercent: Int = 84,
    val simulatedSteps: Int = 12402,
    val simulatedSleepHours: Int = 7,
    val simulatedSleepMinutes: Int = 20
)

enum class VoiceState { IDLE, RECORDING, PARSING }

sealed interface DiaryUserIntent {
    data class LoadNotes(val dateString: String) : DiaryUserIntent
    data class SaveTextNote(val content: String, val mood: String) : DiaryUserIntent
    data class SaveAudioNote(val path: String, val text: String, val mood: String) : DiaryUserIntent
    data class SaveOcrNote(val path: String, val text: String, val mood: String) : DiaryUserIntent
    data class RequestDailySummary(val refinement: String? = null) : DiaryUserIntent
    data class SearchHistory(val query: String) : DiaryUserIntent
    object TriggerVoiceIngestion : DiaryUserIntent
    object CancelVoiceIngestion : DiaryUserIntent
    
    // Auth & Navigation Intents
    data class SignIn(val user: User) : DiaryUserIntent
    object SignOut : DiaryUserIntent
    data class ChangeTab(val tab: String) : DiaryUserIntent
    data class SelectMood(val mood: String) : DiaryUserIntent
    object TapEnergyBoost : DiaryUserIntent
    data class AdjustSteps(val amount: Int) : DiaryUserIntent
    data class AdjustSleep(val minutesAmount: Int) : DiaryUserIntent
}
