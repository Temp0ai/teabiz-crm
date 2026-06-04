package com.teabiz.crm.ui.screens.whatsapp

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.remote.WhatsAppCatalogFetcher
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.WhatsAppCatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppCatalogScreen(
    viewModel: WhatsAppCatalogViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val catalogProducts by viewModel.catalogProducts.collectAsState()
    val generatedMessages by viewModel.generatedMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isGeneratingMessages by viewModel.isGeneratingMessages.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val editingProductIndex by viewModel.editingProductIndex.collectAsState()
    val editedMessages by viewModel.editedMessages.collectAsState()
    var phoneNumber by remember { mutableStateOf("917020134619") }
    var showPhoneInput by remember { mutableStateOf(true) }
    var selectedTone by remember { mutableStateOf("Professional") }
    var selectedLanguage by remember { mutableStateOf("English") }
    var showToneDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.checkConnection()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.teabiz.crm.R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp Catalog")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF25D366),
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
            // Phone Input
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Fetch Catalog", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Enter WhatsApp Business phone number to fetch catalog", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                        )

                        Button(
                            onClick = {
                                viewModel.fetchCatalog(phoneNumber)
                                showPhoneInput = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = phoneNumber.isNotBlank() && !isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetching...")
                            } else {
                                Icon(Icons.Default.CloudDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Fetch Catalog")
                            }
                        }
                    }
                }
            }

            // Status
            if (statusMessage.isNotBlank()) {
                item {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (statusMessage.startsWith("Error")) StatusLost.copy(alpha = 0.1f) else TeaGreen.copy(alpha = 0.1f)
                    ) {
                        Text(
                            statusMessage,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (statusMessage.startsWith("Error")) StatusLost else TeaGreen
                        )
                    }
                }
            }

            // AI Generation Options
            if (catalogProducts.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Generate AI Messages", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = showToneDropdown,
                                        onExpandedChange = { showToneDropdown = it }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedTone,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Tone") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToneDropdown) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = showToneDropdown,
                                            onDismissRequest = { showToneDropdown = false }
                                        ) {
                                            listOf("Professional", "Friendly", "Casual", "Urgent", "Formal").forEach { tone ->
                                                DropdownMenuItem(
                                                    text = { Text(tone) },
                                                    onClick = {
                                                        selectedTone = tone
                                                        showToneDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    ExposedDropdownMenuBox(
                                        expanded = showLanguageDropdown,
                                        onExpandedChange = { showLanguageDropdown = it }
                                    ) {
                                        OutlinedTextField(
                                            value = selectedLanguage,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Language") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageDropdown) },
                                            modifier = Modifier.fillMaxWidth().menuAnchor()
                                        )
                                        ExposedDropdownMenu(
                                            expanded = showLanguageDropdown,
                                            onDismissRequest = { showLanguageDropdown = false }
                                        ) {
                                            listOf("English", "Hindi", "Marathi").forEach { lang ->
                                                DropdownMenuItem(
                                                    text = { Text(lang) },
                                                    onClick = {
                                                        selectedLanguage = lang
                                                        showLanguageDropdown = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = { viewModel.generateMessages(selectedTone, selectedLanguage) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isGeneratingMessages,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                            ) {
                                if (isGeneratingMessages) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate AI Messages for All Products")
                                }
                            }
                        }
                    }
                }
            }

            // Products List
            if (catalogProducts.isNotEmpty()) {
                item {
                    Text("Products (${catalogProducts.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                itemsIndexed(catalogProducts) { index, product ->
                    ProductCard(
                        product = product,
                        message = generatedMessages[product.name] ?: "",
                        editedMessage = editedMessages[product.name] ?: "",
                        isEditing = editingProductIndex == index,
                        onEdit = { viewModel.updateEditedMessage(product.name, it) },
                        onSave = { viewModel.saveEditedMessage(product.name) },
                        onStartEdit = { viewModel. let { } },
                        onShare = { phone ->
                            viewModel.shareProductOnWhatsApp(phone, product.name, generatedMessages[product.name] ?: "")
                        }
                    )
                }
            } else if (!isLoading && !showPhoneInput) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No catalog products found", color = Color.Gray)
                            Text("Try a different phone number", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: WhatsAppCatalogFetcher.CatalogProduct,
    message: String,
    editedMessage: String,
    isEditing: Boolean,
    onEdit: (String) -> Unit,
    onSave: () -> Unit,
    onStartEdit: () -> Unit,
    onShare: (String) -> Unit
) {
    var sharePhone by remember { mutableStateOf("") }
    var showShareDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
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
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    if (product.price.isNotBlank()) {
                        Text(product.price, style = MaterialTheme.typography.bodyMedium, color = TeaGreen, fontWeight = FontWeight.Bold)
                    }
                }
                if (product.category.isNotBlank()) {
                    Surface(shape = MaterialTheme.shapes.extraSmall, color = PremixGold.copy(alpha = 0.2f)) {
                        Text(
                            product.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = PremixGold
                        )
                    }
                }
            }

            if (product.description.isNotBlank()) {
                Text(product.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            if (message.isNotBlank()) {
                HorizontalDivider()
                Text("AI Generated Message:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                if (isEditing) {
                    OutlinedTextField(
                        value = editedMessage.ifBlank { message },
                        onValueChange = onEdit,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onSave,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                        ) {
                            Text("Save")
                        }
                        OutlinedButton(
                            onClick = onStartEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFF25D366).copy(alpha = 0.05f)
                    ) {
                        Text(
                            message,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onStartEdit,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit")
                        }
                        Button(
                            onClick = { showShareDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share on WA")
                        }
                    }
                }
            }
        }
    }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share on WhatsApp") },
            text = {
                OutlinedTextField(
                    value = sharePhone,
                    onValueChange = { sharePhone = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onShare(sharePhone)
                        showShareDialog = false
                    },
                    enabled = sharePhone.isNotBlank()
                ) { Text("Share") }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) { Text("Cancel") }
            }
        )
    }
}
