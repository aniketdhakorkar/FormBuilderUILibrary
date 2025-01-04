package validation

import model.parameters.ChildrenX

fun hideAndShowValidation(
    elementId: Int,
    parameterMap: Map<Int, ChildrenX>,
    selectedOptionIds: List<Int>,
): Map<Int, Boolean> {
    val result = mutableMapOf<Int, Boolean>()

    fun processDependencies(currentId: Int, isVisible: Boolean) {
        parameterMap[currentId]?.elementOptionDependent?.forEach { (key, dependentIds) ->
            val currentVisibility = key.toInt() in selectedOptionIds
            dependentIds.split(",").mapNotNull { id ->
                val dependentId = id.toIntOrNull()
                dependentId?.let {
                    // Update visibility
                    result[it] = result[it] ?: false || currentVisibility

                    // Apply recursion only if the element is being hidden
                    if (!currentVisibility) {
                        processDependencies(it, currentVisibility)
                    }
                }
            }
        }
    }

    // Start processing dependencies from the given elementId
    processDependencies(elementId, true)

    return result
}





