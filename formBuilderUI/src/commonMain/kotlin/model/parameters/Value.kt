package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Value(
    @SerialName("dependant")
    val dependant: String,
    @SerialName("dependent_operator")
    val dependentOperator: String,
    @SerialName("dependent_result")
    val dependentResult: String
)