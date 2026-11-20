package com.example.mobile2lab

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.os.LocaleListCompat
import com.example.mobile2lab.ui.theme.Mobile2LabTheme

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
