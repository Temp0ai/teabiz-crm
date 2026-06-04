package com.teabiz.crm.ui.screens.media

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.teabiz.crm.ui.viewmodel.AiVideoViewModel
import com.teabiz.crm.util.WhatsAppUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiVideoScreen(
    onBack: () -> Unit,
    viewModel: AiVideoViewModel = hiltViewModel()
) {
    val videoContent by viewModel.videoContent.collectAsState()
    val imageContent by viewModel.imageContent.collectAsState()
    val batchVideos by viewModel.batchVideos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Video Script", "Image Ideas", "Batch Generate")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Video & Image Generator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    0 -> VideoScriptTab(videoContent, viewModel)
                    1 -> ImageIdeaTab(imageContent, viewModel)
                    2 -> BatchGenerateTab(batchVideos, viewModel)
                }
            }
        }
    }
}

@Composable
private fun VideoScriptTab(
    video: com.teabiz.crm.data.remote.AiVideoGenerator.GeneratedVideo?,
    viewModel: AiVideoViewModel
) {
    val context = LocalContext.current
    var product by remember { mutableStateOf("Tea Premix") }
    var platform by remember { mutableStateOf("Instagram Reels") }
    var duration by remember { mutableStateOf("30 seconds") }
    var style by remember { mutableStateOf("Professional") }
    var aspectRatio by remember { mutableStateOf("9:16") }
    var showShareMenu by remember { mutableStateOf(false) }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine", "Tea Powder", "Coffee Powder", "Masala Tea", "Green Tea")
    val platforms = listOf("Instagram Reels", "YouTube Shorts", "Facebook Video", "TikTok", "LinkedIn", "YouTube Long")
    val durations = listOf("15 seconds", "30 seconds", "60 seconds", "90 seconds", "2 minutes")
    val styles = listOf("Professional", "Casual", "Humorous", "Educational", "Promotional", "Luxury")
    val ratios = listOf("9:16", "1:1", "16:9")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Generate Video Script", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownField("Product", products, product) { product = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Platform", platforms, platform) { platform = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Duration", durations, duration) { duration = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Style", styles, style) { style = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Aspect Ratio", ratios, aspectRatio) { aspectRatio = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.generateVideo(product, platform, duration, style, aspectRatio) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
        ) {
            Icon(Icons.Default.VideoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Video Script")
        }

        video?.let { v ->
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title & Description
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CopyableSection("Title", v.title, context)
                    CopyableSection("Description", v.description, context)
                    CopyableSection("Duration", v.duration, context)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Full Script
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CopyableSection("Full Script", v.script, context)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Visual Guide
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CopyableSection("Visual Guide", v.visualGuide, context)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Hashtags & Music
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CopyableSection("Hashtags", v.hashtags.joinToString(" "), context)
                    CopyableSection("Music", v.musicSuggestion, context)
                    CopyableSection("Call to Action", v.callToAction, context)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Platform-specific content
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF7C4DFF).copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Platform-Specific Content", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val platformContent = when {
                        platform.contains("YouTube") -> viewModel.getYoutubeDescription()
                        platform.contains("Instagram") -> viewModel.getInstagramCaption()
                        platform.contains("LinkedIn") -> viewModel.getLinkedInPost()
                        platform.contains("Facebook") -> viewModel.getFacebookAd()
                        else -> viewModel.getWhatsAppMessage()
                    }
                    
                    CopyableSection("Optimized for $platform", platformContent, context)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Create Video Button
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366).copy(alpha = 0.1f))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Create Video Now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Use these free tools to create your video:", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.capcut.com"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VideoCameraFront, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CapCut")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.canva.com"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DesignServices, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Canva")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://clipchamp.com"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.VideoCameraFront, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clipchamp")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://invideo.io"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("InVideo AI")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Share buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showShareMenu = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
                Button(
                    onClick = { viewModel.clearContent() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }

            DropdownMenu(expanded = showShareMenu, onDismissRequest = { showShareMenu = false }) {
                DropdownMenuItem(
                    text = { Text("WhatsApp") },
                    onClick = {
                        WhatsAppUtils.openWhatsAppBusiness(context, "", viewModel.getWhatsAppMessage())
                        showShareMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Instagram") },
                    onClick = {
                        WhatsAppUtils.copyToClipboard(context, "Instagram", viewModel.getInstagramCaption())
                        showShareMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("YouTube") },
                    onClick = {
                        WhatsAppUtils.copyToClipboard(context, "YouTube", viewModel.getYoutubeDescription())
                        showShareMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("LinkedIn") },
                    onClick = {
                        WhatsAppUtils.copyToClipboard(context, "LinkedIn", viewModel.getLinkedInPost())
                        showShareMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Facebook Ad") },
                    onClick = {
                        WhatsAppUtils.copyToClipboard(context, "Facebook", viewModel.getFacebookAd())
                        showShareMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ImageIdeaTab(
    image: com.teabiz.crm.data.remote.AiVideoGenerator.GeneratedImage?,
    viewModel: AiVideoViewModel
) {
    val context = LocalContext.current
    var product by remember { mutableStateOf("Tea Premix") }
    var style by remember { mutableStateOf("Professional") }
    var aspectRatio by remember { mutableStateOf("1:1") }
    var purpose by remember { mutableStateOf("Social Media Post") }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine", "Tea Powder", "Coffee Powder")
    val styles = listOf("Professional", "Minimalist", "Vintage", "Modern", "Luxury", "Colorful")
    val ratios = listOf("1:1", "16:9", "9:16", "4:3")
    val purposes = listOf("Social Media Post", "Product Banner", "Advertisement", "Story", "Thumbnail", "Website")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Generate Image Ideas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownField("Product", products, product) { product = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Style", styles, style) { style = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Aspect Ratio", ratios, aspectRatio) { aspectRatio = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Purpose", purposes, purpose) { purpose = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.generateImage(product, style, aspectRatio, purpose) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Image Idea")
        }

        image?.let { img ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    CopyableSection("Image Prompt", img.prompt, context)
                    CopyableSection("Style", img.style, context)
                    CopyableSection("Size", "${img.width}x${img.height} px", context)
                    CopyableSection("Description", img.description, context)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Create Image With:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.craiyon.com"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Craiyon")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.stablecog.com"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Stable Cog")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://leonardo.ai"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Leonardo AI")
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bing.com/images/create"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Bing Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchGenerateTab(
    videos: List<com.teabiz.crm.data.remote.AiVideoGenerator.GeneratedVideo>,
    viewModel: AiVideoViewModel
) {
    val context = LocalContext.current
    var product by remember { mutableStateOf("Tea Premix") }
    var platform by remember { mutableStateOf("Instagram Reels") }
    var count by remember { mutableStateOf("3") }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine")
    val platforms = listOf("Instagram Reels", "YouTube Shorts", "Facebook Video")
    val counts = listOf("3", "5", "10")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Batch Generate Content", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownField("Product Category", products, product) { product = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Platform", platforms, platform) { platform = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownField("Number of Videos", counts, count) { count = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.generateBatch(listOf(product), platform, count.toIntOrNull() ?: 3) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Batch")
        }

        if (videos.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("${videos.size} videos generated:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            videos.forEachIndexed { index, video ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(video.title, fontWeight = FontWeight.Bold)
                        Text(video.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Row {
                            IconButton(onClick = {
                                WhatsAppUtils.copyToClipboard(context, "Script ${index+1}", video.script)
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF7C4DFF))
                            }
                            IconButton(onClick = {
                                WhatsAppUtils.copyToClipboard(context, "Title ${index+1}", video.title + "\n\n" + video.hashtags.joinToString(" "))
                            }) {
                                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF25D366))
                            }
                        }
                    }
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
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
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

@Composable
private fun CopyableSection(title: String, content: String, context: android.content.Context) {
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        IconButton(onClick = {
            WhatsAppUtils.copyToClipboard(context, title, content)
        }) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF7C4DFF))
        }
    }
    Text(content)
}
