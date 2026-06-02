package com.teabiz.crm.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val apiKey by viewModel.apiKey.collectAsState()
    val aiModel by viewModel.aiModel.collectAsState()
    val messageTone by viewModel.messageTone.collectAsState()
    val whatsappApiUrl by viewModel.whatsappApiUrl.collectAsState()
    val gmailClientId by viewModel.gmailClientId.collectAsState()
    val businessName by viewModel.businessName.collectAsState()

    var editApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var editAiModel by remember(aiModel) { mutableStateOf(aiModel) }
    var editMessageTone by remember(messageTone) { mutableStateOf(messageTone) }
    var editWhatsappUrl by remember(whatsappApiUrl) { mutableStateOf(whatsappApiUrl) }
    var editGmailClientId by remember(gmailClientId) { mutableStateOf(gmailClientId) }
    var editBusinessName by remember(businessName) { mutableStateOf(businessName) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Business Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                SettingsTextField(
                    value = editBusinessName,
                    onValueChange = { editBusinessName = it },
                    label = "Business Name",
                    icon = Icons.Default.Business,
                    placeholder = "Your Tea/Coffee Business"
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("API Configuration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                SettingsTextField(
                    value = editApiKey,
                    onValueChange = { editApiKey = it },
                    label = "OpenAI API Key",
                    icon = Icons.Default.Key,
                    placeholder = "sk-..."
                )

                SettingsTextField(
                    value = editWhatsappUrl,
                    onValueChange = { editWhatsappUrl = it },
                    label = "WhatsApp API URL",
                    icon = Icons.Default.Chat,
                    placeholder = "http://localhost:3000"
                )

                SettingsTextField(
                    value = editGmailClientId,
                    onValueChange = { editGmailClientId = it },
                    label = "Gmail Client ID",
                    icon = Icons.Default.Email,
                    placeholder = "your-client-id.apps.googleusercontent.com"
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("AI Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                SettingsTextField(
                    value = editAiModel,
                    onValueChange = { editAiModel = it },
                    label = "AI Model",
                    icon = Icons.Default.AutoAwesome,
                    placeholder = "gpt-4, gpt-3.5-turbo"
                )

                Text("Message Tone", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Professional", "Friendly", "Urgent").forEach { tone ->
                        FilterChip(
                            selected = editMessageTone == tone,
                            onClick = { editMessageTone = tone }
                        ) { Text(tone) }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                SettingsRow(icon = Icons.Default.Info, title = "App Version", subtitle = "1.0.0")
                SettingsRow(icon = Icons.Default.Security, title = "Privacy Policy", subtitle = "View policy")
                SettingsRow(icon = Icons.Default.Description, title = "Terms of Service", subtitle = "View terms")
            }
        }

        Button(
            onClick = {
                viewModel.saveAll(
                    apiKey = editApiKey,
                    aiModel = editAiModel,
                    messageTone = editMessageTone,
                    whatsappApiUrl = editWhatsappUrl,
                    gmailClientId = editGmailClientId,
                    businessName = editBusinessName
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Settings")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TeaGreen) },
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = TeaGreen)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
