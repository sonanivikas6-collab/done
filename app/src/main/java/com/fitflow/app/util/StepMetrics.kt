package com.fitflow.app.util

import kotlin.math.roundToInt

object StepMetrics {

    private const val METERS_PER_STEP = 0.762
    private const val KCAL_PER_STEP = 0.04

    fun distanceKm(steps: Int): Double {
        val meters = steps * METERS_PER_STEP
        return meters / 1000.0
    }

    fun distanceMiles(steps: Int): Double {
        return distanceKm(steps) * 0.621371
    }

    fun calories(steps: Int): Int {
        return (steps * KCAL_PER_STEP).roundToInt()
    }

    fun estimatedMinutes(steps: Int): Int {
        return (steps / 100.0).roundToInt()
    }

    fun formatDistanceKm(steps: Int): String {
        return String.format("%.2f", distanceKm(steps))
    }
}
