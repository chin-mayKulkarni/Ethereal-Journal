package com.diary.ai.data.ai

import com.diary.ai.domain.model.Note
import com.diary.ai.domain.model.AISummary
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GeminiClient(apiKey: String) {
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey,
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        },
        systemInstruction = content {
            text("You are an elegant, mindful therapist and executive coach. Focus on high-value bullet points.")
        }
    )

    suspend fun generateDailySummary(notes: List<Note>, date: String, refinement: String?): AISummary {
        val notesBlock = notes.mapIndexed { index, note ->
            "Entry ${index + 1} (${note.mediaType} - Last Modified: ${note.lastModified}):\n\"${note.content}\""
        }.joinToString("\n\n")

        val prompt = """
            Analyze the user's journal entries for $date and generate an insightful, highly polished daily summary.
            Format your analysis exactly in a JSON response schema containing the following list properties:
            - "milestones": A list of 2-4 achievements, flow state moments, breakthroughs, or key reflective milestones.
            - "actionableVectors": A list of 2-4 actionable suggestions, vectors, mindful steps, or executive-coaching-style questions.
            - "emotionalTone": A list of 2-3 deep, highly articulate insights into the user's emotional state or underlying psychological dynamics.
            
            ${refinement?.let { "REFINEMENT INSTRUCTION: $it" } ?: ""}
            
            Journal Entries for $date:
            $notesBlock
        """.trimIndent()

        val response = model.generateContent(prompt)
        val jsonText = response.text ?: throw Exception("Gemini returned empty text")

        return parseSummaryJson(jsonText)
    }

    suspend fun searchTopicAcrossDates(notes: List<Note>, query: String): String {
        val notesContext = notes.mapIndexed { index, note ->
            "Date: ${note.dateString} - Entry ${index + 1}:\n\"${note.content}\""
        }.joinToString("\n\n")

        val prompt = """
            You are a Sophisticated Confidant. Synthesize a coherent summary from the user's journal entries that details: "$query".
            If the history does not contain this information, state that politely.
            
            Journal logs:
            $notesContext
        """.trimIndent()

        return model.generateContent(prompt).text ?: "No insight could be extracted."
    }

    private fun parseSummaryJson(jsonText: String): AISummary {
        val json = Json.parseToJsonElement(jsonText).jsonObject
        val milestones = json["milestones"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val actionableVectors = json["actionableVectors"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
        val emotionalTone = json["emotionalTone"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()

        return AISummary(milestones, actionableVectors, emotionalTone)
    }
}
