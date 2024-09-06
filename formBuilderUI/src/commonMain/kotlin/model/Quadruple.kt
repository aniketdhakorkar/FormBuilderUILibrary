package util

data class Quadruple(
    val remainingValue: Int,
    val parentValue: Int,
    val childValue: Int,
    val expression: String,
    val dependentValue: String
)
