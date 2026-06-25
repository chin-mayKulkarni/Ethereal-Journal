package com.diary.ai.domain.usecase

import com.diary.ai.domain.repository.DiaryRepository

class PerformSemanticSearchUseCase(private val repository: DiaryRepository) {
    suspend operator fun invoke(query: String, userId: String): String {
        return repository.searchNotes(query, userId)
    }
}
