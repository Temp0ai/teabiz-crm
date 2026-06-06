package com.teabiz.crm.ui.screens.leads

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.LeadsViewModel

@Composable
fun LeadsScreen(
    viewModel: LeadsViewModel,
    onLeadClick: (String) -> Unit,
    onAddLead: () -> Unit
) {
    val leads by viewModel.leads.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val context = LocalContext.current

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
                text = "Leads",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = onAddLead,
                containerColor = TeaGreen
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lead")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search leads...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusFilterChip("ALL", "All", selectedFilter) { viewModel.updateFilter(it) }
            StatusFilterChip("NEW", "New", selectedFilter) { viewModel.updateFilter(it) }
            StatusFilterChip("FOLLOW_UP", "Follow-up", selectedFilter) { viewModel.updateFilter(it) }
            StatusFilterChip("CONVERTED", "Converted", selectedFilter) { viewModel.updateFilter(it) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip("Tea Premix", selectedCategory, viewModel)
            CategoryChip("Coffee Premix", selectedCategory, viewModel)
            CategoryChip("Tea Machine", selectedCategory, viewModel)
            CategoryChip("Coffee Machine", selectedCategory, viewModel)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PriorityFilterChip("HOT", "🔥 Hot", viewModel)
            PriorityFilterChip("WARM", "☀ Warm", viewModel)
            PriorityFilterChip("NORMAL", "Normal", viewModel)
            PriorityFilterChip("COLD", "❄ Cold", viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (leads.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No leads found", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leads) { lead ->
                    LeadItem(
                        lead = lead,
                        onClick = { onLeadClick(lead.id) },
                        onWhatsApp = {
                            val phone = lead.phone.replace(Regex("[^0-9+]"), "")
                            val cleanPhone = phone.replace("+", "")
                            com.teabiz.crm.util.WhatsAppUtils.openWhatsAppBusiness(context, cleanPhone)
                        },
                        onCall = {
                            val phone = lead.phone.replace(Regex("[^0-9+]"), "")
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusFilterChip(
    value: String,
    label: String,
    selected: String,
    onSelected: (String) -> Unit
) {
    val isSelected = value == selected
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) TeaGreen else Color.LightGray.copy(alpha = 0.5f),
        modifier = Modifier.clickable { onSelected(value) }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.DarkGray,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun CategoryChip(category: String, selectedCategory: String, viewModel: LeadsViewModel) {
    val isSelected = category == selectedCategory
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) PremixGold else PremixGold.copy(alpha = 0.3f),
        modifier = Modifier.clickable { viewModel.updateCategory(category) }
    ) {
        Text(
            text = category,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else Color.DarkGray
        )
    }
}

@Composable
fun PriorityFilterChip(value: String, label: String, viewModel: LeadsViewModel) {
    val selectedPriority by viewModel.selectedPriority.collectAsState()
    val isSelected = value == selectedPriority
    val color = when (value) {
        "HOT" -> Color(0xFFFF5722)
        "WARM" -> PremixGold
        "COLD" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        modifier = Modifier.clickable { viewModel.updatePriority(value) }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) Color.White else color
        )
    }
}

@Composable
fun PriorityBadgeSmall(priority: String) {
    val color = when (priority) {
        "HOT" -> Color(0xFFFF5722)
        "WARM" -> PremixGold
        "COLD" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Surface(shape = MaterialTheme.shapes.extraSmall, color = color.copy(alpha = 0.2f)) {
        Text(
            text = priority,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SourceLogoSmall(source: String) {
    val bgColor = when (source) {
        "INDIAMART" -> Color(0xFFE53935)
        "JUSTDIAL" -> Color(0xFF1565C0)
        "JSTDL" -> Color(0xFF6A1B9A)
        "KAGGLE" -> Color(0xFF20BEFF)
        "GMB" -> Color(0xFF43A047)
        "WHATSAPP" -> Color(0xFF25D366)
        "WEBSITE" -> Color(0xFF1976D2)
        "DEALER" -> Color(0xFFFF8F00)
        "DISTRIBUTOR" -> Color(0xFF6D4C41)
        "MACHINE" -> Color(0xFF546E7A)
        "ORDER" -> Color(0xFF00897B)
        "REFERRAL" -> Color(0xFFAB47BC)
        "GMAIL" -> Color(0xFFEA4335)
        "PHONE" -> Color(0xFF43A047)
        "EXCEL" -> Color(0xFF2E7D32)
        else -> TeaGreen
    }
    val label = when (source) {
        "INDIAMART" -> "IM"
        "JUSTDIAL" -> "JD"
        "JSTDL" -> "JS"
        "KAGGLE" -> "KG"
        "GMB" -> "GMB"
        "WHATSAPP" -> "WA"
        "WEBSITE" -> "WEB"
        "DEALER" -> "DLR"
        "DISTRIBUTOR" -> "DST"
        "MACHINE" -> "MCH"
        "ORDER" -> "ORD"
        "REFERRAL" -> "REF"
        "GMAIL" -> "GML"
        "PHONE" -> "PH"
        "EXCEL" -> "XLS"
        else -> "MAN"
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = bgColor,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun ScoreBadge(score: Int) {
    Surface(shape = MaterialTheme.shapes.extraSmall, color = TeaGreen.copy(alpha = 0.15f)) {
        Text(
            text = "$score",
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = TeaGreen,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LeadItem(lead: Lead, onClick: () -> Unit, onWhatsApp: () -> Unit = {}, onCall: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SourceLogoSmall(source = lead.source)

            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = lead.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    PriorityBadgeSmall(priority = lead.priority)
                    Spacer(modifier = Modifier.width(4.dp))
                    ScoreBadge(score = lead.leadScore)
                }
                if (lead.company.isNotBlank()) {
                    Text(
                        text = lead.company,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (lead.email.isNotBlank()) {
                        Text(
                            text = lead.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = TeaGreen
                        )
                    }
                    if (lead.phone.isNotBlank()) {
                        Text(
                            text = lead.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = CoffeeBrown
                        )
                    }
                }
                if (lead.productInterest.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        lead.productInterest.take(2).forEach { product ->
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = PremixGold.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = product,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (lead.phone.isNotBlank()) {
                    IconButton(onClick = onWhatsApp, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "WhatsApp",
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCall, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Call",
                            tint = TeaGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }
}
