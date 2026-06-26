package com.diary.ai.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.diary.ai.DiaryApplication
import com.diary.ai.data.local.NoteDatabase
import com.diary.ai.data.local.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database: NoteDatabase by lazy {
        NoteDatabase.getDatabase(applicationContext)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val noteDao = database.noteDao()
        // Default to a mock userId if firebase is not logged in or initialized
        val userId = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: "mock-user-id"
        } catch (e: Exception) {
            "mock-user-id"
        }

        try {
            // 1. Fetch local modifications
            val pendingNotes = noteDao.getPendingSyncNotes(userId)
            
            // 2. LWW conflict reconciliation strategy
            for (localNote in pendingNotes) {
                val remoteNote = MockCloudRepository.getNoteById(localNote.id)
                if (remoteNote == null || localNote.lastModified > remoteNote.lastModified) {
                    // Local is newer -> Push to remote
                    MockCloudRepository.saveNote(localNote)
                    noteDao.insertOrUpdate(localNote.copy(syncStatus = "SYNCHRONIZED"))
                } else {
                    // Remote is newer -> Overwrite local
                    noteDao.insertOrUpdate(remoteNote.copy(syncStatus = "SYNCHRONIZED"))
                }
            }

            // 3. Fetch remote updates and apply locally
            val remoteNotes = MockCloudRepository.getUpdatedNotesForUser(userId)
            for (remoteNote in remoteNotes) {
                val localNote = noteDao.getNoteById(remoteNote.id)
                if (localNote == null || remoteNote.lastModified > localNote.lastModified) {
                    noteDao.insertOrUpdate(remoteNote.copy(syncStatus = "SYNCHRONIZED"))
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
