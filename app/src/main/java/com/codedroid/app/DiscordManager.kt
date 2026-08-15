package com.codedroid.app

import com.codedroid.app.viewmodel.MainViewModel

object DiscordManager {
    var isEnabled: Boolean = false

    fun updatePresence(viewModel: MainViewModel) {
        if (!isEnabled) return
        
        val activeTab = viewModel.getActiveTab()
        val statusText = if (activeTab == null || activeTab.name == "Bez_názvu.txt") {
            "Chiluje v CodeDroid X"
        } else {
            "Upravuje ${activeTab.name}"
        }
        
        viewModel.logToTerminal("[Discord RPC] Status: $statusText")
    }
}
