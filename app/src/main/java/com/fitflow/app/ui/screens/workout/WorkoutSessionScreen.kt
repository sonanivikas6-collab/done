package com.fitflow.app.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitflow.app.data.WorkoutRoutine
import kotlinx.coroutines.delay

private enum class SessionPhase { EXERCISE, REST, DONE }

@Composable
fun WorkoutSessionScreen(
    routine: WorkoutRoutine,
    onFinished: (durationSeconds: Int, calories: Int) -> Unit,
    onExit: () -> Unit
) {
    var exerciseIndex by remember { mutableStateOf(0) }
    var phase by remember { mutableStateOf(SessionPhase.EXERCISE) }
    var secondsLeft by remember { mutableStateOf(routine.exercises.first().durationSeconds) }
    var isPaused by remember { mutableStateOf(false) }
    var elapsedTotal by remember { mutableStateOf(0) }

    val currentExercise = routine.exercises.getOrNull(exerciseIndex)

    LaunchedEffect(exerciseIndex, phase, isPaused) {
        if (phase == SessionPhase.DONE || isPaused) return@LaunchedEffect
        while (secondsLeft > 0) {
            delay(1000)
            if (isPaused) return@LaunchedEffect
            secondsLeft -= 1
            elapsedTotal += 1
        }
        if (phase == SessionPhase.EXERCISE) {
            val hasRest = exerciseIndex < routine.exercises.lastIndex
            if (hasRest) {
                phase = SessionPhase.REST
                secondsLeft = WorkoutRoutine.REST_SECONDS
            } else {
                phase = SessionPhase.DONE
            }
        } else if (phase == SessionPhase.REST) {
            exerciseIndex += 1
            phase = SessionPhase.EXERCISE
            secondsLeft = routine.exercises.getOrNull(exerciseIndex)?.durationSeconds ?: 0
        }
    }

    if (phase == SessionPhase.DONE) {
        WorkoutCompleteView(
            routine = routine,
            onDone = { onFinished(elapsedTotal, routine.estimatedCalories) }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (phase == SessionPhase.REST) "Rest" else "Exercise ${exerciseIndex + 1} / ${routine.exercises.size}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (phase == SessionPhase.REST) "Get ready..." else (currentExercise?.name ?: ""),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = {
                    val total = if (phase == SessionPhase.REST) WorkoutRoutine.REST_SECONDS else (currentExercise?.durationSeconds ?: 1)
                    1f - (secondsLeft.toFloat() / total.toFloat())
                },
                modifier = Modifier.size(180.dp),
                strokeWidth = 12.dp,
                color = if (phase == SessionPhase.REST) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
            )
            Text("$secondsLeft", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))

        if (phase == SessionPhase.EXERCISE && currentExercise != null) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Text(
                    currentExercise.instructions,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { isPaused = !isPaused }, shape = RoundedCornerShape(16.dp)) {
                Icon(if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (isPaused) "Resume" else "Pause")
            }
            Button(
                onClick = { secondsLeft = 0 },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Skip")
            }
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onExit) {
            Text("Exit workout")
        }
    }
}

@Composable
private fun WorkoutCompleteView(routine: WorkoutRoutine, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(16.dp))
        Text("Workout Complete!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "You finished ${routine.title} — approx ${routine.estimatedCalories} kcal burned.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone, shape = RoundedCornerShape(16.dp)) {
            Text("Done")
        }
    }
}
