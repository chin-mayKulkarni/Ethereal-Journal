package com.diary.ai

import com.diary.ai.data.local.NoteEntity
import com.diary.ai.data.sync.MockCloudRepository
import com.diary.ai.domain.model.AISummary
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SyncResolutionTest {

    @Before
    fun setUp() {
        MockCloudRepository.clear()
    }

    @Test
    fun testLastWriteWinsReconciliation() {
        val noteId = "test-note-uuid"
        
        // 1. Setup a remote note with an older timestamp
        val remoteNote = NoteEntity(
            id = noteId,
            userId = "user-123",
            dateString = "2026-06-25",
            content = "Old remote note content",
            mediaType = "TEXT",
            mediaPath = null,
            syncStatus = "SYNCHRONIZED",
            lastModified = 1000L // old timestamp
        )
        MockCloudRepository.saveNote(remoteNote)

        // 2. Setup a local note with a newer timestamp (LWW)
        val localNote = NoteEntity(
            id = noteId,
            userId = "user-123",
            dateString = "2026-06-25",
            content = "New local note content (updated)",
            mediaType = "TEXT",
            mediaPath = null,
            syncStatus = "PENDING_UPDATE",
            lastModified = 2000L // newer timestamp
        )

        // Simulate SyncWorker resolution logic:
        val fetchedRemote = MockCloudRepository.getNoteById(localNote.id)
        assertNotNull(fetchedRemote)
        
        if (fetchedRemote == null || localNote.lastModified > fetchedRemote.lastModified) {
            MockCloudRepository.saveNote(localNote)
        }

        // 3. Verify that the cloud has been updated with the newer local content
        val updatedRemote = MockCloudRepository.getNoteById(noteId)
        assertNotNull(updatedRemote)
        assertEquals("New local note content (updated)", updatedRemote?.content)
    }

    @Test
    fun testGeminiJsonParsing() {
        val mockJsonText = """
            {
              "milestones": [
                "Achieved flow state in Android migration",
                "Successfully set up clean domain boundaries"
              ],
              "actionableVectors": [
                "Verify dependencies compile cleanly",
                "Integrate speech to text dictation"
              ],
              "emotionalTone": [
                "Focused and intellectually stimulated"
              ]
            }
        """.trimIndent()

        // Parse logic matching GeminiClient
        val json = Json.parseToJsonElement(mockJsonText).jsonObject
        val milestones = json["milestones"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val actionableVectors = json["actionableVectors"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val emotionalTone = json["emotionalTone"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

        val parsedSummary = AISummary(milestones, actionableVectors, emotionalTone)

        assertEquals(2, parsedSummary.milestones.size)
        assertEquals("Achieved flow state in Android migration", parsedSummary.milestones[0])
        assertEquals("Successfully set up clean domain boundaries", parsedSummary.milestones[1])
        assertEquals("Focused and intellectually stimulated", parsedSummary.emotionalTone[0])
    }
}
