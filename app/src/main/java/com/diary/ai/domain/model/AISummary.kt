package com.diary.ai.domain.model

data class AISummary(
    val milestones: List<String>,
    val actionableVectors: List<String>,
    val emotionalTone: List<String>
)
