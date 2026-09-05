package com.kyvo.app.data.auth

import com.kyvo.app.domain.auth.AuthRepository
import com.kyvo.app.domain.auth.AuthSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Foundation adapter used until a production authentication provider is selected. */
class PendingAuthRepository : AuthRepository {
    private val session = MutableStateFlow<AuthSession?>(null)

    override fun observeSession(): Flow<AuthSession?> = session.asStateFlow()

    override suspend fun signOut() {
        session.value = null
    }
}

