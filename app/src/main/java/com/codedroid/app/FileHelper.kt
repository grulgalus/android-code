package com.codedroid.app

import java.io.File

data class FileItem(val name: String, val path: String, val isDirectory: Boolean)

object FileHelper {
    // Pro začátek načteme složku dokumentů, kam má appka vždy přístup
    fun getFilesInDir(path: String): List<FileItem> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        return dir.listFiles()?.map {
            FileItem(it.name, it.absolutePath, it.isDirectory)
        }?.sortedWith(compareBy({ !it.isDirectory }, { it.name })) ?: emptyList()
    }

    fun readFile(path: String): String {
        return try {
            File(path).readText()
        } catch (e: Exception) {
            "// Chyba při čtení souboru: ${e.message}"
        }
    }
}
