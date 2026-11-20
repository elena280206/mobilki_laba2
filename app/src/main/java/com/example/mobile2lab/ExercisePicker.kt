package com.example.mobile2lab

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExercisePicker(exercises: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
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


