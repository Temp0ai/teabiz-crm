package com.teabiz.crm.ui.screens.leads

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.LeadsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    leadId: String,
    leadsViewModel: LeadsViewModel,
    onBack: () -> Unit,
    onNavigateToAiFollowUp: (String) -> Unit
) {
    var lead by remember { mutableStateOf<Lead?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(leadId) {
        leadsViewModel.getLeadById(leadId) { fetched ->
            lead = fetched
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lead Detail") },
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
        lead?.let { leadData ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = TeaGreen,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = leadData.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (leadData.company.isNotBlank()) {
                                    Text(
                                        text = leadData.company,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        DetailRow(icon = Icons.Default.Email, label = "Email", value = leadData.email)
                        DetailRow(icon = Icons.Default.Phone, label = "Phone", value = leadData.phone)
                        DetailRow(icon = Icons.Default.LocationOn, label = "City", value = leadData.city)
                        DetailRow(icon = Icons.Default.Source, label = "Source", value = leadData.source)
                        DetailRow(icon = Icons.Default.Business, label = "Client Type", value = leadData.clientType)

                        if (leadData.productInterest.isNotEmpty()) {
                            Text(
                                text = "Product Interest",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                leadData.productInterest.forEach { product ->
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = PremixGold.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = product,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }

                        if (leadData.message.isNotBlank()) {
                            Text(
                                text = "Message/Inquiry",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            Text(
                                text = leadData.message,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("NEW", "CONTACTED", "FOLLOW_UP").forEach { status ->
                                StatusButton(status, leadData.status) { newStatus ->
                                    scope.launch {
                                        leadsViewModel.updateLeadStatus(leadId, newStatus)
                                        lead = leadData.copy(status = newStatus)
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("CONVERTED", "LOST").forEach { status ->
                                StatusButton(status, leadData.status) { newStatus ->
                                    scope.launch {
                                        leadsViewModel.updateLeadStatus(leadId, newStatus)
                                        lead = leadData.copy(status = newStatus)
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            leadData?.let { l ->
                                val phone = l.phone.ifBlank { l.email }
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://wa.me/${phone.replace(Regex("[^0-9]"), "")}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp")
                    }
                    OutlinedButton(
                        onClick = {
                            leadData?.let { l ->
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${l.phone}")
                                }
                                context.startActivity(intent)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call")
                    }
                }

                Button(
                    onClick = { onNavigateToAiFollowUp(leadId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate AI Follow-up Message")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    if (value.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = TeaGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun StatusButton(
    status: String,
    currentStatus: String,
    onStatusSelected: (String) -> Unit
) {
    val isSelected = status == currentStatus
    val color = when (status) {
        "NEW" -> StatusNew
        "CONTACTED" -> StatusContacted
        "FOLLOW_UP" -> StatusFollowUp
        "CONVERTED" -> StatusConverted
        "LOST" -> StatusLost
        else -> Color.Gray
    }

    OutlinedButton(
        onClick = { onStatusSelected(status) },
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent,
            contentColor = color
        )
    ) {
        Text(status, style = MaterialTheme.typography.labelSmall)
    }
}
