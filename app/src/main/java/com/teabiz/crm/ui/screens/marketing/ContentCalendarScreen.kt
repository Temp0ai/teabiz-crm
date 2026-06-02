package com.teabiz.crm.ui.screens.marketing

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.ContentCalendar
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.MarketingViewModel

@Composable
fun ContentCalendarScreen(
    viewModel: MarketingViewModel,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val content by viewModel.contentCalendar.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Content Calendar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = PremixGold,
                contentColor = Color.DarkGray
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }, backgroundColor = PremixGold) {
                Icon(Icons.Default.Add, contentDescription = "Create Content", tint = Color.DarkGray)
            }
        }
    ) { padding ->
        if (content.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No content scheduled", color = Color.Gray)
                    Text("Create your first post to get started", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(content) { item ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PlatformIcon(item.platform)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(item.contentType, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text(item.platform, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                }
                                ContentStatusChip(item.status)
                            }

                            if (item.caption.isNotBlank()) {
                                Text(
                                    text = item.caption.take(150) + if (item.caption.length > 150) "..." else "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            if (item.hashtags.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    item.hashtags.take(3).forEach { tag ->
                                        Surface(shape = MaterialTheme.shapes.extraSmall, color = StatusNew.copy(alpha = 0.2f)) {
                                            Text("#$tag", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = StatusNew)
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(onClick = { viewModel.deleteContent(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLost)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateContentDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { platform, caption, hashtags ->
                viewModel.addContent(
                    ContentCalendar(
                        platform = platform,
                        contentType = "Social Media Post",
                        caption = caption,
                        hashtags = hashtags.split(",").map { it.trim() },
                        status = "DRAFT"
                    )
                )
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun PlatformIcon(platform: String) {
    val (icon, color) = when (platform.lowercase()) {
        "instagram" -> Icons.Default.Instagram to Color(0xFFE4405F)
        "facebook" -> Icons.Default.Facebook to Color(0xFF1877F2)
        "linkedin" -> Icons.Default.LinkedIn to Color(0xFF0A66C2)
        "twitter" -> Icons.Default.Twitter to Color(0xFF1DA1F2)
        else -> Icons.Default.Public to Color.Gray
    }
    Icon(icon, contentDescription = platform, tint = color, modifier = Modifier.size(24.dp))
}

@Composable
fun ContentStatusChip(status: String) {
    val color = when (status) {
        "DRAFT" -> Color.Gray
        "SCHEDULED" -> StatusFollowUp
        "POSTED" -> StatusConverted
        "FAILED" -> StatusLost
        else -> Color.Gray
    }
    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.2f)) {
        Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun CreateContentDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var platform by remember { mutableStateOf("Instagram") }
    var caption by remember { mutableStateOf("") }
    var hashtags by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Content") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Platform", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Instagram", "Facebook", "LinkedIn", "Twitter").forEach { p ->
                        FilterChip(selected = platform == p, onClick = { platform = p }) { Text(p) }
                    }
                }
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text("Caption") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = hashtags,
                    onValueChange = { hashtags = it },
                    label = { Text("Hashtags (comma-separated)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(platform, caption, hashtags) }, enabled = caption.isNotBlank()) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
