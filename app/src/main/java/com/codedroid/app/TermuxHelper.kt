package com.codedroid.app

import android.content.Context
import android.content.Intent

object TermuxHelper {
    
    fun runCommand(context: Context, filePath: String) {
        val executable = when {
            filePath.endsWith(".js") -> "node"
            filePath.endsWith(".py") -> "python"
            filePath.endsWith(".sh") -> "bash"
            else -> "cat" // Pokud neznáme, aspoň to vypíšeme
        }

        // Extrahujeme složku a jméno souboru
        val folderPath = filePath.substringBeforeLast("/")
        val fileName = filePath.substringAfterLast("/")
        
        // Zkombinujeme příkaz: přesuneme se do složky a spustíme soubor
        val command = "cd '$folderPath' && $executable '$fileName'"

        // Odeslání speciálního Intentu přímo do Termuxu
        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setClassName("com.termux", "com.termux.app.RunCommandService")
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/login")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-l", "-c", command))
            // 1 znamená, že Termux se má po spuštění příkazu nezavírat
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "1")
        }

        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
