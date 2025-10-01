package digital.tonima.core.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.paulrybitskyi.hiltbinder.BindType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@BindType(installIn = BindType.Component.SINGLETON, to = AppPreferencesRepository::class)
class AppPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferencesRepository {

    private object PreferencesKeys {
        val GLOBAL_ALARM_ENABLED = booleanPreferencesKey("global_alarm_enabled")
        val DISABLED_EVENT_IDS = stringSetPreferencesKey("disabled_event_ids")
        val AUTOSTART_SUGGESTION_DISMISSED = booleanPreferencesKey("autostart_suggestion_dismissed")
    }

    override fun isGlobalAlarmEnabled(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.GLOBAL_ALARM_ENABLED] ?: true
            }
    }

    override suspend fun setGlobalAlarmEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GLOBAL_ALARM_ENABLED] = enabled
        }
    }

    override fun getDisabledEventIds(): Flow<Set<String>> {
        return context.dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.DISABLED_EVENT_IDS] ?: emptySet()
            }
    }
    override suspend fun setDisabledEventIds(ids: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISABLED_EVENT_IDS] = ids
        }
    }

    override fun getAutostartSuggestionDismissed(): Flow<Boolean> {
        return context.dataStore.data
            .map { preferences ->
                preferences[PreferencesKeys.AUTOSTART_SUGGESTION_DISMISSED] ?: false
            }
    }

    override suspend fun setAutostartSuggestionDismissed(dismissed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTOSTART_SUGGESTION_DISMISSED] = dismissed
        }
    }
}
