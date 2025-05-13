package model.parameters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AutoCalculate(
    @SerialName("operation")
    val operation: String,
    @SerialName("operator")
    val operator: String,
    @SerialName("operator_result")
    val operatorResult: String,
)
