package validation

import util.InputWrapper

fun calculateRemainingValuesForFocusChange(
    elementId: Int,
    dependentOperatorMap: Map<List<Int>, List<Int>>,
    localParameterValueMap: Map<Int, InputWrapper>
): Int {
    var parentValue = 0
    var childValue = 0

    dependentOperatorMap.forEach { (key, value) ->
        if (key.contains(elementId)) {
            value.forEach {
                parentValue += (localParameterValueMap[it]?.value
                    ?: "0").toIntOrNull() ?: 0
            }
            key.forEach {
                childValue += (localParameterValueMap[it]?.value
                    ?: "0").toIntOrNull() ?: 0
            }
        }
    }
    return parentValue - childValue
}
