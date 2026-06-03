package com.teabiz.crm.ui.screens.leads

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.teabiz.crm.data.model.LeadActivity
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.LeadsViewModel
import com.teabiz.crm.util.QuotationGenerator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeadDetailScreen(
    leadId: String,
    leadsViewModel: LeadsViewModel,
    onBack: () -> Unit,
    onNavigateToAiFollowUp: (String) -> Unit
) {
    var lead by remember { mutableStateOf<Lead?>(null) }
    val activities by leadsViewModel.getActivitiesForLead(leadId).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showQuotationDialog by remember { mutableStateOf(false) }

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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Lead Info Card
                item {
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
                                Column(modifier = Modifier.weight(1f)) {
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
                                // Priority Badge
                                PriorityBadge(priority = leadData.priority)
                            }

                            // Score Bar
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Score:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Spacer(modifier = Modifier.width(8.dp))
                                LinearProgressIndicator(
                                    progress = { leadData.leadScore / 100f },
                                    modifier = Modifier.weight(1f).height(8.dp),
                                    color = when {
                                        leadData.leadScore >= 70 -> Color(0xFFFF5722)
                                        leadData.leadScore >= 45 -> PremixGold
                                        else -> Color.Gray
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${leadData.leadScore}/100", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider()

                            DetailRow(icon = Icons.Default.Email, label = "Email", value = leadData.email)
                            DetailRow(icon = Icons.Default.Phone, label = "Phone", value = leadData.phone)
                            DetailRow(icon = Icons.Default.LocationOn, label = "City", value = leadData.city)
                            DetailRow(icon = Icons.Default.Source, label = "Source", value = leadData.source)
                            DetailRow(icon = Icons.Default.Business, label = "Client Type", value = leadData.clientType)

                            if (leadData.productInterest.isNotEmpty()) {
                                Text("Product Interest", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
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
                                Text("Message/Inquiry", style = MaterialTheme.typography.labelLarge, color = Color.Gray)
                                Text(text = leadData.message, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("NEW", "CONTACTED", "FOLLOW_UP").forEach { status ->
                                    StatusButton(status, leadData.status) { newStatus ->
                                        scope.launch {
                                            leadsViewModel.updateLeadStatus(leadId, newStatus)
                                            leadsViewModel.logActivity(leadId, "STATUS_CHANGE", "Status changed to $newStatus")
                                            lead = leadData.copy(status = newStatus)
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("NEGOTIATION", "CONVERTED", "LOST").forEach { status ->
                                    StatusButton(status, leadData.status) { newStatus ->
                                        scope.launch {
                                            leadsViewModel.updateLeadStatus(leadId, newStatus)
                                            leadsViewModel.logActivity(leadId, "STATUS_CHANGE", "Status changed to $newStatus")
                                            lead = leadData.copy(status = newStatus)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val phone = leadData.phone
                                if (phone.isNotBlank()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse("https://wa.me/${phone.replace(Regex("[^0-9]"), "")}")
                                    }
                                    context.startActivity(intent)
                                    scope.launch { leadsViewModel.logActivity(leadId, "WHATSAPP", "Opened WhatsApp chat") }
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
                                if (leadData.phone.isNotBlank()) {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:${leadData.phone}")
                                    }
                                    context.startActivity(intent)
                                    scope.launch { leadsViewModel.logActivity(leadId, "CALL", "Made a phone call") }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call")
                        }
                    }
                }

                // Quick Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showScheduleDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Schedule")
                        }
                        OutlinedButton(
                            onClick = { showAddActivityDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.NoteAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Note")
                        }
                        OutlinedButton(
                            onClick = { showQuotationDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Quotation")
                        }
                    }
                }

                // AI Follow-up Button
                item {
                    Button(
                        onClick = { onNavigateToAiFollowUp(leadId) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate AI Follow-up Message")
                    }
                }

                // Activity Timeline
                item {
                    Text(
                        "Activity Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (activities.isEmpty()) {
                    item {
                        Text("No activities yet", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }

                items(activities) { activity ->
                    ActivityItem(activity)
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Schedule Follow-up Dialog
    if (showScheduleDialog) {
        ScheduleFollowUpDialog(
            leadName = lead?.name ?: "",
            onDismiss = { showScheduleDialog = false },
            onSchedule = { date, time ->
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, date.first)
                cal.set(Calendar.MONTH, date.second)
                cal.set(Calendar.DAY_OF_MONTH, date.third)
                cal.set(Calendar.HOUR_OF_DAY, time.first)
                cal.set(Calendar.MINUTE, time.second)
                val scheduledAt = cal.timeInMillis

                scope.launch {
                    leadsViewModel.scheduleFollowUp(leadId, scheduledAt)
                    leadsViewModel.logActivity(leadId, "FOLLOW_UP_SCHEDULED", "Follow-up scheduled for ${SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date(scheduledAt))}")
                    leadsViewModel.updateLeadNextFollowUp(leadId, scheduledAt)
                    lead = lead?.copy(nextFollowUpAt = scheduledAt)
                }
                showScheduleDialog = false
            }
        )
    }

    // Add Activity Dialog
    if (showAddActivityDialog) {
        AddActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onAdd = { type, description ->
                scope.launch {
                    leadsViewModel.logActivity(leadId, type, description)
                }
                showAddActivityDialog = false
            }
        )
    }

    // Quotation Dialog
    if (showQuotationDialog) {
        QuotationDialog(
            lead = lead,
            onDismiss = { showQuotationDialog = false },
            onGenerate = { items, notes ->
                lead?.let { l ->
                    val quotation = QuotationGenerator.Quotation(
                        lead = l,
                        items = items,
                        notes = notes
                    )
                    val file = QuotationGenerator.generatePdf(context, quotation)
                    if (file != null) {
                        scope.launch {
                            leadsViewModel.logActivity(leadId, "QUOTATION", "Generated quotation ${quotation.id} - Rs.%.0f".format(quotation.grandTotal))
                        }
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.provider",
                                file
                            )
                            setDataAndType(uri, "application/pdf")
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(intent)
                    }
                }
                showQuotationDialog = false
            }
        )
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val color = when (priority) {
        "HOT" -> Color(0xFFFF5722)
        "WARM" -> PremixGold
        "NORMAL" -> Color.Gray
        "COLD" -> Color(0xFF2196F3)
        else -> Color.Gray
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                when (priority) {
                    "HOT" -> Icons.Default.LocalFireDepartment
                    "WARM" -> Icons.Default.WbSunny
                    "COLD" -> Icons.Default.AcUnit
                    else -> Icons.Default.Remove
                },
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(priority, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActivityItem(activity: LeadActivity) {
    val icon = when (activity.type) {
        "CALL" -> Icons.Default.Call
        "WHATSAPP" -> Icons.Default.Chat
        "EMAIL" -> Icons.Default.Email
        "MEETING" -> Icons.Default.People
        "NOTE" -> Icons.Default.Note
        "STATUS_CHANGE" -> Icons.Default.SwapHoriz
        "FOLLOW_UP_SCHEDULED" -> Icons.Default.Schedule
        "QUOTATION" -> Icons.Default.Description
        else -> Icons.Default.Info
    }
    val color = when (activity.type) {
        "CALL" -> TeaGreen
        "WHATSAPP" -> Color(0xFF25D366)
        "STATUS_CHANGE" -> PremixGold
        "QUOTATION" -> CoffeeBrown
        else -> Color.Gray
    }
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(30.dp)
                    .background(Color.LightGray.copy(alpha = 0.5f))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(activity.description, style = MaterialTheme.typography.bodyMedium)
            Text(
                dateFormat.format(Date(activity.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ScheduleFollowUpDialog(
    leadName: String,
    onDismiss: () -> Unit,
    onSchedule: (Triple<Int, Int, Int>, Pair<Int, Int>) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var dateTriple by remember { mutableStateOf(Triple(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))) }
    var timePair by remember { mutableStateOf(Pair(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Follow-up for $leadName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        DatePickerDialog(context, { _, year, month, day ->
                            dateTriple = Triple(year, month, day)
                            selectedDate = "$day/${month + 1}/$year"
                        }, dateTriple.first, dateTriple.second, dateTriple.third).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedDate.isNotBlank()) selectedDate else "Select Date")
                }
                OutlinedButton(
                    onClick = {
                        TimePickerDialog(context, { _, hour, minute ->
                            timePair = Pair(hour, minute)
                            selectedTime = String.format("%02d:%02d", hour, minute)
                        }, timePair.first, timePair.minute, true).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedTime.isNotBlank()) selectedTime else "Select Time")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSchedule(dateTriple, timePair) },
                enabled = selectedDate.isNotBlank() && selectedTime.isNotBlank()
            ) { Text("Schedule") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddActivityDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var type by remember { mutableStateOf("NOTE") }
    var description by remember { mutableStateOf("") }
    var showTypeDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Activity") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    ExposedDropdownMenuBox(expanded = showTypeDropdown, onExpandedChange = { showTypeDropdown = it }) {
                        OutlinedTextField(
                            value = type,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = showTypeDropdown, onDismissRequest = { showTypeDropdown = false }) {
                            listOf("CALL", "WHATSAPP", "EMAIL", "MEETING", "NOTE", "OTHER").forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { type = t; showTypeDropdown = false })
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(type, description) },
                enabled = description.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun QuotationDialog(
    lead: Lead?,
    onDismiss: () -> Unit,
    onGenerate: (List<QuotationGenerator.QuotationItem>, String) -> Unit
) {
    var items by remember { mutableStateOf(mutableListOf(
        QuotationGenerator.QuotationItem("Tea Premix (1 pack)", 1, 150.0),
    )) }
    var notes by remember { mutableStateOf("") }
    var showAddItem by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Quotation for ${lead?.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEachIndexed { index, item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Qty: ${item.quantity} x Rs.%.0f = Rs.%.0f".format(item.unitPrice, item.total), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = {
                                items = items.toMutableList().apply { removeAt(index) }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }
                }
                OutlinedButton(
                    onClick = { showAddItem = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Item")
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Total: Rs.%.0f + 18%% GST".format(items.sumOf { it.total }), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TeaGreen)
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(items, notes) },
                enabled = items.isNotEmpty()
            ) { Text("Generate PDF") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )

    if (showAddItem) {
        AddQuotationItemDialog(
            onDismiss = { showAddItem = false },
            onAdd = { name, qty, price ->
                items = items.toMutableList() + QuotationGenerator.QuotationItem(name, qty, price)
                showAddItem = false
            }
        )
    }
}

@Composable
fun AddQuotationItemDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Unit Price (Rs.)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = qty.toIntOrNull() ?: 1
                    val p = price.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && p > 0) onAdd(name, q, p)
                },
                enabled = name.isNotBlank() && price.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun DetailRow(icon: ImageVector, label: String, value: String) {
    if (value.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = TeaGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(text = value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun StatusButton(status: String, currentStatus: String, onStatusSelected: (String) -> Unit) {
    val isSelected = status == currentStatus
    val color = when (status) {
        "NEW" -> StatusNew
        "CONTACTED" -> StatusContacted
        "FOLLOW_UP" -> StatusFollowUp
        "NEGOTIATION" -> PremixGold
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
