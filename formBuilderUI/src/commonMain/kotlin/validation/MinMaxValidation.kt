package validation

fun validateInputInRange(
    newValue: String,
    minValue: Double,
    maxValue: Double
): String {
    val new = newValue.toDoubleOrNull()
    return when {
        new == null || minValue > maxValue -> ""
        new !in minValue..maxValue -> "Value should be in range between [$minValue - $maxValue]"
        else -> ""
    }
}