package com.diary.ai.data.repository

import com.diary.ai.domain.model.User
import com.diary.ai.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    override val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun signIn(user: User) {
        _currentUser.value = user
    }

    override suspend fun signOut() {
        _currentUser.value = null
    }
}
