package com.codedroid.app

import org.eclipse.jgit.api.Git
import java.io.File

object GitHelper {
    fun cloneRepo(repoUrl: String, destFolder: File, onComplete: (Boolean, String) -> Unit) {
        Thread {
            try {
                Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(destFolder)
                    .call()
                onComplete(true, "Klonování úspěšné!")
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Chyba při klonování")
            }
        }.start()
    }
}
