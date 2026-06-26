package com.diary.ai.domain.repository

import com.diary.ai.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<User?>
    suspend fun signIn(user: User)
    suspend fun signOut()
}
