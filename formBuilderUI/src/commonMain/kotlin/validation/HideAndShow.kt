package validation

fun hideAndShowValidation(
    elementOptionDependent: Map<String, String>?,
    optionId: Int
): Map<Int, Boolean> {
    return elementOptionDependent?.flatMap { (key, value) ->
        value.split(",").map { id -> id.toInt() to (optionId == key.toInt()) }
    }?.toMap() ?: emptyMap()
}