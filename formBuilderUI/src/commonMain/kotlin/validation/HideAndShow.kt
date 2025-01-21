package validation

import model.parameters.ChildrenX
import util.InputWrapper

fun hideAndShowValidation(
    elementId: Int,
    parameterMap: Map<Int, ChildrenX>,
    parameterValueMap: Map<Int, InputWrapper>,
    selectedOptionIds: List<Int>
): Map<Int, Boolean> {
    val visibilityMap = mutableMapOf<Int, Boolean>()

    // Helper function to check if a condition is satisfied
    fun isConditionSatisfied(condition: String, currentElementId: Int): Boolean {
        val conditionResult = if ("=" in condition) {
            val (left, right) = condition.split("=").map { it.trim().toIntOrNull() }
            if (left != null && right != null) {
                val elementValue = parameterValueMap[left]?.value.orEmpty()
                val elementValues = elementValue.split(",").map { it.trim() }
                val matchingOptionId = parameterMap[left]?.elementData?.options
                    ?.firstOrNull { it.pValue.toString() in elementValues }?.optionId ?: 0
                selectedOptionIds.contains(matchingOptionId) && matchingOptionId == right
            } else {
                false
            }
        } else {
            condition.toIntOrNull()?.let { selectedOptionIds.contains(it) } ?: false
        }

        if (conditionResult) {
            val parentPValue = parameterMap[elementId]?.elementData?.options
                ?.firstOrNull { selectedOptionIds.contains(it.optionId) }?.pValue ?: 0

            val currentPValue = parameterMap[currentElementId]?.elementData?.options
                ?.firstOrNull { selectedOptionIds.contains(it.optionId) }?.pValue ?: 0

            return parentPValue == currentPValue
        }

        return false
    }

    // Recursive function to process dependencies
    fun processDependencies(currentId: Int, isVisible: Boolean) {
        parameterMap[currentId]?.elementOptionDependent?.forEach { (condition, dependentIds) ->
            val isConditionTrue = if ("|" in condition) {
                condition.split("|").any { isConditionSatisfied(it.trim(), currentId) }
            } else {
                isConditionSatisfied(condition, currentId)
            }

            dependentIds.split(",").mapNotNull { it.toIntOrNull() }.forEach { dependentId ->
                if (visibilityMap[dependentId] != true) {
                    visibilityMap[dependentId] = isConditionTrue
                }

                if (!isConditionTrue) {
                    processDependencies(dependentId, false)
                }
            }
        }
    }

    // Start processing from the root element
    processDependencies(elementId, true)

    return visibilityMap
}


