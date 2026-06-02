package com.teabiz.crm.ui.screens.imports

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.data.model.ImportResult
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.ImportViewModel
import com.teabiz.crm.util.ExcelParser

@Composable
fun ImportScreen(
    viewModel: ImportViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToGmail: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var parseResult by remember { mutableStateOf<ExcelParser.ParseResult?>(null) }
    var isImporting by remember { mutableStateOf(false) }
    var importComplete by remember { mutableStateOf(false) }

    val importState by viewModel.importState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedUri = uri
                importComplete = false
                scope.launch {
                    parseResult = ExcelParser.parseExcel(context, uri)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Leads") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TeaGreen,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "Import History")
                    }
                }
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Import Sources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                                            "application/vnd.ms-excel",
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "text/csv"
                                        ))
                                    }
                                    filePickerLauncher.launch(intent)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown)
                            ) {
                                Icon(Icons.Default.FileUpload, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel/CSV")
                            }

                            Button(
                                onClick = onNavigateToGmail,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusNew)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gmail")
                            }
                        }

                        selectedUri?.let { uri ->
                            Surface(
                                color = TeaGreen.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = TeaGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = uri.lastPathSegment ?: "Selected file",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            parseResult?.let { result ->
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Preview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem("Total Rows", result.totalRows.toString(), TeaGreen)
                                StatItem("Valid Leads", result.leads.size.toString(), StatusConverted)
                                StatItem("Errors", result.errors.size.toString(), StatusLost)
                            }
                        }
                    }
                }

                if (result.errors.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = StatusLost.copy(alpha = 0.1f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Validation Errors", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = StatusLost)
                                result.errors.take(5).forEach { error ->
                                    Text("Row ${error.rowNumber}: ${error.reason}", style = MaterialTheme.typography.bodySmall, color = StatusLost)
                                }
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            viewModel.importLeads(result.leads)
                            importComplete = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = result.leads.isNotEmpty() && !isImporting,
                        colors = ButtonDefaults.buttonColors(containerColor = TeaGreen)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import ${result.leads.size} Leads")
                    }
                }
            }

            if (importComplete) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusConverted.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusConverted)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Import completed successfully!", color = StatusConverted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Expected Column Headers", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Name, Email, Phone, Product Interest, Message, Company, City, Client Type", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Note: Column names are auto-detected. Headers are case-insensitive.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}
