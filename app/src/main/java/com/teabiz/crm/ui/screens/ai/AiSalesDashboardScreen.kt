package com.teabiz.crm.ui.screens.ai

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
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.data.remote.AiSalesAssistant
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.LeadsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSalesDashboardScreen(
    leadsViewModel: LeadsViewModel,
    onBack: () -> Unit
) {
    val leads by leadsViewModel.leads.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Lead Analysis", "Cold Leads", "Ad Copy", "Content Calendar")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF7C4DFF))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI Sales Assistant")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF7C4DFF),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF7C4DFF).copy(alpha = 0.1f)
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            when (selectedTab) {
                0 -> LeadAnalysisTab(leads)
                1 -> ColdLeadsTab(leads)
                2 -> AdCopyTab()
                3 -> ContentCalendarTab()
            }
        }
    }
}

@Composable
fun LeadAnalysisTab(leads: List<Lead>) {
    var selectedLead by remember { mutableStateOf<Lead?>(null) }
    var analysis by remember { mutableStateOf<AiSalesAssistant.LeadAnalysis?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text("Select a lead to analyze", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(leads.take(20)) { lead ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    selectedLead = lead
                    isAnalyzing = true
                },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lead.name, fontWeight = FontWeight.Bold)
                        Text(lead.company.ifBlank { lead.phone }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = when (lead.priority) {
                            "HOT" -> StatusLost.copy(alpha = 0.2f)
                            "WARM" -> PremixGold.copy(alpha = 0.2f)
                            else -> Color.Gray.copy(alpha = 0.2f)
                        }
                    ) {
                        Text(
                            lead.priority,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = when (lead.priority) {
                                "HOT" -> StatusLost
                                "WARM" -> PremixGold
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }
        }
    }

    if (isAnalyzing && selectedLead != null) {
        AlertDialog(
            onDismissRequest = { isAnalyzing = false },
            title = { Text("AI Analysis: ${selectedLead!!.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = Color(0xFF7C4DFF))
                    Text("Analyzing lead with AI...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { isAnalyzing = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun ColdLeadsTab(leads: List<Lead>) {
    val coldLeads = leads.filter { lead ->
        val daysSince = if (lead.lastFollowUpAt != null) {
            (System.currentTimeMillis() - lead.lastFollowUpAt) / (1000 * 60 * 60 * 24)
        } else {
            (System.currentTimeMillis() - lead.createdAt) / (1000 * 60 * 60 * 24)
        }
        daysSince > 14
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${coldLeads.size} cold leads detected", color = Color(0xFFFF9800), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(coldLeads) { lead ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(lead.name, fontWeight = FontWeight.Bold)
                        Text(lead.company.ifBlank { "" }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("Products: ${lead.productInterest.joinToString(", ")}", style = MaterialTheme.typography.bodySmall, color = TeaGreen)
                    Button(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                    ) {
                        Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Send Re-engagement")
                    }
                }
            }
        }
    }
}

@Composable
fun AdCopyTab() {
    var product by remember { mutableStateOf("Tea Premix") }
    var platform by remember { mutableStateOf("Facebook") }
    var generatedAd by remember { mutableStateOf<AiSalesAssistant.AdCopy?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("AI Ad Copy Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            OutlinedTextField(
                value = product,
                onValueChange = { product = it },
                label = { Text("Product") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Facebook", "Instagram", "Google").forEach { p ->
                    FilterChip(
                        selected = platform == p,
                        onClick = { platform = p },
                        label = { Text(p) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = { isGenerating = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Ad Copy")
            }
        }

        if (generatedAd != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = TeaGreen.copy(alpha = 0.05f))) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Generated Ad Copy", fontWeight = FontWeight.Bold, color = TeaGreen)
                        Text(generatedAd!!.headline, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text(generatedAd!!.body, style = MaterialTheme.typography.bodySmall)
                        Button(
                            onClick = { },
                            colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                        ) {
                            Text(generatedAd!!.callToAction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContentCalendarTab() {
    var calendar by remember { mutableStateOf<List<AiSalesAssistant.ContentSuggestion>>(emptyList()) }
    var isGenerating by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("AI Content Calendar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(onClick = { isGenerating = true }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Generate")
                }
            }
        }

        items(calendar) { suggestion ->
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(suggestion.topic, fontWeight = FontWeight.Bold)
                        Surface(shape = MaterialTheme.shapes.extraSmall, color = PremixGold.copy(alpha = 0.2f)) {
                            Text(suggestion.contentType, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = PremixGold)
                        }
                    }
                    Text(suggestion.caption, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(suggestion.bestTime, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        suggestion.hashtags.take(3).forEach { tag ->
                            Surface(shape = MaterialTheme.shapes.extraSmall, color = TeaGreen.copy(alpha = 0.1f)) {
                                Text(tag, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp), style = MaterialTheme.typography.labelSmall, color = TeaGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
