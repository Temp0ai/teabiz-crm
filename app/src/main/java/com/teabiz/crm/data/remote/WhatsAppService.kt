package com.teabiz.crm.data.remote

import com.google.gson.Gson
import com.teabiz.crm.data.model.WhatsAppMessage
import com.teabiz.crm.data.model.WhatsAppCatalogItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

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

    /**
     * Anti-ban bulk messaging with smart delays and message variation.
     * Uses randomized intervals between 15-45 seconds (human-like pattern),
     * pauses during "active hours", and varies message content slightly.
     */
    suspend fun sendBulkMessages(
        messages: List<Pair<String, String>>,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
        onStatus: (String) -> Unit = {}
    ): List<WhatsAppMessage> {
        val results = mutableListOf<WhatsAppMessage>()
        val total = messages.size

        onStatus("Starting bulk send to $total contacts...")

        messages.forEachIndexed { index, (phone, message) ->
            // Add slight message variation to avoid duplicate detection
            val variedMessage = addMessageVariation(message)

            val result = sendMessage(phone, variedMessage)
            results.add(result)
            onProgress(index + 1, total)

            val statusText = if (result.status == "SENT") "Sent" else "Failed"
            onStatus("$statusText ${index + 1}/$total to $phone")

            if (index < messages.lastIndex) {
                // Anti-ban: randomized delay between 15-45 seconds (human pace)
                val baseDelay = Random.nextLong(15_000, 45_000)

                // Extra delay every 10 messages (simulates human break)
                val breakBonus = if ((index + 1) % 10 == 0) {
                    onStatus("Taking a short break after ${index + 1} messages...")
                    Random.nextLong(60_000, 120_000) // 1-2 min break
                } else 0L

                // Occasional longer pause every 25 messages (simulates activity gap)
                val activityGap = if ((index + 1) % 25 == 0) {
                    onStatus("Activity gap pause...")
                    Random.nextLong(180_000, 300_000) // 3-5 min gap
                } else 0L

                val totalDelay = baseDelay + breakBonus + activityGap
                delay(totalDelay)
            }
        }

        val sent = results.count { it.status == "SENT" }
        val failed = results.count { it.status == "FAILED" }
        onStatus("Bulk send complete: $sent sent, $failed failed out of $total")

        return results
    }

    /**
     * Adds slight variations to messages to avoid duplicate content detection.
     * Adds random spacing, punctuation changes, invisible Unicode chars.
     */
    private fun addMessageVariation(message: String): String {
        val variations = listOf(
            { msg: String -> msg.replace("!", "! ").trimEnd() },        // extra space after !
            { msg: String -> msg.replace(".", ". ").trimEnd() },        // extra space after .
            { msg: String -> msg + "\u200B" },                         // zero-width space at end
            { msg: String -> msg.replace("  ", " ") },                 // normalize double spaces
            { msg: String -> msg },                                     // no change
            { msg: String -> msg + " " },                               // trailing space
            { msg: String -> msg.replace("Hello", "Hi").replace("hello", "hi") }, // synonym
            { msg: String -> msg.replace("Thank you", "Thanks").replace("thank you", "thanks") },
        )

        return variations[Random.nextInt(variations.size)](message)
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
