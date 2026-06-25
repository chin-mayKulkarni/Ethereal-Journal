package com.diary.ai.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notes", indices = [Index(value = ["date_string", "user_id"])])
data class NoteEntity(
    @PrimaryKey val id: String, // UUID v4
    @ColumnInfo(name = "user_id") val userId: String, // Firebase/Supabase Authenticated UID
    @ColumnInfo(name = "date_string") val dateString: String, // YYYY-MM-DD format
    @ColumnInfo(name = "content") val content: String,
    @ColumnInfo(name = "media_type") val mediaType: String, // TEXT, VOICE, PHOTO_OCR
    @ColumnInfo(name = "media_path") val mediaPath: String?,
    @ColumnInfo(name = "sync_status") val syncStatus: String, // PENDING_INSERT, PENDING_UPDATE, SYNCHRONIZED
    @ColumnInfo(name = "last_modified") val lastModified: Long // Unix Epoch UTC Timestamp
)
