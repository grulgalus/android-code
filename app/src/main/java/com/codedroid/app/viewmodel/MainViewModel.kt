package com.codedroid.app.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
    var codeText by mutableStateOf("// CodeDroid X Ready!\n")
        private set
    var currentFilePath by mutableStateOf("Nedefinováno")
        private set
    var terminalLogs by mutableStateOf(listOf("> CodeDroid X inicializováno [${time()}]"))
        private set

    fun updateActivePanel(panel: Panel) { activePanel = panel }
    fun updateCode(newCode: String) { codeText = newCode }
    fun appendCode(code: String) { codeText += "\n$code" }
    fun logToTerminal(msg: String) { terminalLogs = terminalLogs + "[${time()}] $msg" }

    fun loadFile(path: String) {
        currentFilePath = path
        codeText = com.codedroid.app.FileHelper.readFile(path)
        logToTerminal("Otevřen soubor: $path")
    }

    // NOVÉ: Otevření souboru přímo z Android Systému (když na něj klikneš)
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
            codeText = content
            logToTerminal("Otevřen soubor z OS: $name")
        } catch (e: Exception) {
            logToTerminal("Chyba při otevírání z OS: ${e.message}")
        }
    }

    fun saveCurrentFile() {
        if (currentFilePath != "Nedefinováno") {
            if (com.codedroid.app.FileHelper.saveFile(currentFilePath, codeText)) logToTerminal("ÚLOŽENO: $currentFilePath")
            else logToTerminal("CHYBA: Nepodařilo se uložit!")
        }
    }

    fun runCurrentFile(context: Context) {
        if (currentFilePath == "Nedefinováno") return
        saveCurrentFile()
        logToTerminal("SPOUŠTÍM: $currentFilePath")
        val cmd = when {
            currentFilePath.endsWith(".py") -> "python $currentFilePath"
            currentFilePath.endsWith(".sh") -> "bash $currentFilePath"
            currentFilePath.endsWith(".js") -> "node $currentFilePath"
            else -> "cat $currentFilePath"
        }
        com.codedroid.app.TermuxHelper.runCommand(context, cmd)
    }

    private fun time(): String = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
}
