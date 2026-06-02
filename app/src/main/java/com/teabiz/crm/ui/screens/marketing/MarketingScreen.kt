package com.teabiz.crm.ui.screens.marketing

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teabiz.crm.ui.theme.*
import com.teabiz.crm.ui.viewmodel.MarketingViewModel

@Composable
fun MarketingScreen(
    viewModel: MarketingViewModel,
    onNavigateToSeo: () -> Unit,
    onNavigateToCompetitors: () -> Unit,
    onNavigateToContent: () -> Unit,
    onNavigateToGmb: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Marketing Tools",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Grow your tea & coffee business with AI-powered marketing",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        MarketingCard(
            title = "SEO & Keyword Research",
            description = "Find the best keywords for your tea/coffee business. Analyze search volume and competition.",
            icon = Icons.Default.Search,
            color = TeaGreen,
            onClick = onNavigateToSeo
        )

        MarketingCard(
            title = "Competitor Analysis",
            description = "Analyze your competitors' strategies and find opportunities to outperform them.",
            icon = Icons.Default.Analytics,
            color = CoffeeBrown,
            onClick = onNavigateToCompetitors
        )

        MarketingCard(
            title = "Content Calendar",
            description = "Plan and schedule social media content. Generate AI-powered posts for Instagram, Facebook.",
            icon = Icons.Default.CalendarMonth,
            color = PremixGold,
            onClick = onNavigateToContent
        )

        MarketingCard(
            title = "Google My Business",
            description = "Manage your GMB profile, respond to reviews, and boost local SEO.",
            icon = Icons.Default.Business,
            color = StatusNew,
            onClick = onNavigateToGmb
        )

        Card(modifier = Modifier.fillMaxWidth(), elevation = 4.dp) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Quick Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                val keywords by viewModel.keywords.collectAsState()
                val competitors by viewModel.competitors.collectAsState()
                val content by viewModel.contentCalendar.collectAsState()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(keywords.size.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TeaGreen)
                        Text("Keywords", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(competitors.size.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = CoffeeBrown)
                        Text("Competitors", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(content.size.toString(), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PremixGold)
                        Text("Posts", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun MarketingCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}
