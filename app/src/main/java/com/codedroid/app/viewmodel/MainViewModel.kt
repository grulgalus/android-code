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
data class EditorTab(val path: String, val name: String, var content: String, var isModified: Boolean = false)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val settings = SettingsManager(application)

    var activePanel by mutableStateOf(Panel.EXPLORER)
    var terminalLogs by mutableStateOf(listOf("> CodeDroid X inicializováno [${time()}]"))
    var chatHistory = mutableStateListOf<ChatMessage>()
    
    // VS Code Systém záložek
    var openTabs = mutableStateListOf<EditorTab>()
    var activeTabIndex by mutableStateOf(-1)
    
    // Cursor Inline AI
    var isInlineAiVisible by mutableStateOf(false)

    init {
        // Výchozí prázdný soubor
        openTabs.add(EditorTab("Bez_názvu.txt", "Bez_názvu.txt", "// Zde začíná tvůj kód...\n"))
        activeTabIndex = 0
    }

    fun updateActivePanel(panel: Panel) { activePanel = panel }
    
    fun getActiveTab(): EditorTab? = if (activeTabIndex in openTabs.indices) openTabs[activeTabIndex] else null

    fun updateActiveContent(newCode: String) {
    fun appendCode(code: String) {
        val tab = getActiveTab()
        if (tab != null) {
            openTabs[activeTabIndex] = tab.copy(content = tab.content + "\n" + code, isModified = true)
        }
    }
        val tab = getActiveTab()
        if (tab != null) {
            // Aby Compose zaregistroval změnu, musíme objekt nahradit
            openTabs[activeTabIndex] = tab.copy(content = newCode, isModified = true)
        }
    }

    fun closeTab(index: Int) {
        if (index in openTabs.indices) {
            openTabs.removeAt(index)
            if (activeTabIndex >= openTabs.size) activeTabIndex = openTabs.size - 1
            if (openTabs.isEmpty()) {
                openTabs.add(EditorTab("Bez_názvu.txt", "Bez_názvu.txt", ""))
                activeTabIndex = 0
            }
        }
    }

    fun logToTerminal(msg: String) { terminalLogs = terminalLogs + "[${time()}] $msg" }

    fun loadFile(path: String) {
        val name = path.substringAfterLast("/")
        val existingIndex = openTabs.indexOfFirst { it.path == path }
        if (existingIndex != -1) {
            activeTabIndex = existingIndex
        } else {
            val content = com.codedroid.app.FileHelper.readFile(path)
            openTabs.add(EditorTab(path, name, content))
            activeTabIndex = openTabs.size - 1
        }
        logToTerminal("Otevřen soubor: $name")
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
            openTabs.add(EditorTab(name, name, content))
            activeTabIndex = openTabs.size - 1
        } catch (e: Exception) {}
    }

    fun saveCurrentFile(context: Context) {
        val tab = getActiveTab() ?: return
        if (com.codedroid.app.FileHelper.saveFile(tab.path, tab.content)) {
            openTabs[activeTabIndex] = tab.copy(isModified = false)
            logToTerminal("ÚLOŽENO: ${tab.name}")
        }
    }

    fun runCurrentFile(context: Context) {
        val tab = getActiveTab() ?: return
        saveCurrentFile(context)
        logToTerminal("SPOUŠTÍM v Termuxu: ${tab.name}")
        com.codedroid.app.TermuxHelper.runCommand(context, tab.path)
    }

    // Cursor Cmd+K (Inline úprava aktuálního souboru)
    fun runInlineAi(prompt: String) {
        val tab = getActiveTab() ?: return
        isInlineAiVisible = false
        logToTerminal("[Cursor AI] Aplikuji změny na ${tab.name}...")
        
        val fullPrompt = "Jsi Cursor AI. Uprav tento kód přesně podle instrukcí: '$prompt'. VRAŤ POUZE HOLÝ KÓD BEZ VYSVĚTLIVEK A BEZ MARKDOWN ZNAČEK (```).\nKÓD:\n${tab.content}"
        
        CoroutineScope(Dispatchers.Main).launch {
            val response = AiClient.queryOpenRouter(fullPrompt, settings.apiKey)
            // Vyčištění případných markdown zbytků od AI
            val cleanCode = response.replace("```javascript", "").replace("```js", "").replace("```python", "").replace("```", "").trim()
            updateActiveContent(cleanCode)
            logToTerminal("[Cursor AI] Kód úspěšně upraven.")
        }
    }

    fun askAi(provider: String, prompt: String, apiKey: String) {
        logToTerminal("[$provider] Odesílám dotaz...")
        settings.apiKey = apiKey 
        chatHistory.add(ChatMessage("user", prompt))
        chatHistory.add(ChatMessage("ai", "Generuji odpověď..."))
        CoroutineScope(Dispatchers.Main).launch {
            val aiResponse = AiClient.queryOpenRouter(prompt, apiKey)
            if (chatHistory.isNotEmpty() && chatHistory.last().role == "ai") chatHistory.removeLast()
            chatHistory.add(ChatMessage("ai", aiResponse))
        }
    }

    private fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
