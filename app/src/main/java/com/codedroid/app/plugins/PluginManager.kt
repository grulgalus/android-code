package com.codedroid.app.plugins

object PluginManager {
    private val loaders = mutableListOf<IPluginLoader>()
    private val activePlugins = mutableMapOf<String, String>()

    fun registerLoader(loader: IPluginLoader) {
        loaders.add(loader)
        println("PluginManager: Úspěšně zaregistrován engine - ${loader.engineName}")
    }

    fun runPlugin(pluginId: String, filePath: String, code: String, enginePreference: String = ""): String {
        val suitableLoader = loaders.find { it.canHandle(filePath, enginePreference) }
        
        return if (suitableLoader != null) {
            val result = suitableLoader.execute(pluginId, code)
            activePlugins[pluginId] = suitableLoader.engineName
            result
        } else {
            "Error: Žádný Loader nenašel způsob, jak spustit $filePath"
        }
    }
    
    // TATO FUNKCE CHYBĚLA!
    fun getAvailableEngines(): List<String> {
        return loaders.map { it.engineName }
    }
}
