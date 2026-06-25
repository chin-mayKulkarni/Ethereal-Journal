package com.diary.ai.domain.usecase

import com.diary.ai.domain.model.Note
import com.diary.ai.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetNotesByDateUseCase(private val repository: DiaryRepository) {
    operator fun invoke(dateString: String, userId: String): Flow<List<Note>> {
        return repository.getNotesByDate(dateString, userId)
            .map { notes -> notes.sortedBy { it.lastModified } }
    }
}
