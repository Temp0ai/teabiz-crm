package com.teabiz.crm.ui.screens.campaigns

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.teabiz.crm.data.model.Campaign
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.CampaignsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignsScreen(
    viewModel: CampaignsViewModel,
    onNavigateToWhatsAppWeb: () -> Unit = {}
) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = com.teabiz.crm.R.drawable.ic_whatsapp),
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WhatsApp",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            Row {
                IconButton(onClick = onNavigateToWhatsAppWeb) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "WhatsApp Web",
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(28.dp)
                    )
                }
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = TeaGreen
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Campaign")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (campaignState is CampaignsViewModel.CampaignState.Sending) {
            val isPaused by viewModel.isPaused.collectAsState()
            val sentList by viewModel.sentContacts.collectAsState()
            val remainingList by viewModel.remainingContacts.collectAsState()
            var showContactDetails by remember { mutableStateOf(false) }

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
                        if (isPaused) {
                            Icon(Icons.Default.Pause, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Campaign Paused", fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                        } else {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF25D366))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sending WhatsApp messages...", fontWeight = FontWeight.Bold)
                        }
                    }
                    if (sendProgress.second > 0) {
                        LinearProgressIndicator(
                            progress = { sendProgress.first.toFloat() / sendProgress.second },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isPaused) Color(0xFFFF9800) else Color(0xFF25D366)
                        )
                        Text(
                            "${sendProgress.first} / ${sendProgress.second} messages sent",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isPaused) {
                            Button(
                                onClick = { viewModel.resumeCampaign() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Resume")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseCampaign() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pause")
                            }
                        }
                        Button(
                            onClick = { viewModel.stopCampaign() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusLost)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }

                    TextButton(onClick = { showContactDetails = !showContactDetails }) {
                        Text(
                            if (showContactDetails) "Hide Details" else "View Sent & Remaining",
                            color = Color(0xFF25D366)
                        )
                        Icon(
                            if (showContactDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color(0xFF25D366)
                        )
                    }

                    if (showContactDetails) {
                        if (sentList.isNotEmpty()) {
                            Surface(shape = MaterialTheme.shapes.small, color = TeaGreen.copy(alpha = 0.1f)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Sent (${sentList.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TeaGreen)
                                    sentList.takeLast(5).forEach { Text("✓ $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                                    if (sentList.size > 5) Text("...and ${sentList.size - 5} more", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                        if (remainingList.isNotEmpty()) {
                            Surface(shape = MaterialTheme.shapes.small, color = Color(0xFFFF9800).copy(alpha = 0.1f)) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Remaining (${remainingList.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                                    remainingList.take(5).forEach { Text("○ $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                                    if (remainingList.size > 5) Text("...and ${remainingList.size - 5} more", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }

                    if (sendStatus.isNotBlank()) {
                        Text(sendStatus, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (campaignState is CampaignsViewModel.CampaignState.Paused) {
            val paused = campaignState as CampaignsViewModel.CampaignState.Paused
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PauseCircle, contentDescription = null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Campaign paused! ${paused.sent} sent, ${paused.failed} failed, ${paused.remaining} remaining", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LaunchedEffect(Unit) { viewModel.resetState() }
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
                        onDelete = { viewModel.deleteCampaign(campaign) },
                        onPause = { viewModel.pauseCampaign() },
                        onResume = { viewModel.resumeCampaign() },
                        onStop = { viewModel.stopCampaign() },
                        onShareMedia = { phone, mediaUri, caption ->
                            viewModel.shareMediaToWhatsApp(phone, mediaUri, caption)
                        }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateCampaignDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, template, category, batchSize, mediaUri, mediaType, scheduledAt, priority, source, city, followUpHours, followUpMessage, abTestMessage, language, tone ->
                viewModel.createCampaign(name, template, category, batchSize, mediaUri, mediaType, scheduledAt, priority, source, city, followUpHours, followUpMessage, abTestMessage, language, tone)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CampaignItem(
    campaign: Campaign,
    onSend: () -> Unit,
    onDelete: () -> Unit,
    onPause: () -> Unit = {},
    onResume: () -> Unit = {},
    onStop: () -> Unit = {},
    onShareMedia: (String, String, String) -> Unit
) {
    var showBatchInfo by remember { mutableStateOf(false) }

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

            if (campaign.targetPriority.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = StatusLost, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Priority: ${campaign.targetPriority}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }

            if (campaign.scheduledAt != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = StatusFollowUp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scheduled: ${SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(campaign.scheduledAt))}", style = MaterialTheme.typography.bodySmall, color = StatusFollowUp)
                }
            }

            if (campaign.abTestEnabled) {
                Surface(shape = MaterialTheme.shapes.extraSmall, color = Color(0xFF7C4DFF).copy(alpha = 0.15f)) {
                    Text("A/B Test", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color(0xFF7C4DFF))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ViewModule, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Batch size: ${campaign.batchSize}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            if (campaign.mediaUri.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Attachment, contentDescription = null, tint = TeaGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Has ${campaign.mediaType} attached", style = MaterialTheme.typography.bodySmall, color = TeaGreen)
                }
            }

            Text(
                text = campaign.messageTemplate.take(100) + if (campaign.messageTemplate.length > 100) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            if (campaign.status == "RUNNING" && campaign.totalBatches > 0) {
                LinearProgressIndicator(
                    progress = { campaign.currentBatch.toFloat() / campaign.totalBatches },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF25D366)
                )
                Text(
                    "Batch ${campaign.currentBatch}/${campaign.totalBatches} complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(campaign.totalRecipients.toString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                    Text("Total", style = MaterialTheme.typography.labelSmall)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (campaign.status) {
                    "DRAFT", "SCHEDULED" -> {
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
                    "RUNNING" -> {
                        Button(
                            onClick = onPause,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }
                        Button(
                            onClick = onStop,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusLost)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                    }
                    "PAUSED" -> {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Resume")
                        }
                        Button(
                            onClick = onStop,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusLost)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop")
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLost)
                        }
                    }
                    else -> {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLost)
                        }
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
        "PAUSED" -> Color(0xFFFF9800)
        "RECURRING" -> Color(0xFF7C4DFF)
        else -> Color.Gray
    }

    Surface(shape = MaterialTheme.shapes.small, color = color.copy(alpha = 0.2f)) {
        Text(status, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCampaignDialog(
    viewModel: CampaignsViewModel,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, Int, String, String, Long?, String, String, String, Int, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var template by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var batchSize by remember { mutableIntStateOf(100) }
    var showBatchDropdown by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    val selectedMediaUri by viewModel.selectedMediaUri.collectAsState()
    val selectedMediaType by viewModel.selectedMediaType.collectAsState()
    val isGeneratingAiText by viewModel.isGeneratingAiText.collectAsState()
    val contactCount by viewModel.contactCount.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val bestTimeSuggestion by viewModel.bestTimeSuggestion.collectAsState()

    var aiTone by remember { mutableStateOf("Professional") }
    var aiLanguage by remember { mutableStateOf("English") }
    var showToneDropdown by remember { mutableStateOf(false) }
    var showLanguageDropdown by remember { mutableStateOf(false) }

    var selectedPriority by remember { mutableStateOf("") }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    var selectedSource by remember { mutableStateOf("") }
    var showSourceDropdown by remember { mutableStateOf(false) }
    var selectedCity by remember { mutableStateOf("") }

    var scheduledDateTime by remember { mutableStateOf<Long?>(null) }
    var showSchedulePicker by remember { mutableStateOf(false) }

    var followUpHours by remember { mutableIntStateOf(0) }
    var followUpMessage by remember { mutableStateOf("") }
    var showFollowUp by remember { mutableStateOf(false) }

    var abTestMessage by remember { mutableStateOf("") }
    var showAbTest by remember { mutableStateOf(false) }

    var showTemplateDropdown by remember { mutableStateOf(false) }

    val productCategories = listOf(
        "All Contacts",
        "Tea Premix",
        "Coffee Premix",
        "Nescafe Premix",
        "Tea Vending Machine",
        "Coffee Vending Machine",
        "Tea & Coffee Vending Machine",
        "Tea Machine",
        "Coffee Machine",
        "Instant Chai",
        "Masala Chai Premix"
    )

    val priorityOptions = listOf("All", "HOT", "WARM", "NORMAL", "COLD")
    val sourceOptions = listOf("All", "IndiaMART", "JustDial", "Website", "Referral", "Walk-in", "Social Media", "Google Ads", "Facebook Ads", "Instagram", "LinkedIn", "Trade Show", "Cold Call", "Email", "WhatsApp", "Other")

    LaunchedEffect(category, selectedPriority, selectedSource, selectedCity) {
        val cat = if (category.isBlank() || category == "All Contacts") "" else category
        val pri = if (selectedPriority.isBlank() || selectedPriority == "All") "" else selectedPriority
        val src = if (selectedSource.isBlank() || selectedSource == "All") "" else selectedSource
        viewModel.getFilteredLeads(cat, pri, src, selectedCity)
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val mimeType = context.contentResolver.getType(it) ?: ""
            val type = when {
                mimeType.startsWith("video") -> "video"
                mimeType.startsWith("image") -> "image"
                else -> "image"
            }
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            viewModel.setSelectedMedia(it, type)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = com.teabiz.crm.R.drawable.ic_whatsapp),
                    contentDescription = "WhatsApp",
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Campaign")
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Campaign Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Template selector
                if (templates.isNotEmpty()) {
                    item {
                        Box {
                            ExposedDropdownMenuBox(
                                expanded = showTemplateDropdown,
                                onExpandedChange = { showTemplateDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = "Load from template...",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Templates") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTemplateDropdown) },
                                    leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showTemplateDropdown,
                                    onDismissRequest = { showTemplateDropdown = false }
                                ) {
                                    templates.forEach { t ->
                                        DropdownMenuItem(
                                            text = { Text("${t.name} (${t.useCount}x used)") },
                                            onClick = {
                                                template = t.messageTemplate
                                                aiTone = t.tone
                                                aiLanguage = t.language
                                                if (t.category.isNotBlank()) category = t.category
                                                showTemplateDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Product Category with Contact Count
                item {
                    Box {
                        ExposedDropdownMenuBox(
                            expanded = showCategoryDropdown,
                            onExpandedChange = { showCategoryDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = category.ifBlank { "All Contacts" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Target Product Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                                leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showCategoryDropdown,
                                onDismissRequest = { showCategoryDropdown = false }
                            ) {
                                productCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            category = if (cat == "All Contacts") "" else cat
                                            showCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (contactCount > 0) TeaGreen.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = if (contactCount > 0) TeaGreen else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "$contactCount contacts found",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (contactCount > 0) TeaGreen else Color.Gray
                                )
                            }
                        }
                    }
                }

                // Advanced Targeting
                item {
                    HorizontalDivider()
                    Text("Advanced Targeting", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = showPriorityDropdown,
                                onExpandedChange = { showPriorityDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedPriority.ifBlank { "All" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Lead Priority") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPriorityDropdown) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showPriorityDropdown,
                                    onDismissRequest = { showPriorityDropdown = false }
                                ) {
                                    priorityOptions.forEach { p ->
                                        DropdownMenuItem(
                                            text = { Text(p) },
                                            onClick = {
                                                selectedPriority = if (p == "All") "" else p
                                                showPriorityDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = showSourceDropdown,
                                onExpandedChange = { showSourceDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedSource.ifBlank { "All" },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Lead Source") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSourceDropdown) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showSourceDropdown,
                                    onDismissRequest = { showSourceDropdown = false }
                                ) {
                                    sourceOptions.forEach { s ->
                                        DropdownMenuItem(
                                            text = { Text(s) },
                                            onClick = {
                                                selectedSource = if (s == "All") "" else s
                                                showSourceDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = selectedCity,
                        onValueChange = { selectedCity = it },
                        label = { Text("Target City (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Box {
                        ExposedDropdownMenuBox(
                            expanded = showBatchDropdown,
                            onExpandedChange = { showBatchDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = "$batchSize contacts per batch",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Batch Size") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBatchDropdown) },
                                modifier = Modifier.fillMaxWidth().menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = showBatchDropdown,
                                onDismissRequest = { showBatchDropdown = false }
                            ) {
                                listOf(25, 50, 75, 100, 150, 200).forEach { size ->
                                    DropdownMenuItem(
                                        text = { Text("$size contacts") },
                                        onClick = {
                                            batchSize = size
                                            showBatchDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // AI Tone + Language
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = showToneDropdown,
                                onExpandedChange = { showToneDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = aiTone,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Tone") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showToneDropdown) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showToneDropdown,
                                    onDismissRequest = { showToneDropdown = false }
                                ) {
                                    listOf("Professional", "Friendly", "Casual", "Urgent", "Formal").forEach { tone ->
                                        DropdownMenuItem(
                                            text = { Text(tone) },
                                            onClick = {
                                                aiTone = tone
                                                showToneDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ExposedDropdownMenuBox(
                                expanded = showLanguageDropdown,
                                onExpandedChange = { showLanguageDropdown = it }
                            ) {
                                OutlinedTextField(
                                    value = aiLanguage,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Language") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLanguageDropdown) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = showLanguageDropdown,
                                    onDismissRequest = { showLanguageDropdown = false }
                                ) {
                                    listOf("English", "Hindi", "Marathi").forEach { lang ->
                                        DropdownMenuItem(
                                            text = { Text(lang) },
                                            onClick = {
                                                aiLanguage = lang
                                                showLanguageDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Generate Button
                item {
                    Button(
                        onClick = {
                            viewModel.generateAiMessage(
                                campaignName = name,
                                productCategory = category,
                                tone = aiTone,
                                language = aiLanguage,
                                messageType = "promotional",
                                onResult = { generated -> template = generated }
                            )
                        },
                        enabled = !isGeneratingAiText,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                    ) {
                        if (isGeneratingAiText) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Generating...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Generate Message")
                        }
                    }
                }

                // Message Template
                item {
                    OutlinedTextField(
                        value = template,
                        onValueChange = { template = it },
                        label = { Text("Message Template") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4
                    )
                    Text(
                        "Placeholders: {name}, {company}, {product}, {city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                // Best Time Suggestion
                item {
                    Surface(shape = MaterialTheme.shapes.small, color = TeaGreen.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TeaGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(bestTimeSuggestion, style = MaterialTheme.typography.bodySmall, color = TeaGreen)
                        }
                    }
                }

                // Schedule
                item {
                    HorizontalDivider()
                    Text("Scheduling", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                }

                item {
                    OutlinedButton(
                        onClick = { showSchedulePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (scheduledDateTime != null) "Scheduled: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(scheduledDateTime!!))}" else "Schedule for Later (optional)")
                    }
                }

                // Follow-up
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Follow-up Sequence", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Switch(checked = showFollowUp, onCheckedChange = { showFollowUp = it })
                    }
                }

                if (showFollowUp) {
                    item {
                        OutlinedTextField(
                            value = followUpHours.toString(),
                            onValueChange = { followUpHours = it.toIntOrNull() ?: 0 },
                            label = { Text("Send follow-up after (hours)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = followUpMessage,
                            onValueChange = { followUpMessage = it },
                            label = { Text("Follow-up Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                // A/B Testing
                item {
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("A/B Testing", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Switch(checked = showAbTest, onCheckedChange = { showAbTest = it })
                    }
                }

                if (showAbTest) {
                    item {
                        OutlinedTextField(
                            value = abTestMessage,
                            onValueChange = { abTestMessage = it },
                            label = { Text("Variant B Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                        Text("50% contacts get Message A, 50% get Message B", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                // Media Attach
                item {
                    HorizontalDivider()
                    Text("Attach Media (Optional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (selectedMediaUri != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TeaGreen.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (selectedMediaType == "image") {
                                    Image(
                                        painter = rememberAsyncImagePainter(selectedMediaUri),
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.VideoFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(60.dp),
                                        tint = TeaGreen
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${selectedMediaType.uppercase()} Attached", fontWeight = FontWeight.Bold)
                                    Text("Will be shared with each contact", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { viewModel.clearSelectedMedia() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = StatusLost)
                                }
                            }
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") }
                            ) {
                                Icon(Icons.Default.Image, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Image")
                            }
                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("video/*") }
                            ) {
                                Icon(Icons.Default.VideoFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Video")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.saveTemplate(name, template, category, aiTone, aiLanguage)
                    onCreate(
                        name, template, category, batchSize,
                        selectedMediaUri?.toString() ?: "", selectedMediaType,
                        scheduledDateTime, selectedPriority, selectedSource, selectedCity,
                        if (showFollowUp) followUpHours else 0, followUpMessage,
                        abTestMessage, aiLanguage, aiTone
                    )
                    viewModel.clearSelectedMedia()
                },
                enabled = name.isNotBlank() && template.isNotBlank() && contactCount > 0
            ) { Text("Create Campaign") }
        },
        dismissButton = {
            TextButton(onClick = {
                viewModel.clearSelectedMedia()
                onDismiss()
            }) { Text("Cancel") }
        }
    )

    if (showSchedulePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = scheduledDateTime ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showSchedulePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    scheduledDateTime = datePickerState.selectedDateMillis
                    showSchedulePicker = false
                }) { Text("Set Date") }
            },
            dismissButton = {
                TextButton(onClick = { showSchedulePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
