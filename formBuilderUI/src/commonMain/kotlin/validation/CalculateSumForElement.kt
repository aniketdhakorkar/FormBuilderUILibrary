package validation

import model.DependentValueCustomText
import util.InputWrapper

fun calculateSumForElement(
    elementId: Int,
    operatorMap: Map<List<Int>, List<Int>>,
    operatorValueMap: Map<Int, DependentValueCustomText>,
    localParameterValueMap: Map<Int, InputWrapper>
): Map<List<Int>, Int> {
    val sumMap = mutableMapOf<List<Int>, Int>()

    operatorMap.forEach { (key, value) ->
        if (key.contains(elementId)) {
            var sum = 0
            key.forEach { childId ->
                val expression = operatorValueMap[childId]?.expression
                val valueStr = localParameterValueMap[childId]?.value.orEmpty()
                if (expression == "sum" && valueStr.isNotBlank()) {
                    sum += valueStr.toIntOrNull() ?: 0
                }
            }
            sumMap[value] = sum
        }
    }

    return sumMap
}
