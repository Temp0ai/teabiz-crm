package com.teabiz.crm.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.remote.GeminiService
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val apiKey by viewModel.apiKey.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val messageTone by viewModel.messageTone.collectAsState()
    val whatsappApiUrl by viewModel.whatsappApiUrl.collectAsState()
    val businessName by viewModel.businessName.collectAsState()
    val businessAddress by viewModel.businessAddress.collectAsState()
    val businessPhone by viewModel.businessPhone.collectAsState()

    var editApiKey by remember(apiKey) { mutableStateOf(apiKey) }
    var editMessageTone by remember(messageTone) { mutableStateOf(messageTone) }
    var editWhatsappUrl by remember(whatsappApiUrl) { mutableStateOf(whatsappApiUrl) }
    var editBusinessName by remember(businessName) { mutableStateOf(businessName) }
    var editBusinessAddress by remember(businessAddress) { mutableStateOf(businessAddress) }
    var editBusinessPhone by remember(businessPhone) { mutableStateOf(businessPhone) }

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

                SettingsTextField(
                    value = editBusinessPhone,
                    onValueChange = { editBusinessPhone = it },
                    label = "Business Phone",
                    icon = Icons.Default.Phone,
                    placeholder = "+91 98765 43210"
                )

                SettingsTextField(
                    value = editBusinessAddress,
                    onValueChange = { editBusinessAddress = it },
                    label = "Business Address (GMB)",
                    icon = Icons.Default.LocationOn,
                    placeholder = "123 Tea Lane, Mumbai, Maharashtra"
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
                    label = "Gemini API Key",
                    icon = Icons.Default.Key,
                    placeholder = "AIza..."
                )

                SettingsTextField(
                    value = editWhatsappUrl,
                    onValueChange = { editWhatsappUrl = it },
                    label = "WhatsApp API URL",
                    icon = Icons.Default.Chat,
                    placeholder = "http://localhost:3000"
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("AI Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                ModelSelector(
                    selectedModel = selectedModel,
                    onModelSelected = { viewModel.setModel(it) }
                )

                Text("Message Tone", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Professional", "Friendly", "Urgent").forEach { tone ->
                        FilterChip(
                            selected = editMessageTone == tone,
                            onClick = { editMessageTone = tone },
                            label = { Text(tone) }
                        )
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
                SettingsRow(icon = Icons.Default.Info, title = "App Version", subtitle = "1.3.8")
                SettingsRow(icon = Icons.Default.Security, title = "Privacy Policy", subtitle = "View policy")
                SettingsRow(icon = Icons.Default.Description, title = "Terms of Service", subtitle = "View terms")
            }
        }

        Button(
            onClick = {
                viewModel.saveAll(
                    apiKey = editApiKey,
                    selectedModel = selectedModel,
                    messageTone = editMessageTone,
                    whatsappApiUrl = editWhatsappUrl,
                    businessName = editBusinessName,
                    businessAddress = editBusinessAddress,
                    businessPhone = editBusinessPhone
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelector(
    selectedModel: String,
    onModelSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val models = GeminiService.AVAILABLE_MODELS
    val selectedName = models.find { it.first == selectedModel }?.second ?: "Gemini 2.0 Flash"

    Column {
        Text("AI Model", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedName,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C4DFF)) }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                models.forEach { (modelId, modelName) ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(modelName)
                                Text(
                                    modelId,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        },
                        onClick = {
                            onModelSelected(modelId)
                            expanded = false
                        },
                        leadingIcon = {
                            if (modelId == selectedModel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = TeaGreen)
                            }
                        }
                    )
                }
            }
        }
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
