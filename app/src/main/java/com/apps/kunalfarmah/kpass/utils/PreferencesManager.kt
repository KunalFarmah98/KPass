package com.apps.kunalfarmah.kpass.utils

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.apps.kunalfarmah.kpass.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@SuppressLint("StaticFieldLeak")
object PreferencesManager {

    lateinit var context: Context
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "encrypted_preferences")

    suspend fun setData(key: String, value: String){
        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = CryptoManager.encrypt(value)
            }
        }
    }

    fun getData(key: String): Flow<String> {
        return context.dataStore.data
            .map { preferences ->
                val password = preferences[stringPreferencesKey(key)] ?: ""
                password.let {
                    if (it.isNotEmpty()) {
                        CryptoManager.decrypt(it)
                    } else {
                        ""
                    }
                }
            }
    }

}