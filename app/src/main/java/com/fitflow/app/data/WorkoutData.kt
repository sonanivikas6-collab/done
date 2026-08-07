package com.fitflow.app.data

data class Exercise(
    val name: String,
    val durationSeconds: Int,
    val instructions: String
)

data class WorkoutRoutine(
    val id: String,
    val title: String,
    val subtitle: String,
    val estimatedCalories: Int,
    val exercises: List<Exercise>
) {
    val totalDurationSeconds: Int
        get() = exercises.sumOf { it.durationSeconds } + (exercises.size - 1) * REST_SECONDS

    companion object {
        const val REST_SECONDS = 10
    }
}

object WorkoutLibrary {

    val sevenMinuteWorkout = WorkoutRoutine(
        id = "seven_min",
        title = "7-Minute Workout",
        subtitle = "Full body, no equipment",
        estimatedCalories = 60,
        exercises = listOf(
            Exercise("Jumping Jacks", 30, "Jump feet apart while raising arms overhead"),
            Exercise("Wall Sit", 30, "Slide down a wall until knees are at 90°"),
            Exercise("Push-ups", 30, "Keep back straight, lower chest to floor"),
            Exercise("Crunches", 30, "Lie down, curl shoulders toward hips"),
            Exercise("Step-ups", 30, "Step onto a sturdy chair, alternate legs"),
            Exercise("Squats", 30, "Feet shoulder-width, lower hips back and down"),
            Exercise("Tricep Dips", 30, "Use a chair edge, lower and raise your body"),
            Exercise("Plank", 30, "Hold a straight line from head to heels"),
            Exercise("High Knees", 30, "Run in place, driving knees up high"),
            Exercise("Lunges", 30, "Step forward, lower back knee toward floor"),
            Exercise("Push-up + Rotation", 30, "Push-up then rotate into a side plank"),
            Exercise("Side Plank", 30, "Hold on one side, then switch")
        )
    )

    val quickStretch = WorkoutRoutine(
        id = "stretch",
        title = "Quick Stretch",
        subtitle = "Loosen up, 5 minutes",
        estimatedCalories = 15,
        exercises = listOf(
            Exercise("Neck Rolls", 20, "Slowly roll your neck in a circle"),
            Exercise("Shoulder Rolls", 20, "Roll shoulders forward then backward"),
            Exercise("Torso Twists", 30, "Hands on hips, twist gently side to side"),
            Exercise("Forward Fold", 30, "Bend forward, let arms hang toward floor"),
            Exercise("Quad Stretch", 30, "Hold one ankle behind you, switch sides"),
            Exercise("Calf Stretch", 30, "Step one leg back, press heel down")
        )
    )

    val coreBlast = WorkoutRoutine(
        id = "core",
        title = "Core Blast",
        subtitle = "Abs & lower back, 6 minutes",
        estimatedCalories = 45,
        exercises = listOf(
            Exercise("Plank", 40, "Hold a straight line from head to heels"),
            Exercise("Bicycle Crunches", 30, "Alternate elbow to opposite knee"),
            Exercise("Leg Raises", 30, "Lie flat, raise legs to 90° and lower slowly"),
            Exercise("Superman Hold", 30, "Lie face down, lift arms and legs"),
            Exercise("Russian Twists", 30, "Sit back slightly, twist side to side"),
            Exercise("Side Plank (L)", 20, "Hold on left forearm, hips lifted"),
            Exercise("Side Plank (R)", 20, "Hold on right forearm, hips lifted")
        )
    )

    val cardioBurn = WorkoutRoutine(
        id = "cardio",
        title = "Cardio Burn",
        subtitle = "Heart-pumping, 8 minutes",
        estimatedCalories = 80,
        exercises = listOf(
            Exercise("Jumping Jacks", 40, "Jump feet apart while raising arms overhead"),
            Exercise("High Knees", 40, "Run in place, driving knees up high"),
            Exercise("Butt Kicks", 40, "Jog in place, kicking heels to glutes"),
            Exercise("Mountain Climbers", 40, "Plank position, drive knees to chest fast"),
            Exercise("Squat Jumps", 30, "Squat down, jump up explosively"),
            Exercise("Jumping Jacks", 40, "Jump feet apart while raising arms overhead")
        )
    )

    val all: List<WorkoutRoutine> = listOf(sevenMinuteWorkout, coreBlast, cardioBurn, quickStretch)
}
