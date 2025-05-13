package validation

import model.DependentValueCustomText
import util.InputWrapper

fun calculateSumForElement(
    elementId: Int,
    operatorMap: Map<List<Int>, List<Int>>,
    operatorValueMap: Map<Int, DependentValueCustomText>,
    localParameterValueMap: Map<Int, InputWrapper>
): Int {
    var sum = 0

    operatorMap.forEach { (key, _) ->
        if (key.contains(elementId)) {
            key.forEach { childId ->
                val expression = operatorValueMap[childId]?.expression
                val valueStr = localParameterValueMap[childId]?.value.orEmpty()

                if (expression == "sum" && valueStr.isNotBlank()) {
                    sum += valueStr.toIntOrNull() ?: 0
                }
            }
        }
    }

    return sum
}
