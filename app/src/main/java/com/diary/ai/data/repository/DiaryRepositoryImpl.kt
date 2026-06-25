package com.diary.ai.data.repository

import com.diary.ai.data.local.NoteDao
import com.diary.ai.data.local.NoteEntity
import com.diary.ai.data.ai.GeminiClient
import com.diary.ai.domain.model.AISummary
import com.diary.ai.domain.model.MediaType
import com.diary.ai.domain.model.Note
import com.diary.ai.domain.model.SyncStatus
import com.diary.ai.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DiaryRepositoryImpl(
    private val noteDao: NoteDao,
    private val geminiClient: GeminiClient
) : DiaryRepository {

    override fun getNotesByDate(dateString: String, userId: String): Flow<List<Note>> {
        return noteDao.getNotesByDate(dateString, userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveNote(note: Note) {
        noteDao.insertOrUpdate(note.toEntity())
    }

    override suspend fun generateDailySummary(dateString: String, userId: String, refinement: String?): AISummary {
        val notes = noteDao.getNotesByDateList(dateString, userId).map { it.toDomain() }
        return geminiClient.generateDailySummary(notes, dateString, refinement)
    }

    override suspend fun searchNotes(query: String, userId: String): String {
        val allNotes = noteDao.getAllNotesForUser(userId).map { it.toDomain() }
        return geminiClient.searchTopicAcrossDates(allNotes, query)
    }

    private fun NoteEntity.toDomain() = Note(
        id = id,
        userId = userId,
        dateString = dateString,
        content = content,
        mediaType = MediaType.valueOf(mediaType),
        mediaPath = mediaPath,
        syncStatus = SyncStatus.valueOf(syncStatus),
        lastModified = lastModified
    )

    private fun Note.toEntity() = NoteEntity(
        id = id,
        userId = userId,
        dateString = dateString,
        content = content,
        mediaType = mediaType.name,
        mediaPath = mediaPath,
        syncStatus = syncStatus.name,
        lastModified = lastModified
    )
}
