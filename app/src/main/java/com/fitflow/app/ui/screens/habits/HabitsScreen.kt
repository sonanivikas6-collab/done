package com.fitflow.app.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val emojiOptions = listOf("💧", "🏋️", "😴", "🥗", "📖", "🧘", "🚭", "☀️")

@Composable
fun HabitsScreen(
    habits: List<com.fitflow.app.ui.HabitUiState>,
    onAddHabit: (name: String, emoji: String, hour: Int?, minute: Int?) -> Unit,
    onDeleteHabit: (com.fitflow.app.data.db.Habit) -> Unit,
    onToggleHabit: (Long, Boolean) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add habit")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Habits", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
            }

            if (habits.isEmpty()) {
                item {
                    Text(
                        "Tap + to add your first habit, like drinking water or reading.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(habits, key = { it.habit.id }) { h ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${h.habit.emoji}  ${h.habit.name}", style = MaterialTheme.typography.bodyLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (h.streak > 0) {
                                    Text("🔥 ${h.streak} day streak  ", style = MaterialTheme.typography.labelSmall)
                                }
                                if (h.habit.reminderEnabled) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = "Reminder on",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                        Row {
                            Checkbox(
                                checked = h.completedToday,
                                onCheckedChange = { onToggleHabit(h.habit.id, h.completedToday) }
                            )
                            IconButton(onClick = { onDeleteHabit(h.habit) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete habit")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, emoji, hour, minute ->
                onAddHabit(name, emoji, hour, minute)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int?, Int?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf(emojiOptions.first()) }
    var reminderOn by remember { mutableStateOf(false) }
    var hour by remember { mutableStateOf(9) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Habit") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Habit name") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                Text("Icon", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    emojiOptions.forEach { emoji ->
                        FilterChip(
                            selected = selectedEmoji == emoji,
                            onClick = { selectedEmoji = emoji },
                            label = { Text(emoji) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderOn, onCheckedChange = { reminderOn = it })
                    Text("Daily reminder")
                }
                if (reminderOn) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Hour (24h): ")
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = { v ->
                                hour = v.filter { it.isDigit() }.toIntOrNull()?.coerceIn(0, 23) ?: hour
                            },
                            modifier = Modifier.width(80.dp),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), selectedEmoji, if (reminderOn) hour else null, if (reminderOn) 0 else null)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
