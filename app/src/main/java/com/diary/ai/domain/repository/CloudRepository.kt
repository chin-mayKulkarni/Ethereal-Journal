package com.diary.ai.domain.repository

import com.diary.ai.data.local.NoteEntity

interface CloudRepository {
    suspend fun getNoteById(id: String): NoteEntity?
    suspend fun saveNote(note: NoteEntity)
    suspend fun getUpdatedNotesForUser(userId: String): List<NoteEntity>
}
