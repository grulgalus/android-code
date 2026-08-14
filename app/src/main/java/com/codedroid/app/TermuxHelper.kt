package com.codedroid.app

import android.content.Context
import android.content.Intent

object TermuxHelper {
    fun runCommand(context: Context, command: String) {
        val intent = Intent("com.termux.RUN_COMMAND")
        intent.setClassName("com.termux", "com.termux.app.RunCommandService")
        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash")
        intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
        intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", "/data/data/com.termux/files/home")
        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
        
        try {
            context.startService(intent)
        } catch (e: Exception) {
            println("Chyba Termux integrace: Je Termux nainstalován a povoleno RUN_COMMAND?")
        }
    }
}
