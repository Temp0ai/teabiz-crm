package com.teabiz.crm.ui.screens.leads

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.AiFollowUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFollowUpScreen(
    leadId: String,
    onBack: () -> Unit
) {
    val viewModel: AiFollowUpViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val generatedMessage by viewModel.generatedMessage.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val selectedTone by viewModel.selectedTone.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedMessageType by viewModel.selectedMessageType.collectAsState()
    val currentLead by viewModel.currentLead.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(leadId) {
        viewModel.loadLead(leadId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Follow-up") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CoffeeBrown,
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
            currentLead?.let { lead ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Lead Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(lead.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        if (lead.company.isNotBlank()) Text(lead.company, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        if (lead.productInterest.isNotEmpty()) {
                            Text("Interest: ${lead.productInterest.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = CoffeeBrown)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Message Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Text("Tone", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Professional", "Friendly", "Urgent").forEach { tone ->
                            FilterChip(
                                selected = selectedTone == tone,
                                onClick = { viewModel.updateTone(tone) },
                                label = { Text(tone) }
                            )
                        }
                    }

                    Text("Language", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("English", "Hindi", "Marathi").forEach { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { viewModel.updateLanguage(lang) },
                                label = { Text(lang) }
                            )
                        }
                    }

                    Text("Message Type", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "initial_inquiry" to "Initial Inquiry Follow-up",
                            "stale_lead" to "Stale Lead Re-engagement",
                            "post_purchase" to "Post-Purchase Thank You",
                            "promotional" to "Promotional Offer"
                        ).forEach { (type, label) ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                RadioButton(
                                    selected = selectedMessageType == type,
                                    onClick = { viewModel.updateMessageType(type) }
                                )
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    currentLead?.let { lead ->
                        viewModel.generateMessage(lead)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGenerating && currentLead != null,
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generating...")
                } else {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Message")
                }
            }

            if (generatedMessage.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Generated Message", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = generatedMessage, style = MaterialTheme.typography.bodyMedium)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Follow-up Message", generatedMessage)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Message copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                            Button(
                                onClick = {
                                    currentLead?.let { lead ->
                                        val phone = lead.phone.ifBlank { lead.email }
                                        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
                                        val url = "https://wa.me/${cleanPhone.replace("+", "")}?text=${Uri.encode(generatedMessage)}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        context.startActivity(intent)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Send WhatsApp")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                currentLead?.let { lead ->
                                    viewModel.saveFollowUp(lead.id, generatedMessage, "WHATSAPP")
                                    Toast.makeText(context, "Follow-up saved!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Follow-up")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
