package com.example.ambition.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.format
import org.json.JSONObject
import java.io.IOException

class AIViewModel : ViewModel() {

    private val client = OkHttpClient()

    private val _response = MutableStateFlow("")
    val response: StateFlow<String> = _response

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun getResponse(question: String, apiKey: String) {
        _loading.value = true

        val url = "https://api.openai.com/v1/chat/completions"
        val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [
                    {"role": "user", "content": "You are an AI researcher specializing in future planning and goal setting. Please provide insights on: \"$question\""}
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Error", "Failed to fetch response", e)
                _response.value = "Error: ${e.message}"
                _loading.value = false
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { body ->
                    Log.d("API response", body)

                    try {
                        val jsonObject = JSONObject(body)
                        val choices = jsonObject.getJSONArray("choices")
                        val text = choices.getJSONObject(0).getJSONObject("message").getString("content")
                        _response.value = text.trim()
                    } catch (e: Exception) {
                        _response.value = "Error parsing response"
                    }
                } ?: run {
                    _response.value = "No response received"
                }
                _loading.value = false
            }
        })
    }
}
