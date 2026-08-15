package com.codedroid.app

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("codedroid_prefs", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value).apply()

    var isDiscordEnabled: Boolean
        get() = prefs.getBoolean("discord_enabled", false)
        set(value) = prefs.edit().putBoolean("discord_enabled", value).apply()
}
