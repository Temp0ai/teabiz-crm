package com.teabiz.crm.ui.screens.whatsapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.WhatsAppCatalogItem
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.WhatsAppCatalogViewModel

@Composable
fun WhatsAppCatalogScreen(
    viewModel: WhatsAppCatalogViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val catalogItems by viewModel.catalogItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkConnection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp Catalog") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color(0xFF25D366),
                contentColor = Color.White
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
                Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connection Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        if (isConnected) {
                            Surface(color = StatusConverted.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusConverted)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WhatsApp Business Connected", color = StatusConverted)
                                }
                            }
                        } else {
                            Surface(color = StatusLost.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = StatusLost)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Not Connected", color = StatusLost)
                                }
                            }
                            Text("Start the WhatsApp bridge server to access your catalog.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            if (!isLoading && catalogItems.isEmpty() && isConnected) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No catalog items found", color = Color.Gray)
                        }
                    }
                }
            }

            items(catalogItems) { item ->
                CatalogItemCard(item)
            }
        }
    }
}

@Composable
fun CatalogItemCard(item: WhatsAppCatalogItem) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${item.currency} ${item.price}", style = MaterialTheme.typography.bodyMedium, color = TeaGreen, fontWeight = FontWeight.Bold)
                }
                val availColor = if (item.availability == "IN_STOCK") StatusConverted else StatusLost
                Surface(shape = MaterialTheme.shapes.extraSmall, color = availColor.copy(alpha = 0.15f)) {
                    Text(
                        text = if (item.availability == "IN_STOCK") "In Stock" else "Out of Stock",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = availColor
                    )
                }
            }

            Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Surface(shape = MaterialTheme.shapes.extraSmall, color = PremixGold.copy(alpha = 0.2f)) {
                Text(
                    text = item.category,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = PremixGold
                )
            }

            Button(
                onClick = {
                    val url = if (item.whatsappShareUrl.isNotBlank()) {
                        item.whatsappShareUrl
                    } else {
                        "https://wa.me/?text=${Uri.encode("${item.name} - ${item.currency} ${item.price}\n${item.description}")}"
                    }
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share on WhatsApp")
            }
        }
    }
}
