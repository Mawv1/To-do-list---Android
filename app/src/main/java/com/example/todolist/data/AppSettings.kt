package com.example.todolist.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppSettingsManager(private val context: Context) {

    // Klucze dla ustawień
    companion object {
        val SHOW_COMPLETED_TASKS = booleanPreferencesKey("show_completed_tasks")
        val SELECTED_CATEGORIES = stringSetPreferencesKey("selected_categories")
        val DEFAULT_NOTIFICATION_MINUTES = intPreferencesKey("default_notification_minutes")

        val ALL_CATEGORIES = listOf("Dom", "Praca", "Szkoła", "Inne")
    }

    // Pobieranie ustawienia pokazywania zakończonych zadań (domyślnie true - pokazuj wszystkie)
    val showCompletedTasks: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SHOW_COMPLETED_TASKS] ?: true
        }

    // Pobieranie wybranych kategorii (domyślnie wszystkie)
    val selectedCategories: Flow<Set<String>> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SELECTED_CATEGORIES] ?: ALL_CATEGORIES.toSet()
        }

    // Pobieranie domyślnego czasu powiadomienia w minutach (domyślnie 15 minut)
    val defaultNotificationMinutes: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[DEFAULT_NOTIFICATION_MINUTES] ?: 15
        }

    suspend fun updateShowCompletedTasks(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_COMPLETED_TASKS] = show
        }
    }

    suspend fun updateSelectedCategories(categories: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CATEGORIES] = categories
        }
    }

    suspend fun updateDefaultNotificationMinutes(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_NOTIFICATION_MINUTES] = minutes
        }
    }
}
