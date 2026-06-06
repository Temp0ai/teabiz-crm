package com.teabiz.crm.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector, @DrawableRes val iconRes: Int = 0) {
    object Dashboard : Screen("dashboard", "Home", Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
    object Leads : Screen("leads", "Leads", Icons.Outlined.People, Icons.Filled.People)
    object Import : Screen("import", "Import", Icons.Outlined.FileUpload, Icons.Filled.FileUpload)
    object Campaigns : Screen("campaigns", "WhatsApp", Icons.AutoMirrored.Outlined.Chat, Icons.AutoMirrored.Filled.Chat, com.teabiz.crm.R.drawable.ic_whatsapp)
    object Marketing : Screen("marketing", "Marketing", Icons.AutoMirrored.Outlined.TrendingUp, Icons.AutoMirrored.Filled.TrendingUp)
    object Settings : Screen("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)

    object LeadDetail : Screen("lead_detail/{leadId}", "Lead Detail", Icons.Outlined.Person, Icons.Filled.Person) {
        fun createRoute(leadId: String) = "lead_detail/$leadId"
    }
    object AddLead : Screen("add_lead", "Add Lead", Icons.Outlined.PersonAdd, Icons.Filled.PersonAdd)
    object ImportHistory : Screen("import_history", "Import History", Icons.Outlined.History, Icons.Filled.History)
    object AiFollowUp : Screen("ai_followup/{leadId}", "AI Follow-up", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome) {
        fun createRoute(leadId: String) = "ai_followup/$leadId"
    }
    object GmailImport : Screen("gmail_import", "Add Lead", Icons.Outlined.Email, Icons.Filled.Email)
    object SeoTools : Screen("seo_tools", "SEO Tools", Icons.Outlined.Search, Icons.Filled.Search)
    object CompetitorAnalysis : Screen("competitor_analysis", "Competitors", Icons.Outlined.Analytics, Icons.Filled.Analytics)
    object ContentCalendar : Screen("content_calendar", "Content Calendar", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth)
    object GmbManagement : Screen("gmb_management", "Google My Business", Icons.Outlined.Business, Icons.Filled.Business)
    object WhatsAppCatalog : Screen("whatsapp_catalog", "Catalog", Icons.Outlined.Store, Icons.Filled.Store)
    object AiSalesDashboard : Screen("ai_sales_dashboard", "AI Assistant", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome)
    object HashtagGenerator : Screen("hashtag_generator", "Hashtags", Icons.Outlined.Tag, Icons.Filled.Tag)
    object AiMediaGenerator : Screen("ai_media_generator", "Media AI", Icons.Outlined.VideoLibrary, Icons.Filled.VideoLibrary)
    object AiVideoGenerator : Screen("ai_video_generator", "Video AI", Icons.Outlined.MovieCreation, Icons.Filled.MovieCreation)
    object WhatsAppOffer : Screen("whatsapp_offer", "Catalog & Offers", Icons.Outlined.Storefront, Icons.Filled.Storefront)
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val badgeCount: Int = 0
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home"),
    BottomNavItem(Screen.Leads, "Leads"),
    BottomNavItem(Screen.Import, "Import"),
    BottomNavItem(Screen.Campaigns, "WhatsApp"),
    BottomNavItem(Screen.Marketing, "Marketing"),
    BottomNavItem(Screen.Settings, "More")
)
