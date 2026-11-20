package com.example.mobile2lab

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetInputValidationTest {
    @Test
    fun acceptsPositiveWeightsWithEitherDecimalSeparator() {
        listOf("12", "12.5", "12,5", "0.5", "0,5", ".5", ",5", "001.25").forEach {
            assertTrue(it, SetInputValidation.acceptsWeightInput(it))
            assertTrue(it, SetInputValidation.isValidWeight(it))
        }
    }

    @Test
    fun allowsIncompleteWeightWhileEditingButNotSaving() {
        listOf("", "0", "0.0", ".", ",", "12.", "12,").forEach {
            assertTrue(it, SetInputValidation.acceptsWeightInput(it))
            assertFalse(it, SetInputValidation.isValidWeight(it))
        }
    }

    @Test
    fun blocksMalformedTypedOrPastedWeights() {
        listOf("abc", "12kg", "-1", "+1", "1e3", "1.2.3", "1,2.3", " 12", "12 ", "1\n2", "NaN", "Infinity").forEach {
            assertFalse(it, SetInputValidation.acceptsWeightInput(it))
            assertFalse(it, SetInputValidation.isValidWeight(it))
        }
    }

    @Test
    fun rejectsOverflowAndUnderflowCannotBeSaved() {
        assertFalse(SetInputValidation.acceptsWeightInput("9".repeat(400)))
        assertFalse(SetInputValidation.isValidWeight("9".repeat(400)))
        assertFalse(SetInputValidation.isValidWeight("0." + "0".repeat(400) + "1"))
        assertFalse(SetInputValidation.acceptsRepetitionsInput("2147483648"))
        assertFalse(SetInputValidation.isValidRepetitions("2147483648"))
    }

    @Test
    fun repetitionsMustBePositiveWholeNumbers() {
        listOf("1", "12", "001", "2147483647").forEach {
            assertTrue(it, SetInputValidation.acceptsRepetitionsInput(it))
            assertTrue(it, SetInputValidation.isValidRepetitions(it))
        }
        listOf("", "0", "00").forEach {
            assertTrue(it, SetInputValidation.acceptsRepetitionsInput(it))
            assertFalse(it, SetInputValidation.isValidRepetitions(it))
        }
        listOf("1.5", "1,5", "-2", "+2", "1a", "1 2", "1\n2").forEach {
            assertFalse(it, SetInputValidation.acceptsRepetitionsInput(it))
            assertFalse(it, SetInputValidation.isValidRepetitions(it))
        }
    }
}
