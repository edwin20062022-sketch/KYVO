package com.kyvo.app.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient

object SupabaseClientFactory {
    fun create(
        url: String,
        publishableKey: String,
        authScheme: String,
    ): SupabaseClient = createSupabaseClient(
        supabaseUrl = url,
        supabaseKey = publishableKey,
    ) {
        install(Auth) {
            scheme = authScheme
            host = "auth-callback"
            flowType = FlowType.PKCE
            alwaysAutoRefresh = true
            autoLoadFromStorage = true
        }
    }
}
