package io.github.vladimirmi.photon.data.managers

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import java.util.*

/**
 * Developer Vladimir Mikhalev 30.05.2017
 */

class PreferencesManager(context: Context) {
    companion object {
        const val KEY_LAST_UPDATE_PREFIX = "KEY_LAST_UPDATE_"
        const val KEY_USER_ID = "KEY_USER_ID"
        const val KEY_USER_TOKEN = "KEY_USER_TOKEN"
        const val KEY_FAV_ALBUM = "KEY_FAV_ALBUM"
    }

    private val editor: SharedPreferences.Editor
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        @SuppressLint("CommitPrefEdits")
        editor = sharedPreferences.edit()
    }

    fun getLastUpdate(name: String): String {
        return sharedPreferences.getString(KEY_LAST_UPDATE_PREFIX + name, Date(0).toString())
    }

    fun saveLastUpdate(name: String, lastModified: String) {
        editor.putString(KEY_LAST_UPDATE_PREFIX + name, lastModified)
        editor.commit()
    }

    fun saveUserId(id: String) {
        editor.putString(KEY_USER_ID, id)
        editor.commit()
    }

    fun saveUserToken(token: String) {
        editor.putString(KEY_USER_TOKEN, token)
        editor.commit()
    }

    fun saveFavAlbumId(id: String) {
        editor.putString(KEY_FAV_ALBUM, id)
        editor.commit()
    }

    fun getUserId(): String {
        return sharedPreferences.getString(KEY_USER_ID, "")
    }

    fun getUserToken(): String {
        return sharedPreferences.getString(KEY_USER_TOKEN, "")
    }

    fun getUserFavAlbumId(): String {
        return sharedPreferences.getString(KEY_FAV_ALBUM, "")
    }

    fun clearUser() {
        editor.remove(KEY_USER_ID)
        editor.remove(KEY_USER_TOKEN)
        editor.commit()
    }
    fun isUserAuth() = !getUserId().isEmpty() && !getUserToken().isEmpty()
}
