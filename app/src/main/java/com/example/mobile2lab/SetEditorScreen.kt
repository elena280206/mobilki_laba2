package com.example.mobile2lab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SetEditorScreen(initialExerciseId: Int, onBack: () -> Unit, onSave: (WorkoutSet) -> Unit) {
    var exerciseId by rememberSaveable { mutableIntStateOf(initialExerciseId) }
    var weight by rememberSaveable { mutableStateOf("") }
    var repetitions by rememberSaveable { mutableStateOf("") }
    var completed by rememberSaveable { mutableStateOf(true) }
    var weightTouched by rememberSaveable { mutableStateOf(false) }
    var repetitionsTouched by rememberSaveable { mutableStateOf(false) }
    var weightRejected by rememberSaveable { mutableStateOf(false) }
    var repetitionsRejected by rememberSaveable { mutableStateOf(false) }
    val validWeight = SetInputValidation.isValidWeight(weight)
    val validRepetitions = SetInputValidation.isValidRepetitions(repetitions)
    val weightError = weightRejected || (weightTouched && !validWeight)
    val repetitionsError = repetitionsRejected || (repetitionsTouched && !validRepetitions)
    val canSave = validWeight && validRepetitions && !weightError && !repetitionsError
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize().imePadding()) {
        WorkoutTopBar(stringResource(R.string.add_set), onBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.screen_description), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(R.string.exercise), style = MaterialTheme.typography.titleMedium)
            ExercisePicker(exercises.map { it.name }, exercises.indexOfFirst { it.id == exerciseId }) {
                exerciseId = exercises[it].id
            }
            OutlinedTextField(
                value = weight,
                onValueChange = { input ->
                    weightTouched = true
                    weightRejected = !SetInputValidation.acceptsWeightInput(input)
                    if (!weightRejected) weight = input
                },
                label = { Text(stringResource(R.string.weight)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                singleLine = true,
                isError = weightError,
                supportingText = { Text(stringResource(if (weightError) R.string.weight_error else R.string.weight_hint)) },
                modifier = Modifier.fillMaxWidth().testTag("weight_input")
            )
            OutlinedTextField(
                value = repetitions,
                onValueChange = { input ->
                    repetitionsTouched = true
                    repetitionsRejected = !SetInputValidation.acceptsRepetitionsInput(input)
                    if (!repetitionsRejected) repetitions = input
                },
                label = { Text(stringResource(R.string.repetitions)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true,
                isError = repetitionsError,
                supportingText = { Text(stringResource(if (repetitionsError) R.string.repetitions_error else R.string.repetitions_hint)) },
                modifier = Modifier.fillMaxWidth().testTag("repetitions_input")
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.set_completed), modifier = Modifier.weight(1f))
                Switch(checked = completed, onCheckedChange = { completed = it })
            }
            Button(
                onClick = {
                    if (canSave) {
                        focusManager.clearFocus()
                        onSave(WorkoutSet(exerciseId, weight, repetitions, completed))
                    }
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().testTag("save_set")
            ) { Text(stringResource(R.string.save_set)) }
            Text(stringResource(R.string.storage_notice), style = MaterialTheme.typography.bodySmall)
        }
    }
}
