package com.example.mobile2lab

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data object WorkoutGraph

@Serializable
data object ExerciseList

@Serializable
data class ExerciseDetails(val exerciseId: Int)

@Serializable
data class AddSet(val exerciseId: Int? = null)

fun interface PlaceholderNotifier {
    fun notifyPlaceholder()
}

val LocalPlaceholderNotifier = staticCompositionLocalOf<PlaceholderNotifier> {
    error("PlaceholderNotifier is not provided")
}

private val workoutSetsSaver = Saver<List<WorkoutSet>, String>(
    save = { Json.encodeToString(it) },
    restore = { Json.decodeFromString<List<WorkoutSet>>(it) }
)

@Composable
fun WorkoutScreen() {
    val navController = rememberNavController()
    // Keep the most recent set per exercise above the NavHost, so navigation cannot discard it.
    var savedSets by rememberSaveable(stateSaver = workoutSetsSaver) {
        mutableStateOf(emptyList<WorkoutSet>())
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val placeholderMessage = stringResource(R.string.not_implemented)
    val savedMessage = stringResource(R.string.saved_message)
    val notifier = remember(placeholderMessage, scope, snackbarHostState) {
        PlaceholderNotifier {
            scope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(placeholderMessage, withDismissAction = true)
            }
        }
    }

    CompositionLocalProvider(LocalPlaceholderNotifier provides notifier) {
        Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
            NavHost(
                navController = navController,
                startDestination = WorkoutGraph,
                modifier = Modifier.padding(padding)
            ) {
                navigation<WorkoutGraph>(startDestination = ExerciseList) {
                    composable<ExerciseList> {
                        ExerciseListScreen(
                            savedSets = savedSets,
                            onExerciseClick = { navController.navigate(ExerciseDetails(it)) },
                            onAddSet = { navController.navigate(AddSet()) }
                        )
                    }
                    composable<ExerciseDetails> { entry ->
                        val route = entry.toRoute<ExerciseDetails>()
                        ExerciseDetailsScreen(
                            exercise = exercises.first { it.id == route.exerciseId },
                            savedSet = savedSets.firstOrNull { it.exerciseId == route.exerciseId },
                            onBack = { navController.popBackStack() },
                            onAddSet = { navController.navigate(AddSet(route.exerciseId)) }
                        )
                    }
                    composable<AddSet> { entry ->
                        val route = entry.toRoute<AddSet>()
                        SetEditorScreen(
                            initialExerciseId = route.exerciseId ?: exercises.first().id,
                            onBack = { navController.popBackStack() },
                            onSave = { set ->
                                savedSets = savedSets.filterNot { it.exerciseId == set.exerciseId } + set
                                // The saved result always opens the matching exercise, even if
                                // the user changed the exercise inside the form.
                                navController.navigate(ExerciseDetails(set.exerciseId)) {
                                    popUpTo<ExerciseList>()
                                    launchSingleTop = true
                                }
                                scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                            }
                        )
                    }
                }
            }
        }
    }
}
