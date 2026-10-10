package com.linguakey.app.data.api

import com.linguakey.app.data.security.SecureStorage
import com.linguakey.app.domain.model.ProfileId
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiTranslationRepository(private val storage: SecureStorage) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    fun translate(sourceText: String, profile: ProfileId, intensity: Int): Result<String> {
        val apiKey = storage.getApiKey()
        val model = storage.getModelId()
        val onlyFree = storage.isOnlyFreeMode()

        // 1. Verificación obligatoria de Modo de Coste Cero
        if (onlyFree && !isModelFree(model)) {
            return Result.failure(
                SecurityException("Bloqueado por el Modo de Coste Cero: El modelo '$model' no está certificado como 0$.")
            )
        }

        val systemPrompt = profile.getSystemPrompt(intensity, storage.getCustomInstructions())

        // 2. Construir cuerpo JSON compatible con OpenAI / OpenRouter
        val jsonBody = JSONObject().apply {
            put("model", model)
            put("temperature", 0.3)
            put("max_tokens", 300)
            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", sourceText)
                })
            }
            put("messages", messages)
        }

        val requestBuilder = Request.Builder()
            .url(storage.getBaseUrl() + "/chat/completions")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .header("HTTP-Referer", "https://linguakey.app")
            .header("X-Title", "LinguaKey Android Keyboard")

        if (apiKey.isNotBlank()) {
            requestBuilder.header("Authorization", "Bearer $apiKey")
        }

        return try {
            val response = client.newCall(requestBuilder.build()).execute()
            val raw = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(raw).optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (e: Exception) {
                    "HTTP ${response.code}"
                }
                return Result.failure(IOException(errorMsg))
            }

            val responseJson = JSONObject(raw)
            val content = responseJson.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")?.trim() ?: ""

            if (content.isBlank()) {
                Result.failure(IllegalStateException("Respuesta vacía del proveedor."))
            } else {
                Result.success(cleanTranslation(content))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isModelFree(modelId: String): Boolean {
        return modelId.endsWith(":free") || modelId.contains("free", ignoreCase = true)
    }

    private fun cleanTranslation(text: String): String {
        var clean = text.trim()
        if (clean.startsWith(""") && clean.endsWith(""")) {
            clean = clean.substring(1, clean.length - 1).trim()
        }
        return clean
    }
}
