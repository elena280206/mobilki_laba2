package com.example.mobile2lab

/** Editable prefixes allow deleting text and entering fractional weights such as 0,5. */
object SetInputValidation {
    private val weightInput = Regex("[0-9]*([.,][0-9]*)?")
    private val completeWeight = Regex("([0-9]+([.,][0-9]+)?|[.,][0-9]+)")
    private val repetitionsInput = Regex("[0-9]*")

    fun acceptsWeightInput(value: String): Boolean {
        if (!weightInput.matches(value)) return false
        if (value.isEmpty() || value == "." || value == ",") return true
        return value.replace(',', '.').toDoubleOrNull()?.isFinite() == true
    }

    fun isValidWeight(value: String): Boolean =
        completeWeight.matches(value) &&
            value.replace(',', '.').toDoubleOrNull()?.let { it.isFinite() && it > 0 } == true

    fun acceptsRepetitionsInput(value: String): Boolean =
        repetitionsInput.matches(value) && (value.isEmpty() || value.toIntOrNull() != null)

    fun isValidRepetitions(value: String): Boolean =
        repetitionsInput.matches(value) && (value.toIntOrNull() ?: 0) > 0
}
