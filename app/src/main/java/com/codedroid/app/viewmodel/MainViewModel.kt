package com.codedroid.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Panel { EXPLORER, GIT, EXTENSIONS, AI_AGENT }

class MainViewModel : ViewModel() {
    var activePanel by mutableStateOf(Panel.EXPLORER)
        private set
        
    var codeText by mutableStateOf("// CodeDroid X Ready!\nfun main() {\n    println(\"Ahoj!\")\n}")
    
    var terminalLogs by mutableStateOf(listOf("> CodeDroid X inicializováno [${time()}]"))
        private set

    fun setActivePanel(panel: Panel) {
        activePanel = panel
    }

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
