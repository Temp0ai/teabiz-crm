package com.teabiz.crm.ui.screens.imports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.ImportViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GmailImportScreen(
    viewModel: ImportViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var productInterest by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("MANUAL") }
    var showSuccess by remember { mutableStateOf(false) }
    val importState by viewModel.importState.collectAsState()

    LaunchedEffect(importState) {
        if (importState is ImportViewModel.ImportState.Completed) {
            showSuccess = true
            name = ""
            email = ""
            phone = ""
            productInterest = ""
            message = ""
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Lead") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StatusNew,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showSuccess) {
                Surface(
                    color = StatusConverted.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusConverted)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Lead added successfully!", color = StatusConverted)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Lead Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Contact Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = productInterest,
                        onValueChange = { productInterest = it },
                        label = { Text("Product Interest") },
                        placeholder = { Text("e.g., Tea Premix, Coffee Machine") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Notes / Message") },
                        placeholder = { Text("Any details from the inquiry...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) }
                    )

                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("Source") },
                        placeholder = { Text("e.g., EMAIL, WHATSAPP, REFERRAL") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Source, contentDescription = null) }
                    )
                }
            }

            Button(
                onClick = {
                    val products = productInterest.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val lead = Lead(
                        name = name.trim(),
                        email = email.trim(),
                        phone = phone.trim(),
                        productInterest = products.ifEmpty { listOf("Other") },
                        message = message.trim(),
                        source = source.trim().ifBlank { "MANUAL" },
                        status = "NEW"
                    )
                    viewModel.importLeads(listOf(lead))
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && importState !is ImportViewModel.ImportState.Processing,
                colors = ButtonDefaults.buttonColors(containerColor = StatusNew)
            ) {
                if (importState is ImportViewModel.ImportState.Processing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Lead")
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Quick Tips", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("• Fill in the contact name (required) and as many details as possible", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• Add multiple products separated by commas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• Use the source field to track where the lead came from", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}
