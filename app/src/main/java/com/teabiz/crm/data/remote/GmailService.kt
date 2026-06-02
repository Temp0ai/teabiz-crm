package com.teabiz.crm.data.remote

import android.content.Context
import android.net.Uri
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import com.google.api.services.gmail.model.MessagePart
import com.teabiz.crm.data.model.EmailMessage
import com.teabiz.crm.data.model.GmailAuthState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val _authState = MutableStateFlow(GmailAuthState())
    val authState: StateFlow<GmailAuthState> = _authState

    private var gmailClient: Gmail? = null

    suspend fun authenticate(context: Context, authCode: String): GmailAuthState {
        return withContext(Dispatchers.IO) {
            try {
                val tokenResponse = GoogleAuthorizationCodeTokenRequest(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    CLIENT_ID,
                    CLIENT_SECRET,
                    authCode,
                    REDIRECT_URI
                ).execute()

                val credential = com.google.api.client.googleapis.auth.oauth2.GoogleCredential.Builder()
                    .setTransport(NetHttpTransport())
                    .setJsonFactory(GsonFactory.getDefaultInstance())
                    .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                    .build()
                    .setFromTokenResponse(tokenResponse)

                gmailClient = Gmail.Builder(
                    NetHttpTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("TeaBiz CRM").build()

                val state = GmailAuthState(
                    isAuthenticated = true,
                    accessToken = tokenResponse.accessToken,
                    refreshToken = tokenResponse.refreshToken
                )
                _authState.value = state
                state
            } catch (e: Exception) {
                val errorState = GmailAuthState(error = e.message)
                _authState.value = errorState
                errorState
            }
        }
    }

    suspend fun fetchEmails(maxResults: Int = 100): List<EmailMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val client = gmailClient ?: return@withContext emptyList()

                val response: ListMessagesResponse = client.users().messages()
                    .list("me")
                    .setMaxResults(maxResults.toLong())
                    .setQ("is:unread category:primary")
                    .execute()

                val messages = response.messages ?: return@withContext emptyList()

                messages.mapNotNull { messageRef ->
                    try {
                        val fullMessage = client.users().messages()
                            .get("me", messageRef.id)
                            .setFormat("full")
                            .execute()
                        parseEmailMessage(fullMessage)
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun searchEmails(query: String, maxResults: Int = 50): List<EmailMessage> {
        return withContext(Dispatchers.IO) {
            try {
                val client = gmailClient ?: return@withContext emptyList()

                val response = client.users().messages()
                    .list("me")
                    .setMaxResults(maxResults.toLong())
                    .setQ(query)
                    .execute()

                val messages = response.messages ?: return@withContext emptyList()

                messages.mapNotNull { messageRef ->
                    try {
                        val fullMessage = client.users().messages()
                            .get("me", messageRef.id)
                            .setFormat("full")
                            .execute()
                        parseEmailMessage(fullMessage)
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun parseEmailMessage(message: Message): EmailMessage? {
        val headers = message.payload?.headers ?: return null
        val from = headers.firstOrNull { it.name == "From" }?.value ?: ""
        val subject = headers.firstOrNull { it.name == "Subject" }?.value ?: ""
        val date = headers.firstOrNull { it.name == "Date" }?.value ?: ""

        val body = extractBody(message.payload)

        val fromName = extractNameFromEmail(from)
        val fromEmail = extractEmail(from)

        return EmailMessage(
            id = message.id ?: "",
            threadId = message.threadId ?: "",
            from = fromEmail,
            fromName = fromName,
            subject = subject,
            body = body,
            snippet = message.snippet ?: "",
            date = System.currentTimeMillis(),
            isRead = message.labelIds?.contains("UNREAD") != true,
            labels = message.labelIds?.toList() ?: emptyList()
        )
    }

    private fun extractBody(part: MessagePart?): String {
        if (part == null) return ""

        if (part.mimeType == "text/plain" && part.body?.data != null) {
            return try {
                String(Base64.getUrlDecoder().decode(part.body.data))
            } catch (e: Exception) {
                ""
            }
        }

        if (part.mimeType == "text/html" && part.body?.data != null) {
            return try {
                val html = String(Base64.getUrlDecoder().decode(part.body.data))
                stripHtml(html)
            } catch (e: Exception) {
                ""
            }
        }

        val parts = part.parts
        if (parts != null) {
            for (subPart in parts) {
                val body = extractBody(subPart)
                if (body.isNotBlank()) return body
            }
        }

        return ""
    }

    private fun stripHtml(html: String): String {
        return html.replace(Regex("<[^>]*>"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractNameFromEmail(from: String): String {
        val match = Regex("^(.*?)<").find(from)
        return match?.groupValues?.get(1)?.trim()?.replace("\"", "") ?: from.substringBefore("@")
    }

    private fun extractEmail(from: String): String {
        val match = Regex("<(.*?)>").find(from)
        return match?.groupValues?.get(1) ?: from
    }

    companion object {
        const val CLIENT_ID = ""
        const val CLIENT_SECRET = ""
        const val REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob"
        const val SCOPE = "https://www.googleapis.com/auth/gmail.readonly"
    }
}
