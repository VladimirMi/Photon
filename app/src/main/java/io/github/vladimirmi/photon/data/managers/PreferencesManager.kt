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
    private val KEY_PRODUCT_LAST_UPDATE = "PRODUCT_LAST_UPDATE"

    private val editor: SharedPreferences.Editor
    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        @SuppressLint("CommitPrefEdits")
        editor = sharedPreferences.edit()
    }


    fun getLastUpdate(): String = sharedPreferences.getString(KEY_PRODUCT_LAST_UPDATE, Date(0).toString())

    fun saveLastUpdate(lastModified: String) {
        editor.putString(KEY_PRODUCT_LAST_UPDATE, lastModified)
        editor.commit()
    }
}
