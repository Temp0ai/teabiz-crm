package com.teabiz.crm.ui.screens.marketing

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.util.ProductCatalog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCatalogScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val products = remember { ProductCatalog.getDefaultProducts() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Catalog") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val file = ProductCatalog.generateCatalogPdf(context, products)
                        if (file != null) {
                            ProductCatalog.shareCatalog(context, file)
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Catalog", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TeaGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Our Products",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Share these products with your customers via WhatsApp",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                product.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                product.price,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(product.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        product.features.forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = TeaGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(feature, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val message = buildString {
                                        appendLine("🍵 *${product.name}*")
                                        appendLine(product.description)
                                        appendLine()
                                        appendLine("💰 Price: ${product.price}")
                                        appendLine()
                                        appendLine("✨ Features:")
                                        for (f in product.features) appendLine("• $f")
                                        appendLine()
                                        appendLine("📞 Contact TeaBiz for orders!")
                                    }
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, message)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Product"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                            Button(
                                onClick = {
                                    val message = buildString {
                                        appendLine("🍵 *${product.name}*")
                                        appendLine(product.description)
                                        appendLine()
                                        appendLine("💰 Price: ${product.price}")
                                        appendLine()
                                        appendLine("✨ Features:")
                                        for (f in product.features) appendLine("• $f")
                                        appendLine()
                                        appendLine("📞 Contact us for bulk orders!")
                                    }
                                    val url = "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("WhatsApp")
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val file = ProductCatalog.generateCatalogPdf(context, products)
                        if (file != null) {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PremixGold)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Full Catalog (PDF)")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
