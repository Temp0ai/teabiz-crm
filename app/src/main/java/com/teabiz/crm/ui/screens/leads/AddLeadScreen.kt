package com.teabiz.crm.ui.screens.leads

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.data.model.ProductCategory
import com.teabiz.crm.data.model.ClientType
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.LeadsViewModel
import java.util.UUID

@Composable
fun AddLeadScreen(
    viewModel: LeadsViewModel,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var clientType by remember { mutableStateOf("") }
    var selectedProducts by remember { mutableStateOf(setOf<String>()) }
    var message by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Lead") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Contact Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                    OutlinedTextField(
                        value = company,
                        onValueChange = { company = it },
                        label = { Text("Company") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) }
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Product Interest", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ProductCategory.entries.forEach { category ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Checkbox(
                                checked = selectedProducts.contains(category.displayName),
                                onCheckedChange = { checked ->
                                    selectedProducts = if (checked) {
                                        selectedProducts + category.displayName
                                    } else {
                                        selectedProducts - category.displayName
                                    }
                                }
                            )
                            Text(
                                text = category.displayName,
                                modifier = Modifier.padding(start = 8.dp, top = 12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Additional Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Text("Client Type", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ClientType.entries.take(4).forEach { type ->
                            FilterChip(
                                selected = clientType == type.displayName,
                                onClick = { clientType = type.displayName },
                                label = { Text(type.displayName, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message/Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val lead = Lead(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            email = email,
                            phone = phone,
                            company = company,
                            city = city,
                            clientType = clientType,
                            productInterest = selectedProducts.toList(),
                            message = message,
                            source = "MANUAL"
                        )
                        viewModel.addLead(lead)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Lead")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
