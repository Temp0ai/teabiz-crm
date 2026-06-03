package com.teabiz.crm.ui.screens.marketing

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.HashtagViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagGeneratorScreen(
    viewModel: HashtagViewModel,
    onBack: () -> Unit
) {
    val generatedHashtags by viewModel.generatedHashtags.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val productType by viewModel.productType.collectAsState()
    val geminiApiKey by viewModel.geminiApiKey.collectAsState()
    val context = LocalContext.current

    var showProductDropdown by remember { mutableStateOf(false) }
    var showPlatformDropdown by remember { mutableStateOf(false) }
    var showGeminiKeyInput by remember { mutableStateOf(false) }
    var geminiKeyText by remember { mutableStateOf("") }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setImageUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hashtag Generator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PremixGold,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Generate Trendy Hashtags",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "AI analyzes current trends on Instagram & Facebook for your product",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Product Type Dropdown
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Product Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Box {
                        ExposedDropdownMenuBox(
                            expanded = showProductDropdown,
                            onExpandedChange = { showProductDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = productType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Product") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProductDropdown) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                leadingIcon = { Icon(Icons.Default.LocalCafe, contentDescription = null) }
                            )
                            ExposedDropdownMenu(
                                expanded = showProductDropdown,
                                onDismissRequest = { showProductDropdown = false }
                            ) {
                                HashtagViewModel.PRODUCT_TYPES.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            viewModel.updateProductType(type)
                                            showProductDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Content Upload
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Content Upload (Optional)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (selectedImageUri != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(selectedImageUri),
                                contentDescription = "Selected content",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text("Content selected! AI will analyze it for relevant hashtags.", style = MaterialTheme.typography.bodySmall, color = TeaGreen)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .border(2.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .background(Color.LightGray.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Upload photo/video of your product", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { imagePicker.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Select Photo or Video")
                    }
                }
            }

            // Platform Selection
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Platform", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Instagram", "Facebook", "Both").forEach { platform ->
                            FilterChip(
                                selected = selectedPlatform == platform,
                                onClick = { viewModel.updatePlatform(platform) },
                                label = { Text(platform) }
                            )
                        }
                    }
                }
            }

            // Gemini API Key (Optional - for trending hashtags)
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A73E8).copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF1A73E8))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gemini AI (for Trending Hashtags)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Text("Add Gemini API key for current trending hashtags analysis", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                    if (geminiApiKey.isNotBlank()) {
                        Text("Gemini API configured", style = MaterialTheme.typography.bodySmall, color = TeaGreen)
                    } else {
                        OutlinedTextField(
                            value = geminiKeyText,
                            onValueChange = { geminiKeyText = it },
                            label = { Text("Gemini API Key (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        if (geminiKeyText.isNotBlank()) {
                            Button(
                                onClick = {
                                    viewModel.setGeminiApiKey(geminiKeyText)
                                    geminiKeyText = ""
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A73E8))
                            ) {
                                Text("Save Gemini Key")
                            }
                        }
                        Text("Get free key from: aistudio.google.com/app/apikey", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }

            // Generate Button
            Button(
                onClick = { viewModel.generateHashtags() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = PremixGold)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI Analyzing Trends...", color = Color.White)
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Trendy Hashtags")
                }
            }

            // Generated Hashtags
            if (generatedHashtags.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Generated Hashtags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("${generatedHashtags.size} tags", style = MaterialTheme.typography.labelMedium, color = PremixGold)
                        }

                        HorizontalDivider()

                        if (selectedPlatform == "Both") {
                            val instaTags = generatedHashtags.filterIndexed { i, _ -> i < generatedHashtags.size / 2 }
                            val fbTags = generatedHashtags.filterIndexed { i, _ -> i >= generatedHashtags.size / 2 }

                            Text("Instagram", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE1306C))
                            Text(
                                text = instaTags.joinToString(" ") { "#$it" },
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 24.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Facebook", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                            Text(
                                text = fbTags.joinToString(" ") { "#$it" },
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 24.sp
                            )
                        } else {
                            Text(
                                text = generatedHashtags.joinToString(" ") { "#$it" },
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 24.sp
                            )
                        }

                        HorizontalDivider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val text = generatedHashtags.joinToString(" ") { "#$it" }
                                    val clip = android.content.ClipData.newPlainText("Hashtags", text)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Hashtags copied!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy All")
                            }
                            Button(
                                onClick = {
                                    val text = generatedHashtags.joinToString(" ") { "#$it" }
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                                    }
                                    context.startActivity(android.content.Intent.createChooser(intent, "Share Hashtags"))
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
