package com.teabiz.crm.ui.screens.campaigns

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
import com.teabiz.crm.data.model.Campaign
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.CampaignsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(viewModel: CampaignsViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    val campaigns by viewModel.campaigns.collectAsState()
    val campaignState by viewModel.campaignState.collectAsState()
    val sendProgress by viewModel.sendProgress.collectAsState()
    val sendStatus by viewModel.sendStatus.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Campaigns",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = TeaGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Campaign")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (campaignState is CampaignsViewModel.CampaignState.Sending) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF25D366).copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF25D366))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sending WhatsApp messages...", fontWeight = FontWeight.Bold)
                    }
                    if (sendProgress.second > 0) {
                        LinearProgressIndicator(
                            progress = { sendProgress.first.toFloat() / sendProgress.second },
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF25D366)
                        )
                        Text(
                            "${sendProgress.first} / ${sendProgress.second} messages sent",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    if (sendStatus.isNotBlank()) {
                        Text(sendStatus, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (campaignState is CampaignsViewModel.CampaignState.Completed) {
            val completed = campaignState as CampaignsViewModel.CampaignState.Completed
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TeaGreen.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TeaGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Campaign sent! ${completed.sent} delivered, ${completed.failed} failed", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LaunchedEffect(Unit) { viewModel.resetState() }
        }

        if (campaigns.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No campaigns yet", color = Color.Gray)
                    Text("Create your first campaign to get started", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(campaigns) { campaign ->
                    CampaignItem(
                        campaign = campaign,
                        onSend = { viewModel.sendCampaign(campaign.id) },
                        onDelete = { viewModel.deleteCampaign(campaign) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCampaignDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, template, category ->
                viewModel.createCampaign(name, template, category)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CampaignItem(
    campaign: Campaign,
    onSend: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(campaign.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                CampaignStatusChip(status = campaign.status)
            }

            if (campaign.targetCategory.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Category, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(campaign.targetCategory, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            Text(
                text = campaign.messageTemplate.take(100) + if (campaign.messageTemplate.length > 100) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(campaign.sentCount.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TeaGreen)
                    Text("Sent", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(campaign.failedCount.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusLost)
                    Text("Failed", style = MaterialTheme.typography.labelSmall)
                }
            }

            if (campaign.status == "DRAFT") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSend,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Now")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLost)
                    }
                }
            }
        }
    }
}

@Composable
fun CampaignStatusChip(status: String) {
    val color = when (status) {
        "DRAFT" -> Color.Gray
        "SCHEDULED" -> StatusFollowUp
        "RUNNING" -> StatusNew
        "COMPLETED" -> StatusConverted
        "FAILED" -> StatusLost
        else -> Color.Gray
    }

    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.2f)) {
        Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
fun CreateCampaignDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var template by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Campaign") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Campaign Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Target Category (e.g., Tea Premix)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it },
                    label = { Text("Message Template") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Text("Use {name}, {company}, {product} as placeholders", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, template, category) },
                enabled = name.isNotBlank() && template.isNotBlank()
            ) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
