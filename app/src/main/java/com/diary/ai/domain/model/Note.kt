package com.diary.ai.domain.model

enum class MediaType { TEXT, VOICE, PHOTO_OCR }
enum class SyncStatus { PENDING_INSERT, PENDING_UPDATE, SYNCHRONIZED }

data class Note(
    val id: String,
    val userId: String,
    val dateString: String,
    val content: String,
    val mediaType: MediaType,
    val mediaPath: String?,
    val syncStatus: SyncStatus,
    val lastModified: Long
)
