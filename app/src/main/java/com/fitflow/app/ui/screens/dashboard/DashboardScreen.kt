package com.fitflow.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitflow.app.ads.BannerAdView
import com.fitflow.app.ui.DashboardUiState
import com.fitflow.app.util.DateUtils
import kotlin.math.max

private val OrangeStart = Color(0xFFFF6B35)
private val OrangeEnd = Color(0xFFFF9457)

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onToggleHabit: (Long, Boolean) -> Unit,
    onStartWorkout: () -> Unit,
    onOpenTimeline: () -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(DateUtils.displayToday(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Hi, ${state.displayName} 👋", style = MaterialTheme.typography.headlineMedium)
                }
                Row {
                    TextButton(onClick = onOpenTimeline) { Text("Timeline") }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            }
        }

        item {
            BigStepGauge(
                steps = state.todaySteps,
                goal = state.stepGoal,
                sensorAvailable = state.sensorAvailable
            )
        }

        item {
            StatsRow(
                distanceKm = state.todayDistanceKm,
                calories = state.todayCalories,
                minutes = state.todayMinutes
            )
        }

        item {
            WeeklyStepsChart(weekSteps = state.weekSteps, weekTotal = state.weekTotalSteps)
        }

        item {
            WorkoutStatusCard(done = state.workoutDoneToday, onStartWorkout = onStartWorkout)
        }

        item {
            Text("Today's Habits", style = MaterialTheme.typography.titleLarge)
        }

        if (state.habits.isEmpty()) {
            item {
                Text(
                    "No habits yet. Add one from the Habits tab!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(state.habits) { habitState ->
                HabitRow(
                    name = habitState.habit.emoji + "  " + habitState.habit.name,
                    completed = habitState.completedToday,
                    streak = habitState.streak,
                    onToggle = { onToggleHabit(habitState.habit.id, habitState.completedToday) }
                )
            }
        }

        item {
            BannerAdView()
        }
    }
}

@Composable
private fun BigStepGauge(steps: Int, goal: Int, sensorAvailable: Boolean) {
    val progress = if (goal > 0) (steps.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(OrangeStart, OrangeEnd)))
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Goal: $goal steps",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(190.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        strokeWidth = 14.dp
                    )
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(190.dp),
                        color = Color.White,
                        strokeWidth = 14.dp
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "$steps",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "steps today",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (!sensorAvailable) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Step sensor not found on this device",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsRow(distanceKm: Double, calories: Int, minutes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            icon = Icons.Filled.Route,
            iconTint = Color(0xFFFF9457),
            value = String.format("%.2f", distanceKm),
            unit = "km",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Filled.LocalFireDepartment,
            iconTint = Color(0xFFFF6B35),
            value = "$calories",
            unit = "kcal",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            icon = Icons.Filled.Timer,
            iconTint = Color(0xFF00C896),
            value = "$minutes",
            unit = "min",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Card(shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun WeeklyStepsChart(weekSteps: List<Pair<String, Int>>, weekTotal: Int) {
    val maxSteps = max(weekSteps.maxOfOrNull { it.second } ?: 1, 1)
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("This Week", style = MaterialTheme.typography.titleMedium)
                Text(
                    "$weekTotal steps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weekSteps.forEach { (label, steps) ->
                    val barHeight = (steps.toFloat() / maxSteps.toFloat()).coerceIn(0.05f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight(barHeight)
                                .background(
                                    Brush.verticalGradient(listOf(OrangeStart, OrangeEnd)),
                                    RoundedCornerShape(6.dp)
                                )
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(label, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutStatusCard(done: Boolean, onStartWorkout: () -> Unit) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Workout", style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (done) "Completed today 🎉" else "Not done yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (!done) {
                Button(onClick = onStartWorkout, shape = RoundedCornerShape(16.dp)) {
                    Text("Start")
                }
            }
        }
    }
}

@Composable
private fun HabitRow(name: String, completed: Boolean, streak: Int, onToggle: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(name, style = MaterialTheme.typography.bodyLarge)
                if (streak > 0) {
                    Text(
                        "🔥 $streak day streak",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (completed) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Toggle habit",
                    tint = if (completed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
