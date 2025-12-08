package com.upet.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("upet_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        private val TOKEN = stringPreferencesKey("auth_token")
        private val ROLE = stringPreferencesKey("user_role")
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN] }
    val role: Flow<String?> = context.dataStore.data.map { it[ROLE] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN] = token
        }
    }

    suspend fun saveRole(role: String) {
        context.dataStore.edit { prefs ->
            prefs[ROLE] = role
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
