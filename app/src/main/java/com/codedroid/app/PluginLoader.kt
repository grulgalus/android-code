package com.codedroid.app

import app.cash.quickjs.QuickJs

object PluginLoader {
    // Toto umožní spouštět JS kód uživatele bezpečně přes QuickJS engine
    // Aplikace s tím projde na Google Play i F-Droid.
    fun executePlugin(javascriptCode: String): String {
        var quickJs: QuickJs? = null
        return try {
            quickJs = QuickJs.create()
            // Zde spustíme skript a vrátíme výsledek
            val result = quickJs.evaluate(javascriptCode)
            result?.toString() ?: "Skript proběhl, ale nic nevrátil."
        } catch (e: Exception) {
            "Chyba pluginu: ${e.message}"
        } finally {
            quickJs?.close()
        }
    }
}
