package com.example.mobile2lab

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutFlowTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchFiltersCardsAndBackPreservesQuery() {
        val query = compose.activity.getString(R.string.bench_press).uppercase()
        compose.onNodeWithTag("exercise_search").performTextInput(query)
        compose.onNodeWithTag("exercise_1").assertDoesNotExist()
        compose.onNodeWithTag("exercise_2").performClick()
        compose.onNodeWithTag("exercise_details_2").assertIsDisplayed()
        compose.onNodeWithTag("navigate_back").performClick()
        compose.onNodeWithTag("exercise_search").assertTextContains(query)
        compose.onNodeWithTag("exercise_search").performTextReplacement("zzzz")
        compose.onNodeWithTag("empty_search").assertIsDisplayed()
        compose.onNodeWithContentDescription(compose.activity.getString(R.string.clear_search)).performClick()
        compose.onNodeWithTag("exercise_1").assertExists()
        compose.onNodeWithTag("exercise_2").assertExists()
    }

    @Test
    fun invalidInputIsRejectedAndImmediatelyShowsError() {
        compose.onNodeWithTag("add_set").performClick()
        val weight = compose.onNodeWithTag("weight_input")
        val repetitions = compose.onNodeWithTag("repetitions_input")
        val save = compose.onNodeWithTag("save_set")
        save.assertIsNotEnabled()
        weight.performTextReplacement("12")
        repetitions.performTextReplacement("10")
        save.assertIsEnabled()

        weight.performTextReplacement("12a")
        weight.assertTextContains("12").assert(hasError())
        save.assertIsNotEnabled()
        weight.performTextReplacement("12,5")
        weight.assert(hasNoError())
        weight.performTextReplacement("12,5.")
        weight.assertTextContains("12,5").assert(hasError())
        save.assertIsNotEnabled()
        weight.performTextReplacement("0")
        weight.assert(hasError())
        weight.performTextReplacement("0,5")
        weight.assert(hasNoError())

        repetitions.performTextReplacement("10.5")
        repetitions.assertTextContains("10").assert(hasError())
        save.assertIsNotEnabled()
        repetitions.performTextReplacement("2147483648")
        repetitions.assertTextContains("10").assert(hasError())
        repetitions.performTextReplacement("11")
        save.assertIsEnabled()
        repetitions.performTextClearance()
        repetitions.assert(hasError())
        save.assertIsNotEnabled()
    }

    @Test
    fun savesToSelectedExerciseAndRestoresDraftAfterRecreation() {
        compose.onNodeWithTag("exercise_2").performClick()
        compose.onNodeWithTag("add_set").performClick()
        compose.onNodeWithTag("weight_input").performTextReplacement("40")
        compose.onNodeWithTag("repetitions_input").performTextReplacement("10")
        compose.onNodeWithTag("save_set").performScrollTo().performClick()
        compose.onNodeWithTag("exercise_details_2").assertIsDisplayed()
        compose.onNodeWithTag("saved_set").assertExists()
        compose.onNodeWithText(compose.activity.getString(R.string.weight_value, "40")).assertExists()

        compose.onNodeWithTag("add_set").performClick()
        compose.onNodeWithTag("weight_input").performTextReplacement("45,5")
        compose.onNodeWithTag("repetitions_input").performTextReplacement("8")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("weight_input").assertTextContains("45,5")
        compose.onNodeWithTag("repetitions_input").assertTextContains("8")
        compose.onNodeWithTag("save_set").assertIsEnabled()
        compose.onNodeWithTag("navigate_back").performClick()
        compose.onNodeWithText(compose.activity.getString(R.string.weight_value, "40")).assertExists()
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        compose.onNodeWithTag("exercise_list").assertIsDisplayed()
        compose.onNodeWithTag("exercise_1").performClick()
        compose.onNodeWithTag("exercise_details_1").assertIsDisplayed()
        compose.onNodeWithTag("saved_set").assertDoesNotExist()
    }

    private fun hasError() = SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)
    private fun hasNoError() = SemanticsMatcher.keyNotDefined(SemanticsProperties.Error)
}
