package com.teabiz.crm.ui.screens.imports

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
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
import com.teabiz.crm.data.remote.GmailService
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.ImportViewModel

@Composable
fun GmailImportScreen(
    viewModel: ImportViewModel,
    onBack: () -> Unit
) {
    val gmailAuthState by viewModel.gmailAuthState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gmail Import") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Gmail Connection", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    if (gmailAuthState.isAuthenticated) {
                        Surface(
                            color = StatusConverted.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusConverted)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Connected to Gmail", color = StatusConverted)
                            }
                        }
                    } else {
                        if (gmailAuthState.error != null) {
                            Surface(
                                color = StatusLost.copy(alpha = 0.1f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = StatusLost)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Error: ${gmailAuthState.error}", color = StatusLost)
                                }
                            }
                        }

                        Text("Connect your Gmail account to import leads from emails.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)

                        Button(
                            onClick = {
                                val authUrl = "https://accounts.google.com/o/oauth2/v2/auth?" +
                                    "client_id=${GmailService.CLIENT_ID}" +
                                    "&redirect_uri=${Uri.encode(GmailService.REDIRECT_URI)}" +
                                    "&response_type=code" +
                                    "&scope=${Uri.encode(GmailService.SCOPE)}" +
                                    "&access_type=offline" +
                                    "&prompt=consent"

                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusNew)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect Gmail Account")
                        }

                        Text(
                            "Note: Configure your Gmail Client ID in Settings before connecting.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Search & Import", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search Query") },
                        placeholder = { Text("e.g., subject:inquiry product:tea") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                    )

                    Button(
                        onClick = {
                            viewModel.searchGmailLeads(searchQuery)
                            isSearching = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = searchQuery.isNotBlank() && !isSearching && gmailAuthState.isAuthenticated,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusNew)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Searching...")
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search Emails")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Search Tips", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("• Use keywords like 'inquiry', 'quote', 'price' to find leads", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• Search for product names: 'tea premix', 'coffee machine'", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• Filter by sender: 'from:@company.com'", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("• Contact details are extracted automatically from email content", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}
