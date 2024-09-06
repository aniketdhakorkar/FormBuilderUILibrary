package validation

fun expressionValidation(
    expression: String,
    remainingValue: Int,
    parentValue: Int,
    childValue: Int,
    dependentValue: String
): String {
    when (expression) {
        "less than equal" -> {
            if (remainingValue < 0) {
                return "Please enter a value of $dependentValue or less"
            }
        }

        "greater than equal" -> {
            if (parentValue < childValue) {
                return "Please enter a value of $dependentValue or greater"
            }
        }

        "less" -> {
            if (remainingValue < 0) {
                return "Please enter a value less than $dependentValue"
            }
        }

        "greater" -> {
            if (parentValue <= childValue) {
                return "Please enter a value greater than $dependentValue"
            }
        }

        "equal" -> {
            if (remainingValue != 0) {
                return "Please enter a value equal to $dependentValue"
            }
        }
    }
    return ""
}