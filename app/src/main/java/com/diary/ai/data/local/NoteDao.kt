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
}
