package com.lifeonline.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.model.DecomposedTask
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.TaskType
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.scale.HowToLiveBetterScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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

    suspend fun testApiConnection(
        testKey: String = apiKey,
        testUrl: String = baseUrl,
        testModel: String = modelName
    ): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (testKey.isBlank()) {
            return@withContext Pair(false, "API Key 不能为空，请先在上方填入 Key")
        }
        val startTime = System.currentTimeMillis()
        try {
            val url = if (testUrl.endsWith("/")) "${testUrl}chat/completions" else "$testUrl/chat/completions"
            val bodyMap = mapOf(
                "model" to testModel,
                "messages" to listOf(
                    mapOf("role" to "user", "content" to "请回复单个词：PONG")
                ),
                "max_tokens" to 20
            )
            val requestBody = gson.toJson(bodyMap).toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $testKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string()?.take(150) ?: ""
                    return@withContext Pair(false, "HTTP ${response.code} 错误: $errBody")
                }
                val respStr = response.body?.string() ?: ""
                val jsonObject = gson.fromJson(respStr, JsonObject::class.java)
                val choices = jsonObject.getAsJsonArray("choices")
                if (choices != null && choices.size() > 0) {
                    val content = choices[0].asJsonObject.getAsJsonObject("message").get("content").asString
                    val elapsed = System.currentTimeMillis() - startTime
                    return@withContext Pair(true, "验证成功！耗时: ${elapsed}ms | 模型响应: ${content.trim()}")
                }
                Pair(false, "未解析到模型回复内容")
            }
        } catch (e: Exception) {
            Pair(false, "连接异常: ${e.message ?: e.toString()}")
        }
    }

    suspend fun transcribeAudio(audioFile: File): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            throw RuntimeException("未检测到 API Key，请先在【配置】页面填入硅基流动等 API Key")
        }

        val url = if (baseUrl.contains("siliconflow")) {
            "https://api.siliconflow.cn/v1/audio/transcriptions"
        } else {
            val base = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            "${base}audio/transcriptions"
        }
        val model = if (baseUrl.contains("siliconflow")) "FunAudioLLM/SenseVoiceSmall" else "whisper-1"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", model)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/m4a".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: ""
                throw RuntimeException("语音识别服务响应异常 (${response.code}): $err")
            }
            val respStr = response.body?.string() ?: ""
            val json = gson.fromJson(respStr, JsonObject::class.java)
            json.get("text")?.asString ?: ""
        }
    }

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

    suspend fun decomposeTasks(userInput: String): List<DecomposedTask> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext HowToLiveBetterScale.createOfflineDecomposedTasks(userInput)
        }

        try {
            val userPrompt = HowToLiveBetterScale.buildDecomposePrompt(userInput)
            val jsonResponse = callLlm(HowToLiveBetterScale.SYSTEM_BENCHMARK_PROMPT, userPrompt)
            val cleanJson = extractJson(jsonResponse)

            val list = mutableListOf<DecomposedTask>()
            if (cleanJson.startsWith("[")) {
                val array = gson.fromJson(cleanJson, com.google.gson.JsonArray::class.java)
                array.forEach { elem ->
                    val obj = elem.asJsonObject
                    val title = obj.get("title")?.asString ?: ""
                    val typeStr = obj.get("taskType")?.asString ?: "DAILY"
                    val attr = obj.get("targetAttribute")?.asString ?: "VIT"
                    val exp = obj.get("expReward")?.asInt ?: 15
                    val taskType = if (typeStr == "SIDE_QUEST") TaskType.SIDE_QUEST else TaskType.DAILY
                    if (title.isNotBlank()) {
                        list.add(DecomposedTask(title, taskType, attr, exp, true))
                    }
                }
            } else if (cleanJson.startsWith("{")) {
                val root = gson.fromJson(cleanJson, JsonObject::class.java)
                for (key in listOf("tasks", "items", "data")) {
                    if (root.has(key) && root.get(key).isJsonArray) {
                        val array = root.getAsJsonArray(key)
                        array.forEach { elem ->
                            val obj = elem.asJsonObject
                            val title = obj.get("title")?.asString ?: ""
                            val typeStr = obj.get("taskType")?.asString ?: "DAILY"
                            val attr = obj.get("targetAttribute")?.asString ?: "VIT"
                            val exp = obj.get("expReward")?.asInt ?: 15
                            val taskType = if (typeStr == "SIDE_QUEST") TaskType.SIDE_QUEST else TaskType.DAILY
                            if (title.isNotBlank()) {
                                list.add(DecomposedTask(title, taskType, attr, exp, true))
                            }
                        }
                        break
                    }
                }
            }
            if (list.isNotEmpty()) list else HowToLiveBetterScale.createOfflineDecomposedTasks(userInput)
        } catch (e: Exception) {
            e.printStackTrace()
            HowToLiveBetterScale.createOfflineDecomposedTasks(userInput)
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
        var trimmed = text.trim()
        val jsonPattern = Regex("""```(?:json)?\s*([\s\S]*?)\s*```""")
        val match = jsonPattern.find(trimmed)
        if (match != null) {
            trimmed = match.groupValues[1].trim()
        }
        val firstBracket = trimmed.indexOf('[')
        val firstBrace = trimmed.indexOf('{')

        if (firstBracket != -1 && (firstBrace == -1 || firstBracket < firstBrace)) {
            val lastBracket = trimmed.lastIndexOf(']')
            if (lastBracket != -1 && lastBracket > firstBracket) {
                return trimmed.substring(firstBracket, lastBracket + 1)
            }
        }

        if (firstBrace != -1) {
            val lastBrace = trimmed.lastIndexOf('}')
            if (lastBrace != -1 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1)
            }
        }
        return trimmed
    }
}
