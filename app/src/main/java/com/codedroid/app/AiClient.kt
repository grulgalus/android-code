package com.codedroid.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AiClient {
    suspend fun queryOpenRouter(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        // TADY JE FIX PRO 401: Odstraníme všechny případné mezery a znaky \n na začátku a konci
        val cleanKey = apiKey.trim()
        
        if (cleanKey.isBlank()) return@withContext "// CHYBA: Zadejte API Klíč v nastavení AI!"

        try {
            val url = URL("https://openrouter.ai/api/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $cleanKey")
            conn.setRequestProperty("HTTP-Referer", "https://github.com/grulgalus/android-code")
            conn.doOutput = true

            // TADY JE FIX PRO 400: Použijeme jiný spolehlivý model, který je zdarma
            val body = JSONObject().apply {
                put("model", "google/gemini-2.0-flash-exp:free") 
                put("messages", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }))
            }

            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else {
                // Přidáme i chybovou hlášku přímo z těla odpovědi, abychom přesně věděli, co se OpenRouteru nelíbí
                val errorBody = conn.errorStream?.bufferedReader()?.readText() ?: "Žádný detajl"
                "// CHYBA API: ${conn.responseMessage} (Kód: ${conn.responseCode})\n// Detail: $errorBody"
            }
        } catch (e: Exception) {
            "// CHYBA SÍTĚ: ${e.localizedMessage}"
        }
    }
}
