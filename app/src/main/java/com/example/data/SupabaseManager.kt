package com.example.data

import com.example.BuildConfig
import com.example.api.OptimizedApiClient
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseManager {
    val client: SupabaseClient by lazy {
        val baseUrl = if (BuildConfig.SUPABASE_PROJECT_ID.isNotEmpty()) {
            "https://${BuildConfig.SUPABASE_PROJECT_ID}.supabase.co"
        } else {
            "https://dummy.supabase.co"
        }

        createSupabaseClient(
            supabaseUrl = baseUrl,
            supabaseKey = BuildConfig.SUPABASE_API_KEY
        ) {
            this.httpEngine = OkHttp.create {
                preconfigured = OptimizedApiClient.okHttpClient
            }
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
}
