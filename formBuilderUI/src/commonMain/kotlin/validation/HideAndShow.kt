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

    // Check if a condition holds true
    fun isConditionSatisfied(condition: String): Boolean {
        return if ("=" in condition) {
            val (left, right) = condition.split("=").map { it.trim().toIntOrNull() }
            if (left != null && right != null) {
                val elementValue = parameterValueMap[left]?.value.orEmpty()
                val matchingOptionId = parameterMap[left]?.elementData?.options
                    ?.firstOrNull { it.pValue.toString() == elementValue }?.optionId ?: 0
                selectedOptionIds.contains(matchingOptionId) && matchingOptionId == right
            } else {
                false
            }
        } else {
            condition.toIntOrNull()?.let { selectedOptionIds.contains(it) } ?: false
        }
    }

    // Process dependencies recursively
    fun processDependencies(currentId: Int, isVisible: Boolean) {
        parameterMap[currentId]?.elementOptionDependent?.forEach { (condition, dependentIds) ->
            val isConditionTrue = if ("|" in condition) {
                condition.split("|").any { isConditionSatisfied(it.trim()) }
            } else {
                isConditionSatisfied(condition)
            }

            dependentIds.split(",").mapNotNull { it.toIntOrNull() }.forEach { dependentId ->
                visibilityMap[dependentId] = isConditionTrue && isVisible
                if (!isConditionTrue || !isVisible) {
                    processDependencies(dependentId, false)
                }
            }
        }
    }

    // Start processing from the root element
    processDependencies(elementId, true)

    return visibilityMap
}







