package digital.tonima.core.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

interface AppPreferencesRepository {
    fun isGlobalAlarmEnabled(): Flow<Boolean>
    suspend fun setGlobalAlarmEnabled(enabled: Boolean)

    fun getDisabledEventIds(): Flow<Set<String>>
    suspend fun setDisabledEventIds(ids: Set<String>)

    fun getAutostartSuggestionDismissed(): Flow<Boolean>
    suspend fun setAutostartSuggestionDismissed(dismissed: Boolean)
}
