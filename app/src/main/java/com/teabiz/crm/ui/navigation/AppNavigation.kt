package com.teabiz.crm.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teabiz.crm.ui.screens.campaigns.CampaignsScreen
import com.teabiz.crm.ui.screens.dashboard.DashboardScreen
import com.teabiz.crm.ui.screens.imports.ImportScreen
import com.teabiz.crm.ui.screens.leads.AddLeadScreen
import com.teabiz.crm.ui.screens.leads.LeadDetailScreen
import com.teabiz.crm.ui.screens.leads.LeadsScreen
import com.teabiz.crm.ui.screens.marketing.MarketingScreen
import com.teabiz.crm.ui.screens.marketing.SeoToolsScreen
import com.teabiz.crm.ui.screens.marketing.CompetitorScreen
import com.teabiz.crm.ui.screens.marketing.ContentCalendarScreen
import com.teabiz.crm.ui.screens.marketing.GmbScreen
import com.teabiz.crm.ui.screens.settings.SettingsScreen
import com.teabiz.crm.ui.screens.whatsapp.WhatsAppCatalogScreen
import com.teabiz.crm.ui.screens.ai.AiSalesDashboardScreen
import com.teabiz.crm.ui.theme.TeaGreen
import com.teabiz.crm.ui.viewmodel.*

@Composable
fun AppNavigation(
    dashboardViewModel: DashboardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    leadsViewModel: LeadsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    importViewModel: ImportViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    campaignsViewModel: CampaignsViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    marketingViewModel: MarketingViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    settingsViewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route?.let { route ->
        route == Screen.Dashboard.route ||
        route == Screen.Leads.route ||
        route == Screen.Import.route ||
        route == Screen.Campaigns.route ||
        route == Screen.Marketing.route ||
        route == Screen.Settings.route ||
        route.startsWith("lead_detail") ||
        route.startsWith("add_lead") ||
        route.startsWith("ai_followup") ||
        route.startsWith("gmail_import") ||
        route.startsWith("whatsapp_catalog") ||
        route.startsWith("ai_sales_dashboard") ||
        route.startsWith("seo_tools") ||
        route.startsWith("competitor") ||
        route.startsWith("content_calendar") ||
        route.startsWith("gmb") ||
        route.startsWith("hashtag_generator") ||
        route.startsWith("product_catalog")
    } ?: false

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            icon = {
                                if (item.screen.iconRes != 0) {
                                    Icon(
                                        painter = painterResource(id = item.screen.iconRes),
                                        contentDescription = item.label,
                                        tint = if (selected) TeaGreen else Color.Gray
                                    )
                                } else {
                                    Icon(
                                        if (selected) item.screen.selectedIcon else item.screen.icon,
                                        contentDescription = item.label
                                    )
                                }
                            },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = TeaGreen,
                                selectedTextColor = TeaGreen,
                                indicatorColor = TeaGreen.copy(alpha = 0.12f),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToLeads = { navController.navigate(Screen.Leads.route) },
                    onNavigateToImport = { navController.navigate(Screen.Import.route) },
                    onNavigateToAiDashboard = { navController.navigate(Screen.AiSalesDashboard.route) }
                )
            }

            composable(Screen.Leads.route) {
                LeadsScreen(
                    viewModel = leadsViewModel,
                    onLeadClick = { leadId ->
                        navController.navigate(Screen.LeadDetail.createRoute(leadId))
                    },
                    onAddLead = { navController.navigate(Screen.AddLead.route) }
                )
            }

            composable(Screen.Import.route) {
                ImportScreen(
                    viewModel = importViewModel,
                    onNavigateToHistory = { navController.navigate(Screen.ImportHistory.route) },
                    onNavigateToGmail = { navController.navigate(Screen.GmailImport.route) }
                )
            }

            composable(Screen.Campaigns.route) {
                CampaignsScreen(
                    viewModel = campaignsViewModel
                )
            }

            composable(Screen.Marketing.route) {
                MarketingScreen(
                    viewModel = marketingViewModel,
                    onNavigateToSeo = { navController.navigate(Screen.SeoTools.route) },
                    onNavigateToCompetitors = { navController.navigate(Screen.CompetitorAnalysis.route) },
                    onNavigateToContent = { navController.navigate(Screen.ContentCalendar.route) },
                    onNavigateToGmb = { navController.navigate(Screen.GmbManagement.route) },
                    onNavigateToHashtags = { navController.navigate(Screen.HashtagGenerator.route) },
                    onNavigateToProductCatalog = { navController.navigate("product_catalog") },
                    onNavigateToAiMedia = { navController.navigate(Screen.AiMediaGenerator.route) },
                    onNavigateToAiVideo = { navController.navigate(Screen.AiVideoGenerator.route) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable(Screen.HashtagGenerator.route) {
                val hashtagViewModel: HashtagViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                com.teabiz.crm.ui.screens.marketing.HashtagGeneratorScreen(
                    viewModel = hashtagViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.LeadDetail.route,
                arguments = listOf(navArgument("leadId") { type = NavType.StringType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getString("leadId") ?: return@composable
                LeadDetailScreen(
                    leadId = leadId,
                    leadsViewModel = leadsViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToAiFollowUp = { id ->
                        navController.navigate(Screen.AiFollowUp.createRoute(id))
                    }
                )
            }

            composable(Screen.AddLead.route) {
                AddLeadScreen(
                    viewModel = leadsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GmailImport.route) {
                com.teabiz.crm.ui.screens.imports.GmailImportScreen(
                    viewModel = importViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ImportHistory.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Import History - Coming Soon")
                }
            }

            composable(
                route = Screen.AiFollowUp.route,
                arguments = listOf(navArgument("leadId") { type = NavType.StringType })
            ) { backStackEntry ->
                val leadId = backStackEntry.arguments?.getString("leadId") ?: return@composable
                com.teabiz.crm.ui.screens.leads.AiFollowUpScreen(
                    leadId = leadId,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SeoTools.route) {
                SeoToolsScreen(
                    viewModel = marketingViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CompetitorAnalysis.route) {
                CompetitorScreen(
                    viewModel = marketingViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ContentCalendar.route) {
                ContentCalendarScreen(
                    viewModel = marketingViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.GmbManagement.route) {
                GmbScreen(
                    viewModel = marketingViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.WhatsAppCatalog.route) {
                val whatsappCatalogViewModel: com.teabiz.crm.ui.viewmodel.WhatsAppCatalogViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                WhatsAppCatalogScreen(
                    viewModel = whatsappCatalogViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AiSalesDashboard.route) {
                AiSalesDashboardScreen(
                    leadsViewModel = leadsViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("product_catalog") {
                com.teabiz.crm.ui.screens.marketing.ProductCatalogScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AiMediaGenerator.route) {
                com.teabiz.crm.ui.screens.media.AiMediaScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AiVideoGenerator.route) {
                com.teabiz.crm.ui.screens.media.AiVideoScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
