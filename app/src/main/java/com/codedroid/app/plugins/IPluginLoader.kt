package com.codedroid.app.plugins

interface IPluginLoader {
    val engineName: String
    fun canHandle(filePath: String, enginePreference: String): Boolean
    fun execute(pluginId: String, code: String): String
}
