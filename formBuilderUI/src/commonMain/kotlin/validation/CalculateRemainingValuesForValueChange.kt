package validation

import util.DependentValueCustomText
import util.InputWrapper
import util.Quadruple

fun calculateRemainingValuesForValueChange(
    elementId: Int,
    newValue: String,
    dependentOperatorMap: Map<List<Int>, List<Int>>,
    dependentValueMap: Map<Int, DependentValueCustomText>,
    localParameterValueMap: Map<Int, InputWrapper>
): Quadruple {
    var remainingValue = 0
    var parentValue = 0
    var childValue = 0
    var expression = ""
    var dependentValue = ""

    dependentOperatorMap.forEach { (key, value) ->
        if (key.contains(elementId)) {
            value.forEach {
                parentValue += (localParameterValueMap[it]?.value
                    ?: "0").toIntOrNull() ?: 0
            }
            key.forEach {
                childValue += if (it == elementId) (newValue.toIntOrNull()
                    ?: 0) else (localParameterValueMap[it]?.value
                    ?: "0").toIntOrNull() ?: 0
                if (it == elementId) {
                    expression = dependentValueMap[it]?.expression ?: ""
                    dependentValue = dependentValueMap[it]?.value ?: ""
                }
            }
            remainingValue = parentValue - childValue
        }
    }
    return Quadruple(
        remainingValue = remainingValue,
        parentValue = parentValue,
        childValue = childValue,
        expression = expression,
        dependentValue = dependentValue
    )
}