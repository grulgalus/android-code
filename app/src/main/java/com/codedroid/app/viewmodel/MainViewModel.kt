package com.codedroid.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Panel { EXPLORER, GIT, EXTENSIONS, AI_AGENT, SETTINGS }

class MainViewModel : ViewModel() {
    var activePanel by mutableStateOf(Panel.EXPLORER)
        private set
        
    var codeText by mutableStateOf("// CodeDroid X Ready!\nfun main() {\n    println(\"Ahoj!\")\n}")
        private set
    
    var terminalLogs by mutableStateOf(listOf("> CodeDroid X inicializováno [${time()}]"))
        private set

    // PŘEJMENOVÁNO, aby nekolidovalo s autogenerovaným "setActivePanel"
    fun updateActivePanel(panel: Panel) {
        activePanel = panel
    }

    // Tady jsme to také upravili na private set pro čistotu
    fun updateCode(newCode: String) {
        codeText = newCode
    }

    fun appendCode(code: String) {
        codeText += "\n$code"
    }

    fun logToTerminal(msg: String) {
        terminalLogs = terminalLogs + "[${time()}] $msg"
    }

    private fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
