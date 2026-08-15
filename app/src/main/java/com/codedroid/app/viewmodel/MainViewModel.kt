package com.codedroid.app.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.*
import com.codedroid.app.SettingsManager
import com.codedroid.app.AiClient

enum class Panel { EXPLORER, GIT, EXTENSIONS, AI_AGENT, SETTINGS }

data class ChatMessage(val role: String, val content: String)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val settings = SettingsManager(application)

    var activePanel by mutableStateOf(Panel.EXPLORER)
        private set
    var codeText by mutableStateOf("// CodeDroid X Ready!\n")
        private set
    var currentFilePath by mutableStateOf("Nedefinováno")
        private set
    var currentUri by mutableStateOf<Uri?>(null)
        private set
    var terminalLogs by mutableStateOf(listOf("> CodeDroid X inicializováno [${time()}]"))
        private set
        
    // Chatovací historie pro Cursor-style AI panel
    var chatHistory = mutableStateListOf<ChatMessage>()
        private set

    fun updateActivePanel(panel: Panel) { activePanel = panel }
    
    fun updateCode(newCode: String) { codeText = newCode }
    
    fun appendCode(code: String) { codeText += "\n$code" }
    
    fun logToTerminal(msg: String) { terminalLogs = terminalLogs + "[${time()}] $msg" }

    fun loadFile(path: String) {
        currentFilePath = path
        currentUri = null
        codeText = com.codedroid.app.FileHelper.readFile(path)
        logToTerminal("Otevřen soubor: $path")
    }

    fun loadFromUri(context: Context, uri: Uri) {
        try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            var name = "Soubor_z_OS"
            if (cursor != null && cursor.moveToFirst() && nameIndex != null && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
            cursor?.close()
            
            val content = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
            currentFilePath = name 
            currentUri = uri
            codeText = content
            logToTerminal("Otevřen soubor z Androidu: $name")
        } catch (e: Exception) {
            logToTerminal("Chyba při otevírání z OS: ${e.message}")
        }
    }

    fun saveCurrentFile(context: Context) {
        if (currentUri != null) {
            try {
                context.contentResolver.openOutputStream(currentUri!!)?.use { output ->
                    output.write(codeText.toByteArray())
                }
                logToTerminal("ÚLOŽENO přes Android OS: $currentFilePath")
            } catch (e: Exception) {
                logToTerminal("CHYBA ukládání URI: ${e.message}")
            }
        } else if (currentFilePath != "Nedefinováno") {
            if (com.codedroid.app.FileHelper.saveFile(currentFilePath, codeText)) {
                logToTerminal("ÚLOŽENO: $currentFilePath")
            } else {
                logToTerminal("CHYBA: Nepodařilo se uložit!")
            }
        } else {
            logToTerminal("CHYBA: Není co uložit.")
        }
    }

    fun runCurrentFile(context: Context) {
        if (currentFilePath == "Nedefinováno") return
        saveCurrentFile(context)
        logToTerminal("SPOUŠTÍM: $currentFilePath")
        val cmd = when {
            currentFilePath.endsWith(".py") -> "python \"$currentFilePath\""
            currentFilePath.endsWith(".sh") -> "bash \"$currentFilePath\""
            currentFilePath.endsWith(".js") -> "node \"$currentFilePath\""
            else -> "cat \"$currentFilePath\""
        }
        com.codedroid.app.TermuxHelper.runCommand(context, cmd)
    }

    // Skutečná AI integrace (s historií chatu)
    fun askAi(provider: String, prompt: String, apiKey: String) {
        logToTerminal("[$provider] Odesílám dotaz na server...")
        settings.apiKey = apiKey // Uložení klíče
        
        // Zapsání do chatu
        chatHistory.add(ChatMessage("user", prompt))
        chatHistory.add(ChatMessage("ai", "Generuji odpověď..."))
        
        // Asynchronní volání API
        CoroutineScope(Dispatchers.Main).launch {
            val aiResponse = AiClient.queryOpenRouter(prompt, apiKey)
            
            // Smažeme text "Generuji odpověď..."
            if (chatHistory.isNotEmpty() && chatHistory.last().role == "ai") {
                chatHistory.removeLast()
            }
            
            // Vložíme reálnou odpověď
            chatHistory.add(ChatMessage("ai", aiResponse))
            logToTerminal("[$provider] Odpověď přijata.")
        }
    }

    private fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
