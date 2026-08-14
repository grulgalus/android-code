package com.codedroid.app.plugins

import android.content.Context
import com.codedroid.app.TermuxHelper

class TermuxBashLoader(private val context: Context) : IPluginLoader {
    override val engineName = "TermuxBash-Engine"

    override fun canHandle(filePath: String, enginePreference: String): Boolean {
        return filePath.endsWith(".sh") || enginePreference == "termux"
    }

    override fun execute(pluginId: String, code: String): String {
        TermuxHelper.runCommand(context, code)
        return "TermuxBash: Příkaz odeslán do Termuxu na pozadí."
    }
}
