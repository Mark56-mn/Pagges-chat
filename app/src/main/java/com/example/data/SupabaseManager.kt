package com.example.data

import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

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
            install(Postgrest)
            install(Auth)
            install(Realtime)
        }
    }
}
