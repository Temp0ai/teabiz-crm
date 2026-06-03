package com.teabiz.crm.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.teabiz.crm.data.model.Lead
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object QuotationGenerator {

    data class QuotationItem(
        val name: String,
        val quantity: Int,
        val unitPrice: Double
    ) {
        val total: Double get() = quantity * unitPrice
    }

    data class Quotation(
        val id: String = "QT-${System.currentTimeMillis() % 100000}",
        val lead: Lead,
        val items: List<QuotationItem>,
        val taxPercent: Double = 18.0,
        val discount: Double = 0.0,
        val notes: String = "",
        val validDays: Int = 15
    ) {
        val subtotal: Double get() = items.sumOf { it.total }
        val discountAmount: Double get() = subtotal * discount / 100
        val taxableAmount: Double get() = subtotal - discountAmount
        val taxAmount: Double get() = taxableAmount * taxPercent / 100
        val grandTotal: Double get() = taxableAmount + taxAmount
    }

    fun generatePdf(context: Context, quotation: Quotation): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 22f
                color = Color.parseColor("#2E7D32")
                isFakeBoldText = true
            }
            val subtitlePaint = Paint().apply {
                textSize = 14f
                color = Color.parseColor("#666666")
            }
            val headerPaint = Paint().apply {
                textSize = 12f
                color = Color.BLACK
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                textSize = 11f
                color = Color.parseColor("#333333")
            }
            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 1f
            }
            val accentPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                strokeWidth = 2f
            }

            var y = 40f

            // Company Header
            canvas.drawText("TeaBiz CRM", 40f, y, titlePaint)
            y += 25f
            canvas.drawText("Tea & Coffee Vending Solutions", 40f, y, subtitlePaint)
            y += 20f
            canvas.drawText("Quotation / Estimate", 40f, y, subtitlePaint)
            y += 35f

            // Accent line
            canvas.drawLine(40f, y, 555f, y, accentPaint)
            y += 25f

            // Quotation Info
            canvas.drawText("Quotation #: ${quotation.id}", 40f, y, headerPaint)
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            canvas.drawText("Date: ${dateFormat.format(Date())}", 350f, y, bodyPaint)
            y += 20f
            canvas.drawText("Valid for: ${quotation.validDays} days", 40f, y, bodyPaint)
            y += 30f

            // Customer Info
            canvas.drawText("BILL TO:", 40f, y, headerPaint)
            y += 20f
            canvas.drawText(quotation.lead.name, 40f, y, bodyPaint)
            y += 18f
            if (quotation.lead.company.isNotBlank()) {
                canvas.drawText(quotation.lead.company, 40f, y, bodyPaint)
                y += 18f
            }
            if (quotation.lead.phone.isNotBlank()) {
                canvas.drawText("Ph: ${quotation.lead.phone}", 40f, y, bodyPaint)
                y += 18f
            }
            if (quotation.lead.email.isNotBlank()) {
                canvas.drawText("Email: ${quotation.lead.email}", 40f, y, bodyPaint)
                y += 18f
            }
            if (quotation.lead.city.isNotBlank()) {
                canvas.drawText(quotation.lead.city, 40f, y, bodyPaint)
                y += 18f
            }
            y += 20f

            // Table Header
            canvas.drawLine(40f, y, 555f, y, accentPaint)
            y += 20f
            canvas.drawText("Item", 40f, y, headerPaint)
            canvas.drawText("Qty", 300f, y, headerPaint)
            canvas.drawText("Rate", 370f, y, headerPaint)
            canvas.drawText("Amount", 460f, y, headerPaint)
            y += 20f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f

            // Items
            for (item in quotation.items) {
                canvas.drawText(item.name, 40f, y, bodyPaint)
                canvas.drawText("${item.quantity}", 310f, y, bodyPaint)
                canvas.drawText("Rs.%.0f".format(item.unitPrice), 365f, y, bodyPaint)
                canvas.drawText("Rs.%.0f".format(item.total), 450f, y, bodyPaint)
                y += 20f
            }

            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 25f

            // Totals
            val totalX = 380f
            canvas.drawText("Subtotal:", totalX, y, bodyPaint)
            canvas.drawText("Rs.%.0f".format(quotation.subtotal), 460f, y, bodyPaint)
            y += 20f

            if (quotation.discount > 0) {
                canvas.drawText("Discount (${quotation.discount}%):", totalX, y, bodyPaint)
                canvas.drawText("-Rs.%.0f".format(quotation.discountAmount), 455f, y, bodyPaint)
                y += 20f
            }

            canvas.drawText("GST (${quotation.taxPercent}%):", totalX, y, bodyPaint)
            canvas.drawText("Rs.%.0f".format(quotation.taxAmount), 460f, y, bodyPaint)
            y += 25f

            canvas.drawLine(totalX, y, 555f, y, accentPaint)
            y += 20f

            val totalPaint = Paint().apply {
                textSize = 14f
                color = Color.parseColor("#2E7D32")
                isFakeBoldText = true
            }
            canvas.drawText("TOTAL:", totalX, y, totalPaint)
            canvas.drawText("Rs.%.0f".format(quotation.grandTotal), 445f, y, totalPaint)
            y += 40f

            // Notes
            if (quotation.notes.isNotBlank()) {
                canvas.drawText("Notes:", 40f, y, headerPaint)
                y += 18f
                canvas.drawText(quotation.notes, 40f, y, bodyPaint)
                y += 25f
            }

            // Footer
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
            val footerPaint = Paint().apply {
                textSize = 9f
                color = Color.parseColor("#999999")
            }
            canvas.drawText("Generated by TeaBiz CRM | Thank you for your business!", 40f, y, footerPaint)

            document.finishPage(page)

            // Save to Downloads
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TeaBiz")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "Quotation_${quotation.id}.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            null
        }
    }
}
