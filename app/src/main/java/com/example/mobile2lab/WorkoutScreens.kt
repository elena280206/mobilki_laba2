package com.example.mobile2lab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("navigate_back")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        },
        windowInsets = WindowInsets(0, 0, 0, 0)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseListScreen(savedSets: List<WorkoutSet>, onExerciseClick: (Int) -> Unit, onAddSet: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val matchingExercises = exercises.filter {
        stringResource(it.name).contains(query.trim(), ignoreCase = true) ||
            stringResource(it.muscles).contains(query.trim(), ignoreCase = true)
    }

    Scaffold(
        topBar = { WorkoutTopBar(stringResource(R.string.app_name)) },
        floatingActionButton = { AddSetFab(onAddSet) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { focusManager.clearFocus() },
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = { Text(stringResource(R.string.search_exercises)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search))
                                }
                            }
                        },
                        modifier = Modifier.testTag("exercise_search")
                    )
                },
                expanded = false,
                onExpandedChange = {},
                windowInsets = WindowInsets(0, 0, 0, 0),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {}
            LazyColumn(
                modifier = Modifier.fillMaxSize().testTag("exercise_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "heading") {
                    Text(stringResource(R.string.exercises_title), style = MaterialTheme.typography.headlineSmall)
                }
                if (matchingExercises.isEmpty()) {
                    item(key = "empty_search") {
                        Text(stringResource(R.string.no_exercises_found), modifier = Modifier.testTag("empty_search"))
                    }
                }
                items(matchingExercises, key = { it.id }, contentType = { "exercise" }) { exercise ->
                    ExerciseCard(
                        exercise,
                        savedSets.firstOrNull { it.exerciseId == exercise.id },
                        onClick = {
                            focusManager.clearFocus()
                            onExerciseClick(exercise.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise, savedSet: WorkoutSet?, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("exercise_${exercise.id}")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(exercise.name), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(exercise.muscles), color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (savedSet != null) {
                HorizontalDivider()
                Text(stringResource(R.string.set_summary, savedSet.weight, savedSet.repetitions))
            }
            Text(stringResource(R.string.view_exercise), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun ExerciseDetailsScreen(exercise: Exercise, savedSet: WorkoutSet?, onBack: () -> Unit, onAddSet: () -> Unit) {
    Scaffold(
        topBar = { WorkoutTopBar(stringResource(exercise.name), onBack) },
        floatingActionButton = { AddSetFab(onAddSet) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).testTag("exercise_details_${exercise.id}"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(stringResource(R.string.muscles_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(exercise.muscles), style = MaterialTheme.typography.bodyLarge)
            }
            item {
                Text(stringResource(R.string.about_exercise), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(exercise.description), style = MaterialTheme.typography.bodyLarge)
            }
            item {
                if (savedSet == null) {
                    OutlinedCard(Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.no_saved_sets), modifier = Modifier.padding(16.dp))
                    }
                } else {
                    SavedSetCard(savedSet)
                }
            }
            item { Text(stringResource(R.string.storage_notice), style = MaterialTheme.typography.bodySmall) }
            item { WorkoutHistoryButton() }
        }
    }
}

@Composable
private fun SavedSetCard(set: WorkoutSet) {
    Card(modifier = Modifier.fillMaxWidth().testTag("saved_set")) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.last_set), style = MaterialTheme.typography.titleMedium)
            Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.weight_value, set.weight))
                VerticalDivider(Modifier.padding(horizontal = 12.dp))
                Text(stringResource(R.string.repetitions_value, set.repetitions))
            }
            Text(
                stringResource(if (set.completed) R.string.completed else R.string.planned),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AddSetFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = { Icon(Icons.Default.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.add_set)) },
        modifier = Modifier.testTag("add_set")
    )
}

@Composable
private fun WorkoutHistoryButton() {
    val notifier = LocalPlaceholderNotifier.current
    OutlinedButton(onClick = { notifier.notifyPlaceholder() }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.workout_history))
    }
}
