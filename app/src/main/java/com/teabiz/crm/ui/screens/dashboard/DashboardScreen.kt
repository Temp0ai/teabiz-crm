package com.teabiz.crm.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToLeads: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToAiDashboard: () -> Unit = {}
) {
    val leadCount by viewModel.totalLeads.collectAsState()
    val newLeadCount by viewModel.newLeadCount.collectAsState()
    val followUpCount by viewModel.followUpCount.collectAsState()
    val convertedCount by viewModel.convertedCount.collectAsState()
    val recentLeads by viewModel.recentLeads.collectAsState()
    val campaignCount by viewModel.campaignCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "TeaBiz CRM",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TeaGreen
            )
        }

        item {
            Text(
                text = "Welcome back! Here's your overview.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Total Leads",
                    value = leadCount.toString(),
                    icon = Icons.Default.People,
                    color = TeaGreen
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "New",
                    value = newLeadCount.toString(),
                    icon = Icons.Default.NewReleases,
                    color = StatusNew
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Follow-up",
                    value = followUpCount.toString(),
                    icon = Icons.Default.FollowTheSigns,
                    color = StatusFollowUp
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Converted",
                    value = convertedCount.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = StatusConverted
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Campaigns",
                    value = campaignCount.toString(),
                    icon = Icons.Default.Campaign,
                    color = PremixGold
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Win Rate",
                    value = if (leadCount > 0) "${(convertedCount * 100 / leadCount)}%" else "0%",
                    icon = Icons.Default.TrendingUp,
                    color = CoffeeBrown
                )
            }
        }

        item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToImport,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import")
                        }
                        Button(
                            onClick = onNavigateToAiDashboard,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Assistant")
                        }
                        OutlinedButton(
                            onClick = onNavigateToLeads,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.People, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Leads")
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Recent Leads",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(recentLeads) { lead ->
            RecentLeadItem(lead = lead)
        }

        if (recentLeads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No leads yet. Import or add leads to get started!", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
            Card(
                modifier = modifier,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
            ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun RecentLeadItem(lead: Lead) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = TeaGreen,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = lead.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lead.email.ifBlank { lead.phone },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            StatusChip(status = lead.status)
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status) {
        "NEW" -> StatusNew
        "CONTACTED" -> StatusContacted
        "FOLLOW_UP" -> StatusFollowUp
        "CONVERTED" -> StatusConverted
        "LOST" -> StatusLost
        else -> Color.Gray
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
