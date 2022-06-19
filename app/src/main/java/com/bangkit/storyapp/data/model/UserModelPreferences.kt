package com.bangkit.storyapp.data.model

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserModelPreferences(private val dataStore: DataStore<Preferences>) {

    fun getUser(): Flow<UserModel> {
        return dataStore.data.map {
            UserModel(
                it[KEY_NAME] ?: "",
                it[KEY_EMAIL] ?: "",
                it[KEY_PASSWORD] ?: "",
                it[KEY_USERID] ?: "",
                it[KEY_TOKEN] ?: "",
                it[KEY_STATUS] ?: false
            )
        }
    }

    suspend fun saveUser(user: UserModel) {
        dataStore.edit {
            it[KEY_NAME] = user.name
            it[KEY_EMAIL] = user.email
            it[KEY_PASSWORD] = user.password
            it[KEY_USERID] = user.userId
            it[KEY_TOKEN] = user.token
            it[KEY_STATUS] = user.isLogin
        }
    }

    suspend fun logout() {
        dataStore.edit {
            it[KEY_NAME] = ""
            it[KEY_EMAIL] = ""
            it[KEY_PASSWORD] = ""
            it[KEY_USERID] = ""
            it[KEY_TOKEN] = ""
            it[KEY_STATUS] = false
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserModelPreferences? = null

        private val KEY_NAME = stringPreferencesKey("name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_USERID = stringPreferencesKey("userId")
        private val KEY_TOKEN = stringPreferencesKey("token")
        private val KEY_STATUS = booleanPreferencesKey("state")

        fun getInstance(dataStore: DataStore<Preferences>): UserModelPreferences {
            return INSTANCE ?: synchronized(this) {
                val instance = UserModelPreferences(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}