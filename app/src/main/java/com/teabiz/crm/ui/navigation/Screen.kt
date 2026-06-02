package com.teabiz.crm.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Leads : Screen("leads", "Leads", Icons.Default.People)
    object Import : Screen("import", "Import", Icons.Default.FileUpload)
    object Campaigns : Screen("campaigns", "Campaigns", Icons.Default.Campaign)
    object Marketing : Screen("marketing", "Marketing", Icons.Default.TrendingUp)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)

    object LeadDetail : Screen("lead_detail/{leadId}", "Lead Detail", Icons.Default.Person) {
        fun createRoute(leadId: String) = "lead_detail/$leadId"
    }
    object AddLead : Screen("add_lead", "Add Lead", Icons.Default.PersonAdd)
    object ImportHistory : Screen("import_history", "Import History", Icons.Default.History)
    object AiFollowUp : Screen("ai_followup/{leadId}", "AI Follow-up", Icons.Default.AutoAwesome) {
        fun createRoute(leadId: String) = "ai_followup/$leadId"
    }
    object GmailImport : Screen("gmail_import", "Gmail Import", Icons.Default.Email)
    object SeoTools : Screen("seo_tools", "SEO Tools", Icons.Default.Search)
    object CompetitorAnalysis : Screen("competitor_analysis", "Competitors", Icons.Default.Analytics)
    object ContentCalendar : Screen("content_calendar", "Content Calendar", Icons.Default.CalendarMonth)
    object GmbManagement : Screen("gmb_management", "Google My Business", Icons.Default.Business)
    object WhatsAppCatalog : Screen("whatsapp_catalog", "Catalog", Icons.Default.Store)
}

data class BottomNavItem(
    val screen: Screen,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard"),
    BottomNavItem(Screen.Leads, "Leads"),
    BottomNavItem(Screen.Import, "Import"),
    BottomNavItem(Screen.Campaigns, "Campaigns"),
    BottomNavItem(Screen.Marketing, "Marketing"),
    BottomNavItem(Screen.Settings, "Settings")
)
