package com.teabiz.crm.ui.screens.whatsapp

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teabiz.crm.data.remote.WhatsAppCatalogOfferSender.CatalogProduct
import com.teabiz.crm.ui.viewmodel.WhatsAppOfferViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppOfferScreen(
    onBack: () -> Unit,
    viewModel: WhatsAppOfferViewModel = hiltViewModel()
) {
    val catalogProducts by viewModel.catalogProducts.collectAsState()
    val generatedOffer by viewModel.generatedOffer.collectAsState()
    val bulkMessages by viewModel.bulkMessages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Catalog", "Create Offer", "Edit Offer", "Bulk Send")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalog & Offers") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF25D366))
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(statusMessage)
                    }
                }
            } else {
                when (selectedTab) {
                    0 -> CatalogTab(catalogProducts, viewModel)
                    1 -> CreateOfferTab(viewModel)
                    2 -> EditOfferTab(generatedOffer, viewModel)
                    3 -> BulkSendTab(bulkMessages, viewModel)
                }
            }
        }
    }
}

@Composable
private fun CatalogTab(
    products: List<com.teabiz.crm.data.remote.WhatsAppCatalogOfferSender.CatalogProduct>,
    viewModel: WhatsAppOfferViewModel
) {
    val context = LocalContext.current
    var phone by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Fetch WhatsApp Business Catalog", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Business Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            placeholder = { Text("917020134619") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Button(
            onClick = { viewModel.fetchCatalog(phone) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fetch Catalog")
        }

        if (products.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("${products.size} Products Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            products.forEach { product ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.Bold)
                            Text(product.price, color = Color(0xFF25D366))
                            if (product.description.isNotBlank()) {
                                Text(product.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/c/${phone.replace(Regex("[^0-9]"), "")}"))
                            context.startActivity(intent)
                        }) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "View", tint = Color(0xFF25D366))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateOfferTab(viewModel: WhatsAppOfferViewModel) {
    val context = LocalContext.current
    val generatedOffer by viewModel.generatedOffer.collectAsState()
    
    var product by remember { mutableStateOf("Tea Premix") }
    var discount by remember { mutableStateOf("10%") }
    var catalogLink by remember { mutableStateOf("https://wa.me/c/917020134619") }
    var gmbAddress by remember { mutableStateOf("Arihant Enterprises Mfg/ Export tea & coffee premix") }
    var phoneNumber by remember { mutableStateOf("917020134619") }
    var language by remember { mutableStateOf("English") }
    var tone by remember { mutableStateOf("Professional") }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine", "Tea Powder", "Coffee Powder")
    val languages = listOf("English", "Hindi", "Marathi")
    val tones = listOf("Professional", "Friendly", "Urgent", "Casual")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Create AI Offer Message", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownField("Product", products, product) { product = it }
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = discount,
            onValueChange = { discount = it },
            label = { Text("Discount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = catalogLink,
            onValueChange = { catalogLink = it },
            label = { Text("Catalog Link") },
            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = gmbAddress,
            onValueChange = { gmbAddress = it },
            label = { Text("GMB Address") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownField("Language", languages, language, Modifier.weight(1f)) { language = it }
            DropdownField("Tone", tones, tone, Modifier.weight(1f)) { tone = it }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.generateOffer(product, discount, catalogLink, gmbAddress, phoneNumber, language, tone) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Offer with AI")
        }

        generatedOffer?.let { offer ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366).copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Generated Offer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(offer.message)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
                                val url = "https://wa.me/$cleanPhone?text=${Uri.encode(offer.message)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Send")
                        }
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Offer", offer.message)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Copied!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditOfferTab(
    offer: com.teabiz.crm.data.remote.WhatsAppCatalogOfferSender.OfferMessage?,
    viewModel: WhatsAppOfferViewModel
) {
    val context = LocalContext.current
    var editedMessage by remember(offer) { mutableStateOf(offer?.message ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Edit AI Generated Offer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (offer == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No offer generated yet", color = Color.Gray)
                    Text("Go to 'Create Offer' tab to generate one", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            offer.title.let { title ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366).copy(alpha = 0.1f))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Title: $title", fontWeight = FontWeight.Bold)
                        if (offer.discount.isNotBlank()) Text("Discount: ${offer.discount}")
                        if (offer.catalogLink.isNotBlank()) Text("Catalog: ${offer.catalogLink}")
                        if (offer.gmbAddress.isNotBlank()) Text("Address: ${offer.gmbAddress}")
                        if (offer.phoneNumber.isNotBlank()) Text("Phone: ${offer.phoneNumber}")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = editedMessage,
                onValueChange = { editedMessage = it },
                label = { Text("Edit Message") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 200.dp),
                maxLines = 20
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quick edit buttons
            Text("Quick Add:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { editedMessage += "\n\n🎉 *FREE DELIVERY on orders above ₹5000!*",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Free Delivery", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { editedMessage += "\n\n⏰ *Offer valid till Sunday!*",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Urgency", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { editedMessage += "\n\n📞 *Call now: ${offer.phoneNumber}*" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Phone", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { editedMessage += "\n\n📍 ${offer.gmbAddress}" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Address", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { editedMessage += "\n\n🛒 *View Catalog:* ${offer.catalogLink}" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Catalog", style = MaterialTheme.typography.labelSmall)
                }
                OutlinedButton(
                    onClick = { editedMessage += "\n\n✅ *FSSAI Certified*\n✅ *ISO Certified*" },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Trust", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preview
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Preview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(editedMessage)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val cleanPhone = offer.phoneNumber.replace(Regex("[^0-9]"), "")
                        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(editedMessage)}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send")
                }
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Offer", editedMessage)
                        clipboard.setPrimaryClip(clip)
                        android.widget.Toast.makeText(context, "Copied!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, editedMessage)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }
    }
}

@Composable
private fun BulkSendTab(
    bulkMessages: List<Triple<String, String, String>>,
    viewModel: WhatsAppOfferViewModel
) {
    val context = LocalContext.current
    val catalogProducts by viewModel.catalogProducts.collectAsState()
    var businessPhone by remember { mutableStateOf("917020134619") }
    var product by remember { mutableStateOf("Tea Premix") }
    var discount by remember { mutableStateOf("10%") }
    var catalogLink by remember { mutableStateOf("https://wa.me/c/917020134619") }
    var gmbAddress by remember { mutableStateOf("Arihant Enterprises Mfg/ Export tea & coffee premix") }
    var phoneNumber by remember { mutableStateOf("917020134619") }
    var contactText by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<CatalogProduct?>(null) }
    var includeImages by remember { mutableStateOf(true) }
    var includeAddress by remember { mutableStateOf(true) }
    var includeCatalogLink by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Bulk Send with AI Offers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Fetch catalog, AI generates offers with images & address", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Fetch Catalog Section
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366).copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Step 1: Fetch Catalog", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = businessPhone,
                    onValueChange = { businessPhone = it },
                    label = { Text("Business Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.fetchCatalog(businessPhone) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fetch Catalog Products")
                }
            }
        }

        // Show fetched products
        if (catalogProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("${catalogProducts.size} Products Found:", fontWeight = FontWeight.Bold)
            
            catalogProducts.take(6).forEach { catalogProduct ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                        selectedProduct = catalogProduct
                        product = catalogProduct.name
                        catalogLink = "https://wa.me/c/${businessPhone.replace(Regex("[^0-9]"), "")}"
                    },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedProduct == catalogProduct) Color(0xFF25D366).copy(alpha = 0.2f) else Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(catalogProduct.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            Text(catalogProduct.price, color = Color(0xFF25D366), style = MaterialTheme.typography.labelSmall)
                        }
                        if (selectedProduct == catalogProduct) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF25D366))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Offer Details
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Step 2: Offer Details", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = product,
                    onValueChange = { product = it },
                    label = { Text("Product") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it },
                    label = { Text("Discount") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = catalogLink,
                    onValueChange = { catalogLink = it },
                    label = { Text("Catalog Link") },
                    leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = gmbAddress,
                    onValueChange = { gmbAddress = it },
                    label = { Text("GMB Address") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Include Options
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Include in Message:", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = includeImages,
                        onClick = { includeImages = !includeImages },
                        label = { Text("Catalog Images") }
                    )
                    FilterChip(
                        selected = includeAddress,
                        onClick = { includeAddress = !includeAddress },
                        label = { Text("GMB Address") }
                    )
                    FilterChip(
                        selected = includeCatalogLink,
                        onClick = { includeCatalogLink = !includeCatalogLink },
                        label = { Text("Catalog Link") }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Contacts
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Step 3: Add Contacts", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = contactText,
                    onValueChange = { contactText = it },
                    label = { Text("Contacts (Name, Phone per line)") },
                    placeholder = { Text("John, 919876543210\nJane, 919876543211") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 10
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                val contacts = contactText.lines()
                    .filter { it.contains(",") }
                    .map { line ->
                        val parts = line.split(",", limit = 2)
                        Pair(parts[0].trim(), parts[1].trim())
                    }
                viewModel.generateBulkMessages(contacts, product, discount, catalogLink, gmbAddress, phoneNumber)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate AI Bulk Messages")
        }

        if (bulkMessages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("${bulkMessages.size} Messages Ready", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            bulkMessages.forEachIndexed { index, (name, phone, message) ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(name, fontWeight = FontWeight.Bold)
                                Text(phone, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }
                            Row {
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Message", message)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Copied!", android.widget.Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF7C4DFF))
                                }
                                IconButton(onClick = { viewModel.openWhatsApp(phone, message) }) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color(0xFF25D366))
                                }
                            }
                        }
                        // Show preview
                        Text(
                            message.take(100) + if (message.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        bulkMessages.forEach { (_, phone, message) ->
                            viewModel.openWhatsApp(phone, message)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Send All")
                }
                OutlinedButton(
                    onClick = {
                        val allMessages = bulkMessages.joinToString("\n\n---\n\n") { (name, phone, msg) ->
                            "To: $name ($phone)\n\n$msg"
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, allMessages)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share All"))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share All")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selected: String,
    modifier: Modifier = Modifier,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
