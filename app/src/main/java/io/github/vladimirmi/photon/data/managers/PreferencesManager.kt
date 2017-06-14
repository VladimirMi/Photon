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
    private val KEY_LAST_UPDATE_PREFIX = "LAST_UPDATE_"

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
}
