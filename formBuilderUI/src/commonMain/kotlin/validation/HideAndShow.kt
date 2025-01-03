package validation

fun hideAndShowValidation(
    elementOptionDependent: Map<String, String>?,
    selectedOptionIds: List<Int>
): Map<Int, Boolean> {
    return elementOptionDependent?.flatMap { (key, value) ->
        value.split(",").map { id ->
            id.toInt() to selectedOptionIds.contains(key.toInt())
        }
    }?.groupBy({ it.first }, { it.second })
        ?.mapValues { (_, values) -> values.any { it } }
        ?: emptyMap()
}
