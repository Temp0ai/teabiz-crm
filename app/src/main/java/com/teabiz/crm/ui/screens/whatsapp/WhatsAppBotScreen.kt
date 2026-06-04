package com.teabiz.crm.ui.screens.whatsapp

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.WhatsAppBotViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppBotScreen(
    viewModel: WhatsAppBotViewModel,
    onBack: () -> Unit
) {
    val isRunning by viewModel.isRunning.collectAsState()
    val autoReplyEnabled by viewModel.autoReplyEnabled.collectAsState()
    val welcomeMessage by viewModel.welcomeMessage.collectAsState()
    val conversationHistory by viewModel.conversationHistory.collectAsState()
    val botStatus by viewModel.botStatus.collectAsState()
    val productRecommendations by viewModel.productRecommendations.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = com.teabiz.crm.R.drawable.ic_whatsapp),
                            contentDescription = "WhatsApp",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp Bot")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = !showSettings }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
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
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SmartToy, contentDescription = null, tint = Color(0xFF25D366), modifier = Modifier.size(28.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Bot Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Switch(
                                checked = isRunning,
                                onCheckedChange = { viewModel.toggleBot() },
                                colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF25D366))
                            )
                        }

                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (isRunning) Color(0xFF25D366).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (isRunning) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isRunning) Color(0xFF25D366) else Color.Gray
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (isRunning) "Bot is active & replying to messages" else "Bot is stopped",
                                    color = if (isRunning) Color(0xFF25D366) else Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (botStatus.isNotBlank()) {
                            Text(botStatus, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            if (showSettings) {
                item {
                    BotSettingsCard(
                        autoReplyEnabled = autoReplyEnabled,
                        onAutoReplyToggle = { viewModel.setAutoReply(it) },
                        welcomeMessage = welcomeMessage,
                        onWelcomeMessageChange = { viewModel.setWelcomeMessage(it) },
                        onSave = { viewModel.saveSettings() }
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = TeaGreen.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TeaGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Auto-Reply Rules", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Text("The bot automatically responds to incoming WhatsApp messages using Gemini AI:", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

                        productRecommendations.forEach { rule ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = PremixGold.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    rule,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PremixGold
                                )
                            }
                        }
                    }
                }
            }

            if (conversationHistory.isNotEmpty()) {
                item {
                    Text("Recent Conversations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(conversationHistory) { conversation ->
                    ConversationCard(conversation)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No conversations yet", color = Color.Gray)
                            Text("Bot will start replying when messages arrive", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotSettingsCard(
    autoReplyEnabled: Boolean,
    onAutoReplyToggle: (Boolean) -> Unit,
    welcomeMessage: String,
    onWelcomeMessageChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Bot Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-Reply to Messages")
                Switch(
                    checked = autoReplyEnabled,
                    onCheckedChange = onAutoReplyToggle,
                    colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFF25D366))
                )
            }

            OutlinedTextField(
                value = welcomeMessage,
                onValueChange = onWelcomeMessageChange,
                label = { Text("Welcome Message") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Text("Placeholders: {name}, {product}, {company}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save Settings")
            }
        }
    }
}

@Composable
fun ConversationCard(conversation: WhatsAppBotViewModel.Conversation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(conversation.contactName, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(conversation.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Surface(shape = MaterialTheme.shapes.extraSmall, color = Color.LightGray.copy(alpha = 0.3f)) {
                Text(
                    "User: ${conversation.userMessage}",
                    modifier = Modifier.padding(6.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Surface(shape = MaterialTheme.shapes.extraSmall, color = Color(0xFF25D366).copy(alpha = 0.1f)) {
                Text(
                    "Bot: ${conversation.botReply}",
                    modifier = Modifier.padding(6.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF25D366)
                )
            }
        }
    }
}
