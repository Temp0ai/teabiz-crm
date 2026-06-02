package com.teabiz.crm.ui.screens.marketing

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
import com.teabiz.crm.data.model.SeoKeyword
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.MarketingViewModel

@Composable
fun SeoToolsScreen(
    viewModel: MarketingViewModel,
    onBack: () -> Unit
) {
    var searchKeyword by remember { mutableStateOf("") }
    val keywords by viewModel.keywords.collectAsState()
    val isResearching by viewModel.isResearching.collectAsState()
    val researchResults by viewModel.researchResults.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SEO Tools") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TeaGreen,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Keyword Research", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = searchKeyword,
                            onValueChange = { searchKeyword = it },
                            label = { Text("Enter keywords (comma-separated)") },
                            placeholder = { Text("tea premix, coffee machine, chai powder") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val keywordList = searchKeyword.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                if (keywordList.isNotEmpty()) {
                                    viewModel.researchKeywords(keywordList)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = searchKeyword.isNotBlank() && !isResearching,
                            colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                        ) {
                            if (isResearching) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Researching...")
                            } else {
                                Icon(Icons.Default.Search, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Research Keywords")
                            }
                        }
                    }
                }
            }

            if (researchResults.isNotEmpty()) {
                item {
                    Text("Research Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(researchResults) { result ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(result.keyword, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Search Volume", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(result.searchVolume.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TeaGreen)
                                }
                                Column {
                                    Text("Competition", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(result.competition, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Column {
                                    Text("Trend", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(result.trend, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (result.relatedKeywords.isNotEmpty()) {
                                Text("Related Keywords", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    result.relatedKeywords.take(4).forEach { kw ->
                                        Surface(shape = MaterialTheme.shapes.extraSmall, color = PremixGold.copy(alpha = 0.3f)) {
                                            Text(kw, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }

                            if (result.contentSuggestions.isNotEmpty()) {
                                Text("Content Ideas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                result.contentSuggestions.take(2).forEach { suggestion ->
                                    Text("• $suggestion", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            if (keywords.isNotEmpty()) {
                item {
                    Text("Saved Keywords", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(keywords) { keyword ->
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(keyword.keyword, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Vol: ${keyword.searchVolume} | Comp: ${keyword.competition}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { viewModel.deleteKeyword(keyword) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusLost)
                            }
                        }
                    }
                }
            }
        }
    }
}
