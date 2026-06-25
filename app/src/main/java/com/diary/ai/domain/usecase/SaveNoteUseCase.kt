package com.diary.ai.domain.usecase

import com.diary.ai.domain.model.Note
import com.diary.ai.domain.repository.DiaryRepository
import com.diary.ai.domain.repository.SyncScheduler

class SaveNoteUseCase(
    private val repository: DiaryRepository,
    private val syncScheduler: SyncScheduler
) {
    suspend operator fun invoke(note: Note) {
        repository.saveNote(note)
        syncScheduler.scheduleImmediateSync()
    }
}
