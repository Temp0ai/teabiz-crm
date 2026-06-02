package com.teabiz.crm.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.teabiz.crm.data.model.WhatsAppMessage
import com.teabiz.crm.data.model.WhatsAppCatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()
    private var baseUrl: String = "http://localhost:3000"

    fun setApiUrl(url: String) {
        baseUrl = url.trimEnd('/')
    }

    suspend fun sendMessage(phone: String, message: String): WhatsAppMessage {
        return withContext(Dispatchers.IO) {
            try {
                val body = gson.toJson(mapOf(
                    "phone" to phone,
                    "message" to message
                ))

                val request = Request.Builder()
                    .url("$baseUrl/send-message")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    WhatsAppMessage(
                        recipientPhone = phone,
                        message = message,
                        status = "SENT",
                        sentAt = System.currentTimeMillis()
                    )
                } else {
                    WhatsAppMessage(
                        recipientPhone = phone,
                        message = message,
                        status = "FAILED"
                    )
                }
            } catch (e: Exception) {
                WhatsAppMessage(
                    recipientPhone = phone,
                    message = message,
                    status = "FAILED"
                )
            }
        }
    }

    suspend fun sendBulkMessages(
        messages: List<Pair<String, String>>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): List<WhatsAppMessage> {
        val results = mutableListOf<WhatsAppMessage>()

        messages.forEachIndexed { index, (phone, message) ->
            val result = sendMessage(phone, message)
            results.add(result)
            onProgress(index + 1, messages.size)

            if (index < messages.lastIndex) {
                kotlinx.coroutines.delay(3000) // Rate limit: ~20 msg/min
            }
        }

        return results
    }

    suspend fun sendImageMessage(phone: String, imageUrl: String, caption: String): WhatsAppMessage {
        return withContext(Dispatchers.IO) {
            try {
                val body = gson.toJson(mapOf(
                    "phone" to phone,
                    "imageUrl" to imageUrl,
                    "caption" to caption
                ))

                val request = Request.Builder()
                    .url("$baseUrl/send-image")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()

                if (response.isSuccessful) {
                    WhatsAppMessage(
                        recipientPhone = phone,
                        message = caption,
                        mediaUrl = imageUrl,
                        status = "SENT",
                        sentAt = System.currentTimeMillis()
                    )
                } else {
                    WhatsAppMessage(
                        recipientPhone = phone,
                        message = caption,
                        mediaUrl = imageUrl,
                        status = "FAILED"
                    )
                }
            } catch (e: Exception) {
                WhatsAppMessage(
                    recipientPhone = phone,
                    message = caption,
                    mediaUrl = imageUrl,
                    status = "FAILED"
                )
            }
        }
    }

    suspend fun getCatalog(): List<WhatsAppCatalogItem> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/catalog")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: "[]"

                val type = object : com.google.gson.reflect.TypeToken<List<WhatsAppCatalogItem>>() {}.type
                gson.fromJson(responseBody, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun checkConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/status")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getQRCode(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("$baseUrl/qr")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                response.body?.string()
            } catch (e: Exception) {
                null
            }
        }
    }
}
