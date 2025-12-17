package com.upet.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "upet_prefs")

class TokenDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("token")
        val ROLE_KEY = stringPreferencesKey("role")
        val USER_ID_KEY = stringPreferencesKey("user_id")
        val IS_WALKER_KEY = booleanPreferencesKey("is_walker")
    }

    private val dataStore = context.dataStore

    val token = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
        }
    }

    suspend fun saveRole(role: String) {
        dataStore.edit { prefs ->
            prefs[ROLE_KEY] = role
        }
    }

    suspend fun saveUserId(userId: String) {
        dataStore.edit { prefs ->
            prefs[USER_ID_KEY] = userId
        }
    }

    suspend fun saveUserType(isWalker: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_WALKER_KEY] = isWalker
        }
    }

    suspend fun getToken(): String? {
        val prefs = dataStore.data.first()
        return prefs[TOKEN_KEY]
    }

    suspend fun getRole(): String? {
        val prefs = dataStore.data.first()
        return prefs[ROLE_KEY]
    }

    suspend fun getUserId(): String? {
        val prefs = dataStore.data.first()
        return prefs[USER_ID_KEY]
    }

    suspend fun isWalker(): Boolean {
        val prefs = dataStore.data.first()
        return prefs[IS_WALKER_KEY] ?: false
    }

    suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
    
    suspend fun clearToken() {
        clear()
    }
}
