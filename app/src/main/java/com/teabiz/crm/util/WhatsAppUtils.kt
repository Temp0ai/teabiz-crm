package com.teabiz.crm.util

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppUtils {

    fun openWhatsApp(context: Context, phone: String, message: String = "") {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val url = if (message.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
        } else {
            "https://api.whatsapp.com/send?phone=$cleanPhone"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun openWhatsAppBusiness(context: Context, phone: String, message: String = "") {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val url = if (message.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
        } else {
            "https://api.whatsapp.com/send?phone=$cleanPhone"
        }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.whatsapp.w4b")
        context.startActivity(intent)
    }

    fun shareOnWhatsApp(context: Context, phone: String, message: String) {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
            setPackage("com.whatsapp.w4b")
        }
        context.startActivity(intent)
    }
}
