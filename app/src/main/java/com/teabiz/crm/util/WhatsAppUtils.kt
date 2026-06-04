package com.teabiz.crm.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppUtils {

    fun openWhatsApp(context: Context, phone: String, message: String = "") {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val url = if (message.isNotBlank()) {
            "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
        } else {
            "https://wa.me/$cleanPhone"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun openWhatsAppBusiness(context: Context, phone: String, message: String = "") {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val url = if (message.isNotBlank()) {
            "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
        } else {
            "https://wa.me/$cleanPhone"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.whatsapp.w4b")
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(fallback)
        }
    }

    fun shareOnWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp.w4b")
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(fallback)
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
    }
}
