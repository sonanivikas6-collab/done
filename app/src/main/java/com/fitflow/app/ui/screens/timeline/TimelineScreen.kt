package com.fitflow.app.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitflow.app.ads.FrequencyGuard
import com.fitflow.app.ads.NativeAdCard
import com.fitflow.app.ui.TimelineDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    days: List<TimelineDay>,
    weekTotalSteps: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timeline") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (days.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No history yet — steps will appear here day by day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("This Week", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$weekTotalSteps steps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            days.forEachIndexed { index, day ->
                item(key = day.dateKey) {
                    TimelineRow(day)
                }
                val position = index + 1
                if (position % FrequencyGuard.NATIVE_AD_EVERY_N_ITEMS == 0 && position != days.size) {
                    item(key = "native_ad_$position") {
                        NativeAdCard()
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(day: TimelineDay) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Text(
                day.dayLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricChip(
                    icon = Icons.Filled.DirectionsWalk,
                    tint = Color(0xFFFF6B35),
                    text = "${day.steps}"
                )
                MetricChip(
                    icon = Icons.Filled.LocalFireDepartment,
                    tint = Color(0xFFFF9457),
                    text = "${day.calories}"
                )
                MetricChip(
                    icon = Icons.Filled.Route,
                    tint = Color(0xFF00C896),
                    text = String.format("%.2f km", day.distanceKm)
                )
            }
        }
    }
}

@Composable
private fun MetricChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
