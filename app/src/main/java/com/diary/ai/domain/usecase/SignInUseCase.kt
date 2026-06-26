package com.diary.ai.domain.usecase

import com.diary.ai.domain.model.User
import com.diary.ai.domain.repository.AuthRepository

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(user: User) = repository.signIn(user)
}
