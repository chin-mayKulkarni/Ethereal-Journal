package com.diary.ai.domain.usecase

import com.diary.ai.domain.repository.AuthRepository

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}
