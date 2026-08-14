package com.codedroid.app.plugins

import app.cash.quickjs.QuickJs

class QuickJsLoader : IPluginLoader {
    override val engineName = "QuickJS-Official"

    override fun canHandle(filePath: String, enginePreference: String): Boolean {
        return filePath.endsWith(".js") || enginePreference == "quickjs"
    }

    override fun execute(pluginId: String, code: String): String {
        var quickJs: QuickJs? = null
        return try {
            quickJs = QuickJs.create()
            val result = quickJs.evaluate(code)
            result?.toString() ?: "QuickJS: Spuštěno bez návratové hodnoty."
        } catch (e: Exception) {
            "QuickJS Error [$pluginId]: ${e.message}"
        } finally {
            quickJs?.close()
        }
    }
}
