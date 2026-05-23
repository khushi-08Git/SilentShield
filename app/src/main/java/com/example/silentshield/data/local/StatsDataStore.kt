package com.example.silentshield.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.silentshield.presentation.home.ShieldStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "shield_stats")

class StatsDataStore(private val context: Context) {

    private val CALLS_BLOCKED = intPreferencesKey("calls_blocked")
    private val SMS_SCANNED   = intPreferencesKey("sms_scanned")
    private val LINKS_CHECKED = intPreferencesKey("links_checked")

    val statsFlow: Flow<ShieldStats> = context.dataStore.data.map { prefs ->
        ShieldStats(
            callsBlocked = prefs[CALLS_BLOCKED] ?: 0,
            smsScanned   = prefs[SMS_SCANNED]   ?: 0,
            linksChecked = prefs[LINKS_CHECKED] ?: 0
        )
    }

    suspend fun incrementCallsBlocked() = context.dataStore.edit { prefs ->
        prefs[CALLS_BLOCKED] = (prefs[CALLS_BLOCKED] ?: 0) + 1
    }

    suspend fun incrementSmsScanned() = context.dataStore.edit { prefs ->
        prefs[SMS_SCANNED] = (prefs[SMS_SCANNED] ?: 0) + 1
    }

    suspend fun incrementLinksChecked() = context.dataStore.edit { prefs ->
        prefs[LINKS_CHECKED] = (prefs[LINKS_CHECKED] ?: 0) + 1
    }
}