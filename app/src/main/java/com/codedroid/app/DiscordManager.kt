package com.codedroid.app

import com.codedroid.app.viewmodel.MainViewModel

object DiscordManager {
    var isEnabled: Boolean = false

    // Aktualizuje status podle toho, co zrovna děláš
    fun updatePresence(viewModel: MainViewModel) {
        if (!isEnabled) return
        
        val fileName = viewModel.currentFilePath.substringAfterLast("/")
        val statusText = if (fileName == "Nedefinováno") {
            "Chiluje v CodeDroid X"
        } else {
            "Upravuje $fileName"
        }
        
        // Zde se v budoucnu napojí Discord Webhook nebo API
        viewModel.logToTerminal("[Discord RPC] Status: $statusText")
    }
}
