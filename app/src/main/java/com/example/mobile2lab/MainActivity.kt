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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mobile2lab.ui.theme.Mobile2LabTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent { Mobile2LabTheme { WorkoutScreen() } }
    }
}

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
    var completed by rememberSaveable { mutableStateOf(false) }
    var savedExercise by rememberSaveable { mutableIntStateOf(-1) }
    var savedWeight by rememberSaveable { mutableStateOf("") }
    var savedRepetitions by rememberSaveable { mutableStateOf("") }
    var savedCompleted by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val exercises = listOf(R.string.squat, R.string.bench_press, R.string.deadlift)
    val parsedWeight = weight.replace(',', '.').toDoubleOrNull()
    val validWeight = parsedWeight != null && parsedWeight.isFinite() && parsedWeight > 0
    val validRepetitions = (repetitions.toIntOrNull() ?: 0) > 0
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
                LanguageSwitch()
                Text(
                    stringResource(R.string.screen_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                Text(stringResource(R.string.exercise), style = MaterialTheme.typography.titleMedium)
                exercises.forEachIndexed { index, title ->
                    FilterChip(
                        selected = exercise == index,
                        onClick = { exercise = index },
                        label = { Text(stringResource(title)) }
                    )
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.weight)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    isError = showErrors && !validWeight,
                    supportingText = {
                        if (showErrors && !validWeight) Text(stringResource(R.string.weight_error))
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
                        if (showErrors && !validRepetitions) Text(stringResource(R.string.repetitions_error))
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
                    Card(Modifier.fillMaxWidth()) {
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

@Composable
private fun LanguageSwitch() {
    val isEnglish = LocalConfiguration.current.locales[0].language != "ru"
    val languageLabel = stringResource(R.string.language)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(languageLabel, style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.language_russian))
            Switch(
                checked = isEnglish,
                onCheckedChange = { english ->
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(if (english) "en" else "ru")
                    )
                },
                modifier = Modifier.semantics { contentDescription = languageLabel }
            )
            Text(stringResource(R.string.language_english))
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
