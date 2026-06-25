package com.diary.ai.data.sync

import com.diary.ai.data.local.NoteEntity
import java.util.concurrent.ConcurrentHashMap

object MockCloudRepository {
    private val cloudStore = ConcurrentHashMap<String, NoteEntity>()

    fun getNoteById(id: String): NoteEntity? {
        return cloudStore[id]
    }

    fun saveNote(note: NoteEntity) {
        cloudStore[note.id] = note
    }

    fun getUpdatedNotesForUser(userId: String): List<NoteEntity> {
        return cloudStore.values.filter { it.userId == userId }
    }

    fun clear() {
        cloudStore.clear()
    }
}
