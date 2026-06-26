package com.diary.ai.domain.usecase

import com.diary.ai.domain.model.MediaType
import com.diary.ai.domain.model.Note
import com.diary.ai.domain.model.SyncStatus
import com.diary.ai.domain.repository.DiaryRepository
import com.diary.ai.domain.repository.SyncScheduler
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.assertTrue

class SaveNoteUseCaseTest {

    class MockDiaryRepository : DiaryRepository {
        var saveNoteCalled = false
        override fun getNotesByDate(dateString: String, userId: String) = TODO()
        override suspend fun saveNote(note: Note) { saveNoteCalled = true }
        override suspend fun generateDailySummary(dateString: String, userId: String, refinement: String?) = TODO()
        override suspend fun searchNotes(query: String, userId: String) = TODO()
    }

    class MockSyncScheduler : SyncScheduler {
        var syncScheduled = false
        override fun scheduleImmediateSync() { syncScheduled = true }
    }

    @Test
    fun `invoke calls repository save and schedules sync`() = runBlocking {
        val repository = MockDiaryRepository()
        val scheduler = MockSyncScheduler()
        val useCase = SaveNoteUseCase(repository, scheduler)
        
        val note = Note("1", "user", "2026-06-25", "content", MediaType.TEXT, null, SyncStatus.PENDING_INSERT, 0L)
        
        useCase(note)
        
        assertTrue(repository.saveNoteCalled)
        assertTrue(scheduler.syncScheduled)
    }
}
