package validation

import model.DependentValueCustomText
import util.InputWrapper
import util.Quadruple

fun calculateRemainingValuesForValueChange(
    elementId: Int,
    newValue: String,
    dependentOperatorMap: Map<List<Int>, List<Int>>,
    dependentValueMap: Map<Int, DependentValueCustomText>,
    localParameterValueMap: Map<Int, InputWrapper>,
    isSkipEqualConditions: Boolean = false
): Quadruple {
    var remainingValue = 0
    var parentValue = 0
    var childValue = 0
    var expression = ""
    var dependentValue = ""

    for ((childIds, parentIds) in dependentOperatorMap) {
        if (!childIds.contains(elementId)) continue

        parentValue = parentIds.sumOf { id ->
            localParameterValueMap[id]?.value?.toIntOrNull() ?: 0
        }

        childValue = 0
        for (childId in childIds) {
            val valueStr = if (childId == elementId) {
                newValue
            } else {
                localParameterValueMap[childId]?.value.orEmpty()
            }

            val valueInt = valueStr.toIntOrNull() ?: 0
            childValue += valueInt

            if (childId == elementId) {
                expression = dependentValueMap[childId]?.expression.orEmpty()
                dependentValue = dependentValueMap[childId]?.value.orEmpty()

                if (
                    expression == "equal" &&
                    valueStr.isNotBlank() &&
                    dependentValue.length > valueStr.length
                ) {
                    val firstCharOfDependentValue = dependentValue.first()
                    val firstCharOfNewValue = valueStr.first()

                    if (firstCharOfNewValue <= firstCharOfDependentValue) {
                        return Quadruple(
                            remainingValue = if (isSkipEqualConditions) parentValue - childValue else 0,
                            parentValue = parentValue,
                            childValue = childValue,
                            expression = expression,
                            dependentValue = dependentValue
                        )
                    }
                }
            } else {
                val isBlank = localParameterValueMap[childId]?.value?.isBlank() == true

                if (
                    expression == "equal" &&
                    isBlank &&
                    newValue.isNotBlank() &&
                    newValue.toInt() < parentValue
                ) {
                    return Quadruple(
                        remainingValue = if (childValue >= parentValue) parentValue - childValue else 0,
                        parentValue = parentValue,
                        childValue = childValue,
                        expression = expression,
                        dependentValue = dependentValue
                    )
                }
            }
        }

        remainingValue = parentValue - childValue
    }

    return Quadruple(
        remainingValue = remainingValue,
        parentValue = parentValue,
        childValue = childValue,
        expression = expression,
        dependentValue = dependentValue
    )
}