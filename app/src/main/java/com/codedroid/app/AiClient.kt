package com.codedroid.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object AiClient {
    suspend fun queryOpenRouter(prompt: String, apiKey: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "// CHYBA: Zadejte API Klíč v nastavení AI!"

        try {
            // OpenRouter API Endpoint
            val url = URL("https://openrouter.ai/api/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            // OpenRouter vyžaduje referer
            conn.setRequestProperty("HTTP-Referer", "https://github.com/grulgalus/android-code")
            conn.doOutput = true

            // JSON Body (Posíláme Google Gemini model zdarma přes OpenRouter jako výchozí test)
            val body = JSONObject().apply {
                put("model", "google/gemini-2.5-flash-exp") 
                put("messages", JSONArray().put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                }))
            }

            // Odeslání požadavku
            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            // Čtení odpovědi
            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } else {
                "// CHYBA API: ${conn.responseMessage} (Kód: ${conn.responseCode})"
            }
        } catch (e: Exception) {
            "// CHYBA SÍTĚ: ${e.localizedMessage}"
        }
    }
}
