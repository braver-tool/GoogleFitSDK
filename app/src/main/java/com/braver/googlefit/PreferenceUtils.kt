package com.braver.googlefit

import android.content.Context
import android.content.SharedPreferences

class PreferenceUtils private constructor(context: Context) {
    private val prefManager: SharedPreferences =
        context.getSharedPreferences(PREFERENCE_MAIN, Context.MODE_PRIVATE)

    fun setStringValue(keyName: String?, value: String?) {
        prefManager.edit().putString(keyName, value).apply()
    }

    fun getStringValue(keyName: String): String? {
        return prefManager.getString(keyName, "")
    }


    fun setBooleanValue(keyName: String?, value: Boolean) {
        prefManager.edit().putBoolean(keyName, value).apply()
    }

    fun getBooleanValue(keyName: String?): Boolean {
        return prefManager.getBoolean(keyName, false)
    }


    fun removePref(context: Context) {
        val preferences: SharedPreferences =
            context.getSharedPreferences(PREFERENCE_MAIN, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

    fun removePref(`val`: String?) {
        val editor: SharedPreferences.Editor = prefManager.edit()
        editor.remove(`val`)
        editor.apply()
    }

    companion object {
        private val PREFERENCE_MAIN = "com.braver.googlefit"
        private var prefInstance: PreferenceUtils? = null

        @Synchronized
        fun getInstance(context: Context): PreferenceUtils? {
            if (prefInstance == null) {
                prefInstance = PreferenceUtils(context.applicationContext)
            }
            return prefInstance
        }
    }
}