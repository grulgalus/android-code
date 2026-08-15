package com.codedroid.app

import android.content.Context
import android.content.Intent

object TermuxHelper {
    
    fun runCommand(context: Context, filePath: String) {
        val folderPath = filePath.substringBeforeLast("/")
        val fileName = filePath.substringAfterLast("/")
        
        // Smart Command Generátor
        val command = when {
            filePath.endsWith(".js") || filePath.endsWith(".ts") || filePath.endsWith(".jsx") || filePath.endsWith(".tsx") -> {
                // Bash logika: Skočí do složky -> Existuje package.json? -> Má "dev"? -> Má "start"? -> Jinak obyčejný node
                "cd '$folderPath' && if [ -f package.json ]; then if grep -q '\"dev\"' package.json; then npm run dev; elif grep -q '\"start\"' package.json; then npm start; else node '$fileName'; fi else node '$fileName'; fi"
            }
            filePath.endsWith(".py") -> "cd '$folderPath' && python '$fileName'"
            filePath.endsWith(".sh") -> "cd '$folderPath' && bash '$fileName'"
            else -> "cd '$folderPath' && cat '$fileName'"
        }

        val intent = Intent("com.termux.RUN_COMMAND").apply {
            setClassName("com.termux", "com.termux.app.RunCommandService")
            putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/login")
            putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-l", "-c", command))
            // 1 znamená, že Termux zůstane po spuštění otevřený
            putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "1")
        }

        try {
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
