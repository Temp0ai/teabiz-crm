package com.teabiz.crm.ui.screens.marketing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.Competitor
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.MarketingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitorScreen(
    viewModel: MarketingViewModel,
    onBack: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val competitors by viewModel.competitors.collectAsState()
    val isResearching by viewModel.isResearching.collectAsState()
    val analysis by viewModel.competitorAnalysis.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Competitor Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CoffeeBrown,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = CoffeeBrown) {
                Icon(Icons.Default.Add, contentDescription = "Add Competitor")
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
            if (competitors.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No competitors tracked yet", color = Color.Gray)
                            Text("Add competitors to analyze their strategies", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
            }

            if (isResearching) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(color = CoffeeBrown)
                            Text("Analyzing competitor...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (analysis != null) {
                item {
                    val a = analysis!!
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CoffeeBrown.copy(alpha = 0.05f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = CoffeeBrown, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analysis: ${a.competitorName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                            }

                            if (a.strengths.isNotEmpty()) {
                                Column {
                                    Text("Strengths", style = MaterialTheme.typography.labelLarge, color = StatusConverted, fontWeight = FontWeight.Bold)
                                    a.strengths.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                                }
                            }

                            if (a.weaknesses.isNotEmpty()) {
                                Column {
                                    Text("Weaknesses", style = MaterialTheme.typography.labelLarge, color = StatusLost, fontWeight = FontWeight.Bold)
                                    a.weaknesses.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                                }
                            }

                            if (a.opportunities.isNotEmpty()) {
                                Column {
                                    Text("Opportunities", style = MaterialTheme.typography.labelLarge, color = TeaGreen, fontWeight = FontWeight.Bold)
                                    a.opportunities.forEach { Text("• $it", style = MaterialTheme.typography.bodySmall) }
                                }
                            }

                            Button(
                                onClick = { viewModel.clearAnalysis() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                            ) {
                                Text("Clear Analysis")
                            }
                        }
                    }
                }
            }

            items(competitors) { competitor ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(competitor.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (competitor.website.isNotBlank()) {
                                    Text(competitor.website, style = MaterialTheme.typography.bodySmall, color = TeaGreen)
                                }
                            }
                            IconButton(onClick = { viewModel.deleteCompetitor(competitor) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLost)
                            }
                        }

                        if (competitor.keywords.isNotEmpty()) {
                            Text("Keywords:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                competitor.keywords.take(3).forEach { kw ->
                                    Surface(shape = MaterialTheme.shapes.extraSmall, color = PremixGold.copy(alpha = 0.3f)) {
                                        Text(kw, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        if (competitor.topProducts.isNotEmpty()) {
                            Text("Top Products:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            competitor.topProducts.take(3).forEach { product ->
                                Text("• $product", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Button(
                            onClick = { viewModel.analyzeCompetitor(competitor.name, competitor.website) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isResearching
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Analyze with AI")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCompetitorDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, website ->
                viewModel.addCompetitor(Competitor(name = name, website = website))
                showAddDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCompetitorDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Competitor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Competitor Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website URL") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, website) },
                enabled = name.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
