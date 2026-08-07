package com.fitflow.app.ui.screens.steps

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitflow.app.ui.DashboardUiState

@Composable
fun StepsScreen(
    state: DashboardUiState,
    onGoalChange: (Int) -> Unit
) {
    var goalText by remember(state.stepGoal) { mutableStateOf(state.stepGoal.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Steps", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = {
                    if (state.stepGoal > 0) (state.todaySteps.toFloat() / state.stepGoal).coerceIn(0f, 1f) else 0f
                },
                modifier = Modifier.size(220.dp),
                strokeWidth = 16.dp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${state.todaySteps}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("of ${state.stepGoal} steps", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(String.format("%.2f km", state.todayDistanceKm), fontWeight = FontWeight.Bold)
                Text("Distance", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${state.todayCalories} kcal", fontWeight = FontWeight.Bold)
                Text("Calories", style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${state.todayMinutes} min", fontWeight = FontWeight.Bold)
                Text("Active Time", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(Modifier.height(32.dp))

        if (!state.sensorAvailable) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Text(
                    "This device doesn't report a step-counter sensor, so live step tracking isn't available here.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        Text("Daily Goal", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it.filter { c -> c.isDigit() } },
                modifier = Modifier.width(140.dp),
                singleLine = true
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    val goal = goalText.toIntOrNull() ?: state.stepGoal
                    onGoalChange(goal)
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save")
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Weekly History", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        state.weekSteps.forEach { (label, steps) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label)
                Text("$steps steps")
            }
        }
    }
}
