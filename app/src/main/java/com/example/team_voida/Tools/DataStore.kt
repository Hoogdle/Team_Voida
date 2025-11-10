package com.example.team_voida.Tools

// DataStoreManager.kt
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_TOKEN = stringPreferencesKey("user_token")
        val THEME_MODE = intPreferencesKey("theme_mode")
    }

    // Save login session
    suspend fun saveLoginSession(token: String) {
        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_TOKEN] = token
        }
    }

    // Save login session
    suspend fun setTheme(code: Int) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = code
        }
    }

    // Check if user is logged in
    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[IS_LOGGED_IN] ?: false
    }

    // Get user token
    val userToken: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[USER_TOKEN]
    }


    // Save theme preference
    suspend fun saveThemeMode(mode: Int) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    // Get theme preference
    val themeMode: Flow<Int?> = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: 0
    }

    // Logout - clear session
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}

// Usage in ViewModel
class MainViewModel(private val dataStoreManager: DataStoreManager) : ViewModel() {

    val isLoggedIn = dataStoreManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)


    val userToken = dataStoreManager.userToken
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val themeMode = dataStoreManager.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1)

    fun login(token: String) {
        viewModelScope.launch {
            dataStoreManager.saveLoginSession(token)
        }
    }

    fun logout() {
        viewModelScope.launch {
            dataStoreManager.clearSession()
        }
    }

    fun setTheme(code: Int){
        viewModelScope.launch {
            dataStoreManager.setTheme(code)
        }
    }

    // Factory for creating ViewModel with DataStoreManager
    class Factory(private val dataStoreManager: DataStoreManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(dataStoreManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

//// Usage in Composable
//@Composable
//fun App(viewModel: MainViewModel) {
//    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
//    val userName by viewModel.userName.collectAsState()
//
//    if (isLoggedIn) {
//        HomeScreen(userName = userName ?: "User")
//    } else {
//        LoginScreen(onLogin = { token, userId, name ->
//            viewModel.login(token, userId, name)
//        })
//    }
//}