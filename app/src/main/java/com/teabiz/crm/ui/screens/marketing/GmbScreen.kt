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
import com.teabiz.crm.data.model.GmbPost
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.MarketingViewModel

@Composable
fun GmbScreen(
    viewModel: MarketingViewModel,
    onBack: () -> Unit
) {
    var showCreatePostDialog by remember { mutableStateOf(false) }
    val posts by viewModel.gmbPosts.collectAsState()
    val context = LocalContext.current
    var autoResponseEnabled by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Google My Business") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StatusNew,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreatePostDialog = true }, containerColor = StatusNew) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
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
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("GMB Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Text("Connect your Google My Business account to manage your local presence, respond to reviews, and create posts.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                        Button(
                            onClick = {
                                val gmbUrl = "https://business.google.com"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gmbUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNew)
                        ) {
                            Icon(Icons.Default.Business, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Google My Business")
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("AI Autoresponder", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        Text("Automatically respond to GMB reviews and questions using AI. When enabled, the AI generates professional responses for new reviews based on your business profile.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(
                                checked = autoResponseEnabled,
                                onCheckedChange = { autoResponseEnabled = it }
                            )
                            Text(
                                if (autoResponseEnabled) "Auto-response: ON" else "Auto-response: OFF",
                                modifier = Modifier.padding(top = 12.dp),
                                fontWeight = if (autoResponseEnabled) FontWeight.Bold else FontWeight.Normal,
                                color = if (autoResponseEnabled) StatusConverted else Color.Gray
                            )
                        }

                        if (autoResponseEnabled) {
                            Text(
                                "AI will generate responses to reviews based on: business name, review content, and star rating. Configure your business name in Settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            item {
                Text("Recent Posts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No posts yet", color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tap + to create your first GMB post", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            items(posts) { post ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(post.topicType.ifBlank { "Update" }, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            ContentStatusChip(post.status)
                        }
                        Text(post.summary, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }

    if (showCreatePostDialog) {
        CreateGmbPostDialog(
            onDismiss = { showCreatePostDialog = false },
            onCreate = { summary, topicType ->
                viewModel.addGmbPost(
                    GmbPost(
                        summary = summary,
                        topicType = topicType,
                        status = "DRAFT"
                    )
                )
                showCreatePostDialog = false
            }
        )
    }
}

@Composable
fun CreateGmbPostDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var summary by remember { mutableStateOf("") }
    var topicType by remember { mutableStateOf("Update") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create GMB Post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Post Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Topic Type", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Update", "Offer", "Event", "Product").forEach { type ->
                        FilterChip(selected = topicType == type, onClick = { topicType = type }) { Text(type) }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(summary, topicType) }, enabled = summary.isNotBlank()) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ContentStatusChip(status: String) {
    val color = when (status) {
        "POSTED" -> StatusConverted
        "DRAFT" -> Color.Gray
        "SCHEDULED" -> StatusFollowUp
        else -> Color.Gray
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.15f)) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
