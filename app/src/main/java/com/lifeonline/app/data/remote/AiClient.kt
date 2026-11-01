package com.lifeonline.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.scale.HowToLiveBetterScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class AiClient(private val context: Context) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("ai_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "") ?: ""
        set(value) = prefs.edit().putString("api_key", value).apply()

    var baseUrl: String
        get() = prefs.getString("base_url", "https://api.deepseek.com/v1") ?: "https://api.deepseek.com/v1"
        set(value) = prefs.edit().putString("base_url", value).apply()

    var modelName: String
        get() = prefs.getString("model_name", "deepseek-chat") ?: "deepseek-chat"
        set(value) = prefs.edit().putString("model_name", value).apply()

    suspend fun generateCharacterGenesis(userInput: String): CharacterGenesisResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext HowToLiveBetterScale.createDefaultGenesis(userInput)
        }

        try {
            val userPrompt = HowToLiveBetterScale.buildCharacterGenesisPrompt(userInput)
            val jsonResponse = callLlm(HowToLiveBetterScale.SYSTEM_BENCHMARK_PROMPT, userPrompt)
            val cleanJson = extractJson(jsonResponse)
            gson.fromJson(cleanJson, CharacterGenesisResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            HowToLiveBetterScale.createDefaultGenesis(userInput)
        }
    }

    suspend fun generateDailySettlement(
        currentStats: UserStats,
        todayInput: String,
        stepCount: Int,
        completedTasks: List<String>
    ): SettlementResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext HowToLiveBetterScale.calculateLocalFallbackSettlement(
                todayInput, stepCount, completedTasks
            )
        }

        try {
            val userPrompt = HowToLiveBetterScale.buildSettlementPrompt(
                currentStats, todayInput, stepCount, completedTasks
            )
            val jsonResponse = callLlm(HowToLiveBetterScale.SYSTEM_BENCHMARK_PROMPT, userPrompt)
            val cleanJson = extractJson(jsonResponse)
            gson.fromJson(cleanJson, SettlementResult::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            HowToLiveBetterScale.calculateLocalFallbackSettlement(
                todayInput, stepCount, completedTasks
            )
        }
    }

    private fun callLlm(systemPrompt: String, userPrompt: String): String {
        val url = if (baseUrl.endsWith("/")) "${baseUrl}chat/completions" else "$baseUrl/chat/completions"

        val bodyMap = mapOf(
            "model" to modelName,
            "messages" to listOf(
                mapOf("role" to "system", "content" to systemPrompt),
                mapOf("role" to "user", "content" to userPrompt)
            ),
            "temperature" to 0.7
        )

        val requestBody = gson.toJson(bodyMap).toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("HTTP Error: ${response.code} ${response.message}")
            }
            val responseString = response.body?.string() ?: ""
            val jsonObject = gson.fromJson(responseString, JsonObject::class.java)
            val choices = jsonObject.getAsJsonArray("choices")
            if (choices != null && choices.size() > 0) {
                val firstChoice = choices[0].asJsonObject
                val message = firstChoice.getAsJsonObject("message")
                return message.get("content").asString
            }
            throw RuntimeException("Empty choices in response")
        }
    }

    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        val jsonPattern = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""")
        val match = jsonPattern.find(trimmed)
        if (match != null) {
            return match.groupValues[1].trim()
        }
        val firstBrace = trimmed.indexOf('{')
        val lastBrace = trimmed.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1)
        }
        return trimmed
    }
}
