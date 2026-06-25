package com.diary.ai.domain.repository

import com.diary.ai.domain.model.AISummary
import com.diary.ai.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun getNotesByDate(dateString: String, userId: String): Flow<List<Note>>
    suspend fun saveNote(note: Note)
    suspend fun generateDailySummary(dateString: String, userId: String, refinement: String?): AISummary
    suspend fun searchNotes(query: String, userId: String): String
}
