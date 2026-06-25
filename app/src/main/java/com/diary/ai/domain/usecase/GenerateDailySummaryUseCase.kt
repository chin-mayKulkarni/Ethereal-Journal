package com.diary.ai.domain.usecase

import com.diary.ai.domain.model.AISummary
import com.diary.ai.domain.repository.DiaryRepository

class GenerateDailySummaryUseCase(private val repository: DiaryRepository) {
    suspend operator fun invoke(dateString: String, userId: String, refinement: String? = null): AISummary {
        return repository.generateDailySummary(dateString, userId, refinement)
    }
}
