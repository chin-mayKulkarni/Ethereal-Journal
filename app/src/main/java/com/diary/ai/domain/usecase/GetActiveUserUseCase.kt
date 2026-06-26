package com.diary.ai.domain.usecase

import com.diary.ai.domain.model.User
import com.diary.ai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow

class GetActiveUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): StateFlow<User?> = repository.currentUser
}
