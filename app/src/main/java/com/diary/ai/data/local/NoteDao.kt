package com.diary.ai.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE date_string = :dateString AND user_id = :userId ORDER BY last_modified ASC")
    fun getNotesByDate(dateString: String, userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE sync_status != 'SYNCHRONIZED' AND user_id = :userId")
    suspend fun getPendingSyncNotes(userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE date_string = :dateString AND user_id = :userId ORDER BY last_modified ASC")
    suspend fun getNotesByDateList(dateString: String, userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE user_id = :userId ORDER BY last_modified ASC")
    suspend fun getAllNotesForUser(userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Delete
    suspend fun delete(note: NoteEntity)

    // ─────────────────────────────────────────────────────────────────────────
    // Sync-Support Queries — used exclusively by SyncWorker reconciliation
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the maximum [NoteEntity.lastModified] timestamp for all notes
     * belonging to [userId] on [dateString].
     *
     * Used in Phase B (pull) of the sync algorithm to compare local data
     * freshness against the Google Drive file's modification timestamp.
     * Returns null when no local entries exist for that date.
     */
    @Query(
        "SELECT MAX(last_modified) FROM notes " +
        "WHERE date_string = :dateString AND user_id = :userId"
    )
    suspend fun getMaxLastModifiedForDate(dateString: String, userId: String): Long?

    /**
     * Atomically bulk-inserts or replaces a list of [NoteEntity] rows that
     * have been downloaded from Google Drive.
     *
     * Wrapped in @Transaction so that a partial failure causes a full rollback,
     * preventing the local DB from being left in a partially-updated state.
     * All entries are stamped SYNCHRONIZED because they originate from Drive.
     */
    @Transaction
    suspend fun upsertFromCloud(notes: List<NoteEntity>) {
        for (note in notes) {
            insertOrUpdate(note.copy(syncStatus = "SYNCHRONIZED"))
        }
    }

    /**
     * Returns all unique dateString values (YYYY-MM-DD) for [userId].
     *
     * Used during initial sync on a new device: after Drive's
     * [listAllDayFiles] reveals which cloud dates exist, this query reveals
     * which dates already exist locally so we can skip re-downloading them.
     */
    @Query(
        "SELECT DISTINCT date_string FROM notes WHERE user_id = :userId ORDER BY date_string ASC"
    )
    suspend fun getDistinctDatesForUser(userId: String): List<String>
}
