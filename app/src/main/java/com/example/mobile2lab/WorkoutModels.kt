package com.example.mobile2lab

import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

data class Exercise(
    val id: Int,
    @param:StringRes val name: Int,
    @param:StringRes val muscles: Int,
    @param:StringRes val description: Int
)

// Stable domain IDs are passed between screens instead of positions or resource IDs.
val exercises = listOf(
    Exercise(1, R.string.squat, R.string.squat_muscles, R.string.squat_description),
    Exercise(2, R.string.bench_press, R.string.bench_press_muscles, R.string.bench_press_description),
    Exercise(3, R.string.deadlift, R.string.deadlift_muscles, R.string.deadlift_description)
)

@Serializable
data class WorkoutSet(
    val exerciseId: Int,
    val weight: String,
    val repetitions: String,
    val completed: Boolean
)
