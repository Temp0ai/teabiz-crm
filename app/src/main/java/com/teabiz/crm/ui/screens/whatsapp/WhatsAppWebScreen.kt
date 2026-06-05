package com.teabiz.crm.ui.screens.whatsapp

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppWebScreen(
    onBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var currentUrl by remember { mutableStateOf("https://web.whatsapp.com") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var showQRHelp by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("WhatsApp Web")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showQRHelp = true }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF075E54)
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { loadProgress },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF25D366)
                )
            }

            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(error, color = Color.Red, modifier = Modifier.weight(1f))
                        IconButton(onClick = { errorMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }
            }

            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webView = this
                        
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            builtInZoomControls = true
                            displayZoomControls = false
                            allowFileAccess = true
                            allowContentAccess = true
                            javaScriptCanOpenWindowsAutomatically = true
                            mediaPlaybackRequiresUserGesture = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            databaseEnabled = true
                            setSupportMultipleWindows(false)
                            userAgentString = "Mozilla/5.0 (Linux; Android 13; SM-A546B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                        }

                        webViewClient = object : WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                currentUrl = url
                                return false
                            }

                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                isLoading = true
                                errorMessage = null
                                currentUrl = url ?: "https://web.whatsapp.com"
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                loadProgress = 1f
                                
                                view?.evaluateJavascript("""
                                    (function() {
                                        // Add mobile viewport meta tag
                                        var meta = document.createElement('meta');
                                        meta.name = 'viewport';
                                        meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
                                        document.head.appendChild(meta);
                                        
                                        // Override CSS for mobile
                                        var style = document.createElement('style');
                                        style.textContent = `
                                            * { max-width: 100% !important; }
                                            body { overflow-x: hidden !important; }
                                            #app { width: 100% !important; }
                                            .app-wrapper-web { width: 100% !important; min-height: 100vh !important; }
                                            .two { width: 100% !important; }
                                            .landing-main { width: 100% !important; }
                                        `;
                                        document.head.appendChild(style);
                                    })();
                                """.trimIndent(), null)
                            }

                            override fun onReceivedSslError(
                                view: WebView?,
                                handler: SslErrorHandler?,
                                error: SslError?
                            ) {
                                handler?.proceed()
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                if (request?.isForMainFrame == true) {
                                    isLoading = false
                                    errorMessage = "Connection error. Please check your internet and try again."
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                loadProgress = newProgress / 100f
                            }
                        }

                        loadUrl("https://web.whatsapp.com")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showQRHelp) {
        AlertDialog(
            onDismissRequest = { showQRHelp = false },
            title = { Text("How to Connect") },
            text = {
                Column {
                    Text("1. Open WhatsApp on your phone")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("2. Tap Menu (⋮) → Linked Devices")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3. Tap 'Link a Device'")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("4. Scan the QR code shown on screen")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Note: You need WhatsApp Business app installed on your phone.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showQRHelp = false }) {
                    Text("OK")
                }
            }
        )
    }
}
