package com.lifeonline.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.model.SettlementResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * C/S 模式：与电脑端 Antigravity/FastAPI Agent 守护进程通信的客户端
 */
class DaemonClient(private val context: Context) {

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("daemon_settings", Context.MODE_PRIVATE)

    // 默认通过 adb reverse 直连 localhost:8080，也可输入电脑内网 IP (如 192.168.x.x:8080)
    var serverUrl: String
        get() = prefs.getString("server_url", "http://127.0.0.1:8080") ?: "http://127.0.0.1:8080"
        set(value) = prefs.edit().putString("server_url", value).apply()

    suspend fun isServerOnline(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                response.isSuccessful
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun genesis(background: String): CharacterGenesisResult? = withContext(Dispatchers.IO) {
        try {
            val url = "$serverUrl/api/genesis"
            val body = gson.toJson(mapOf("background" to background))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val root = gson.fromJson(respStr, JsonObject::class.java)
                    val genesisJson = root.getAsJsonObject("genesisResult")
                    return@withContext gson.fromJson(genesisJson, CharacterGenesisResult::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    suspend fun settle(
        dateStr: String,
        rawInput: String,
        stepCount: Int,
        tasks: List<String>
    ): SettlementResult? = withContext(Dispatchers.IO) {
        try {
            val url = "$serverUrl/api/settle"
            val map = mapOf(
                "date" to dateStr,
                "rawInput" to rawInput,
                "stepCount" to stepCount,
                "completedTasks" to tasks
            )
            val body = gson.toJson(map).toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val respStr = response.body?.string() ?: ""
                    val root = gson.fromJson(respStr, JsonObject::class.java)
                    val settleJson = root.getAsJsonObject("settlement")
                    return@withContext gson.fromJson(settleJson, SettlementResult::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}
