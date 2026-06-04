package com.teabiz.crm.ui.screens.media

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.teabiz.crm.ui.viewmodel.AiMediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMediaScreen(
    onBack: () -> Unit,
    viewModel: AiMediaViewModel = hiltViewModel()
) {
    val imageAnalysis by viewModel.imageAnalysis.collectAsState()
    val videoConcept by viewModel.videoConcept.collectAsState()
    val socialPost by viewModel.socialPost.collectAsState()
    val productDescription by viewModel.productDescription.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Image Analysis", "Video Ideas", "Social Posts", "Descriptions")

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.analyzeImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Media Generator") },
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
                    0 -> ImageAnalysisTab(imageAnalysis, imagePickerLauncher)
                    1 -> VideoIdeaTab(videoConcept) { product, platform, duration, style ->
                        viewModel.generateVideoConcept(product, platform, duration, style)
                    }
                    2 -> SocialPostTab(socialPost) { product, platform, occasion ->
                        viewModel.generateSocialMediaPost(product, platform, occasion)
                    }
                    3 -> DescriptionTab(productDescription) { product ->
                        viewModel.generateProductDescription(product)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageAnalysisTab(
    analysis: com.teabiz.crm.data.remote.AiMediaGenerator.ImageAnalysis?,
    imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
        ) {
            Icon(Icons.Default.Image, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Upload Image for Analysis")
        }

        Spacer(modifier = Modifier.height(16.dp))

        analysis?.let { result ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(result.description)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Objects Detected", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(result.objects.joinToString(", "))

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Mood", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(result.mood)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Suggested Caption", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(result.caption)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Hashtags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(result.hashtags.joinToString(" "))

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Marketing Suggestions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    result.suggestions.forEach { suggestion ->
                        Text("• $suggestion")
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoIdeaTab(
    concept: com.teabiz.crm.data.remote.AiMediaGenerator.VideoConcept?,
    onGenerate: (String, String, String, String) -> Unit
) {
    var product by remember { mutableStateOf("Tea Premix") }
    var platform by remember { mutableStateOf("Instagram Reels") }
    var duration by remember { mutableStateOf("30 seconds") }
    var style by remember { mutableStateOf("Professional") }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine", "Tea Powder", "Coffee Powder")
    val platforms = listOf("Instagram Reels", "YouTube Shorts", "Facebook Video", "TikTok", "LinkedIn")
    val durations = listOf("15 seconds", "30 seconds", "60 seconds", "90 seconds")
    val styles = listOf("Professional", "Casual", "Humorous", "Educational", "Promotional")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Generate Video Concepts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownMenuField("Product", products, product) { product = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuField("Platform", platforms, platform) { platform = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuField("Duration", durations, duration) { duration = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuField("Style", styles, style) { style = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onGenerate(product, platform, duration, style) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))
        ) {
            Icon(Icons.Default.VideoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Video Concept")
        }

        concept?.let { c ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(c.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(c.description)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Script", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    c.script.forEachIndexed { index, scene ->
                        Text("${index + 1}. $scene")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Visual Cues", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    c.visualCues.forEach { cue ->
                        Text("• $cue")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Music: ${c.musicSuggestion}")
                    Text("Captions: ${c.captions}")
                    Text("Hashtags: ${c.hashtags.joinToString(" ")}")
                }
            }
        }
    }
}

@Composable
private fun SocialPostTab(
    post: com.teabiz.crm.data.remote.AiMediaGenerator.GeneratedContent?,
    onGenerate: (String, String, String) -> Unit
) {
    var product by remember { mutableStateOf("Tea Premix") }
    var platform by remember { mutableStateOf("Instagram") }
    var occasion by remember { mutableStateOf("General") }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine")
    val platforms = listOf("Instagram", "Facebook", "LinkedIn", "Twitter")
    val occasions = listOf("General", "Festival", "New Year", "Summer", "Winter", "Diwali", "Christmas")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Generate Social Media Posts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownMenuField("Product", products, product) { product = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuField("Platform", platforms, platform) { platform = it }
        Spacer(modifier = Modifier.height(8.dp))
        DropdownMenuField("Occasion", occasions, occasion) { occasion = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onGenerate(product, platform, occasion) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
        ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Social Post")
        }

        post?.let { p ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Generated Post", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(p.content)
                }
            }
        }
    }
}

@Composable
private fun DescriptionTab(
    description: String,
    onGenerate: (String) -> Unit
) {
    var product by remember { mutableStateOf("Tea Premix") }

    val products = listOf("Tea Premix", "Coffee Premix", "Tea Vending Machine", "Coffee Vending Machine", "Tea Powder", "Coffee Powder")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Generate Product Descriptions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        DropdownMenuField("Product", products, product) { product = it }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onGenerate(product) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Icon(Icons.Default.Description, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Description")
        }

        if (description.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Generated Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(description)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownMenuField(
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
