package com.kyvo.app.domain.auth

import kotlinx.coroutines.flow.Flow

enum class AuthProvider { Email, Google }

data class AuthSession(
    val userId: String,
    val email: String,
    val provider: AuthProvider,
)

interface AuthRepository {
    fun observeSession(): Flow<AuthSession?>
    suspend fun signOut()
}

