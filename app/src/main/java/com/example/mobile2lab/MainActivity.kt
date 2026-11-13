package com.example.mobile2lab

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobile2lab.ui.theme.Mobile2LabTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent { Mobile2LabTheme(dynamicColor = false) { WorkoutScreen() } }
    }
}

private val weightFormat = Regex("[0-9]+([.,][0-9]*)?|[.,][0-9]+")
private val repetitionsFormat = Regex("[0-9]+")

fun interface PlaceholderNotifier {
    fun notifyPlaceholder()
}

val LocalPlaceholderNotifier = staticCompositionLocalOf<PlaceholderNotifier> {
    error("PlaceholderNotifier is not provided")
}

@Composable
fun WorkoutScreen() {
    var exercise by rememberSaveable { mutableIntStateOf(0) }
    var weight by rememberSaveable { mutableStateOf("") }
    var repetitions by rememberSaveable { mutableStateOf("") }
    var completed by rememberSaveable { mutableStateOf(true) }
    var savedExercise by rememberSaveable { mutableIntStateOf(-1) }
    var savedWeight by rememberSaveable { mutableStateOf("") }
    var savedRepetitions by rememberSaveable { mutableStateOf("") }
    var savedCompleted by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val exercises = listOf(R.string.squat, R.string.bench_press, R.string.deadlift)
    val parsedWeight = weight.replace(',', '.').toDoubleOrNull()
    val validWeight = weightFormat.matches(weight) &&
        parsedWeight != null && parsedWeight.isFinite() && parsedWeight > 0
    val validRepetitions = repetitionsFormat.matches(repetitions) &&
        (repetitions.toIntOrNull() ?: 0) > 0
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
            Column(
                modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(R.string.screen_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Text(stringResource(R.string.exercise), style = MaterialTheme.typography.titleMedium)
                ExercisePicker(exercises, exercise) { exercise = it }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = showErrors && !validWeight,
                    supportingText = {
                        Text(stringResource(R.string.weight_hint))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = repetitions,
                    onValueChange = { repetitions = it },
                    label = { Text(stringResource(R.string.repetitions)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = showErrors && !validRepetitions,
                    supportingText = {
                        Text(stringResource(R.string.repetitions_hint))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.set_completed), modifier = Modifier.weight(1f))
                    Switch(checked = completed, onCheckedChange = { completed = it })
                }
                Spacer(Modifier.height(4.dp))
                Button(
                    onClick = {
                        showErrors = true
                        if (validWeight && validRepetitions) {
                            savedExercise = exercise
                            savedWeight = weight
                            savedRepetitions = repetitions
                            savedCompleted = completed
                            showErrors = false
                            scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(stringResource(R.string.save_set)) }
                if (savedExercise >= 0) {
                    HorizontalDivider()
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stringResource(R.string.last_set), style = MaterialTheme.typography.titleMedium)
                            Text(stringResource(exercises[savedExercise]), style = MaterialTheme.typography.titleLarge)
                            Row(Modifier.height(IntrinsicSize.Min), verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.weight_value, savedWeight))
                                VerticalDivider(Modifier.padding(horizontal = 12.dp))
                                Text(stringResource(R.string.repetitions_value, savedRepetitions))
                            }
                            Text(
                                stringResource(if (savedCompleted) R.string.completed else R.string.planned),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(stringResource(R.string.storage_notice), style = MaterialTheme.typography.bodySmall)
                WorkoutHistoryButton()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePicker(exercises: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    val labels = exercises.map { stringResource(it) }
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelLarge
    val spacing = 6.dp
    val density = LocalDensity.current
    val labelsWidth = with(density) {
        labels.sumOf { textMeasurer.measure(it, style = labelStyle).size.width }.toDp()
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val chipsWidth = labelsWidth + 24.dp * labels.size + spacing * (labels.size - 1)
        if (chipsWidth <= maxWidth) {
            Row(horizontalArrangement = Arrangement.spacedBy(spacing)) {
                labels.forEachIndexed { index, label ->
                    FilterChip(
                        selected = selected == index,
                        onClick = { onSelect(index) },
                        label = { Text(label, maxLines = 1) }
                    )
                }
            }
        } else {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = labels[selected],
                    onValueChange = {},
                    readOnly = true,
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    labels.forEachIndexed { index, label ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                onSelect(index)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryButton() {
    val notifier = LocalPlaceholderNotifier.current
    OutlinedButton(onClick = { notifier.notifyPlaceholder() }, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.workout_history))
    }
}

@Preview(showBackground = true, locale = "ru", name = "Light Russian")
@Composable
fun WorkoutLightPreview() {
    Mobile2LabTheme(dynamicColor = false) { WorkoutScreen() }
}

@Preview(showBackground = true, locale = "en", name = "Light English")
@Composable
fun WorkoutEnglishPreview() {
    Mobile2LabTheme(dynamicColor = false) { WorkoutScreen() }
}
