package com.teabiz.crm.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

object ProductCatalog {

    data class CatalogProduct(
        val name: String,
        val description: String,
        val price: String,
        val features: List<String> = emptyList()
    )

    fun getDefaultProducts(): List<CatalogProduct> = listOf(
        CatalogProduct(
            name = "Tea Premix",
            description = "Instant masala chai premix powder",
            price = "Rs.150/pack",
            features = listOf("No sugar added", "Premium Assam tea", "Natural spices", "Just add hot water")
        ),
        CatalogProduct(
            name = "Coffee Premix",
            description = "Instant coffee premix with milk",
            price = "Rs.180/pack",
            features = listOf("Rich coffee taste", "Creamy texture", "Instant preparation", "No sugar version available")
        ),
        CatalogProduct(
            name = "Tea Vending Machine",
            description = "Automatic tea vending machine for offices",
            price = "Rs.35,000",
            features = listOf("2-liter capacity", "Auto dispensing", "Easy cleaning", "One-year warranty")
        ),
        CatalogProduct(
            name = "Coffee Vending Machine",
            description = "Professional coffee vending machine",
            price = "Rs.40,000",
            features = listOf("3-liter capacity", "Hot & cold options", "Digital display", "2-year warranty")
        ),
        CatalogProduct(
            name = "2-Option Vending Machine",
            description = "Tea + Coffee combo vending machine",
            price = "Rs.55,000",
            features = listOf("Tea & Coffee both", "5-liter capacity", "Touch panel", "Bulk order discount")
        ),
        CatalogProduct(
            name = "Nescafe Premix",
            description = "Nescafe-compatible instant coffee premix",
            price = "Rs.160/pack",
            features = listOf("Premium quality", "Smooth taste", "Vending machine compatible", "Bulk pricing")
        )
    )

    fun generateCatalogPdf(context: Context, products: List<CatalogProduct>): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 24f
                color = Color.parseColor("#2E7D32")
                isFakeBoldText = true
            }
            val productPaint = Paint().apply {
                textSize = 16f
                color = Color.BLACK
                isFakeBoldText = true
            }
            val bodyPaint = Paint().apply {
                textSize = 11f
                color = Color.parseColor("#333333")
            }
            val pricePaint = Paint().apply {
                textSize = 14f
                color = Color.parseColor("#E65100")
                isFakeBoldText = true
            }
            val accentPaint = Paint().apply {
                color = Color.parseColor("#2E7D32")
                strokeWidth = 2f
            }
            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 1f
            }
            val featurePaint = Paint().apply {
                textSize = 10f
                color = Color.parseColor("#555555")
            }

            var y = 50f

            canvas.drawText("TeaBiz", 40f, y, titlePaint)
            y += 30f
            canvas.drawText("Product Catalog", 40f, y, titlePaint)
            y += 20f
            canvas.drawText("Tea & Coffee Vending Solutions", 40f, y, bodyPaint)
            y += 10f
            canvas.drawLine(40f, y, 555f, y, accentPaint)
            y += 40f

            for (product in products) {
                if (y > 700) {
                    document.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val newPage = document.startPage(newPageInfo)
                    val newCanvas = newPage.canvas
                    y = 50f
                    newCanvas.drawText("TeaBiz - Product Catalog (continued)", 40f, y, titlePaint)
                    y += 40f
                }

                canvas.drawText(product.name, 40f, y, productPaint)
                canvas.drawText(product.price, 400f, y, pricePaint)
                y += 22f
                canvas.drawText(product.description, 40f, y, bodyPaint)
                y += 20f

                for (feature in product.features) {
                    canvas.drawText("  • $feature", 50f, y, featurePaint)
                    y += 16f
                }

                y += 10f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 20f
            }

            val footerPaint = Paint().apply {
                textSize = 9f
                color = Color.parseColor("#999999")
            }
            canvas.drawText("Generated by TeaBiz CRM | Contact us for bulk orders", 40f, 810f, footerPaint)

            document.finishPage(page)

            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "TeaBiz")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "TeaBiz_Product_Catalog.pdf")
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
            file
        } catch (e: Exception) {
            null
        }
    }

    fun shareCatalog(context: Context, file: File) {
        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Check out our Tea & Coffee Vending Products! TeaBiz Catalog")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Product Catalog"))
    }

    fun shareProductViaWhatsApp(context: Context, product: CatalogProduct, phone: String) {
        val message = buildString {
            appendLine("🍵 *${product.name}*")
            appendLine(product.description)
            appendLine()
            appendLine("💰 Price: ${product.price}")
            appendLine()
            appendLine("✨ Features:")
            for (feature in product.features) {
                appendLine("• $feature")
            }
            appendLine()
            appendLine("📞 Contact us for orders!")
        }
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        WhatsAppUtils.openWhatsAppBusiness(context, phone, message)
    }
}
