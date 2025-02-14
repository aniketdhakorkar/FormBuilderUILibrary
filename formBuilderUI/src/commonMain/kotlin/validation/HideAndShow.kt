package validation

import kotlinx.serialization.json.Json
import model.DropdownOption
import model.parameters.ChildrenX
import util.InputWrapper

fun hideAndShowValidation(
    elementId: Int,
    parameterMap: Map<Int, ChildrenX>,
    parameterValueMap: Map<Int, InputWrapper>,
    selectedOptionIds: List<Int>
): Map<Int, Boolean> {
    val visibilityMap = mutableMapOf<Int, Boolean>()

    fun isConditionSatisfied(condition: String, currentElementId: Int): Boolean {
        val conditionResult = when {
            "=" in condition -> {
                val (leftStr, rightStr) = condition.split("=").map { it.trim() }
                val leftId = leftStr.toIntOrNull()
                val rightId = rightStr.toIntOrNull()

                if (leftId != null && rightId != null) {
                    val rawValue = parameterValueMap[leftId]?.value.orEmpty()
                    val elementValue = if ("{" in rawValue) {
                        Json.decodeFromString<DropdownOption>(rawValue).pValue.toString()
                    } else {
                        rawValue
                    }

                    val elementValues = elementValue.split(",").map { it.trim() }
                    val matchingOptionId = parameterMap[leftId]?.elementData?.options
                        ?.firstOrNull { it.pValue.toString() in elementValues }?.optionId ?: 0

                    selectedOptionIds.contains(matchingOptionId) && matchingOptionId == rightId
                } else {
                    false
                }
            }

            else -> condition.toIntOrNull()?.let { selectedOptionIds.contains(it) } ?: false
        }

        if (!conditionResult) return false

        val parentPValue = parameterMap[elementId]?.elementData?.options
            ?.firstOrNull { selectedOptionIds.contains(it.optionId) }?.pValue ?: 0

        val currentPValue = parameterMap[currentElementId]?.elementData?.options
            ?.firstOrNull { selectedOptionIds.contains(it.optionId) }?.pValue ?: 0

        return parentPValue == currentPValue
    }

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

    processDependencies(elementId, true)

    return visibilityMap
}


