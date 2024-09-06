package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Validation(
    @SerialName("exp")
    val exp: String,
    @SerialName("values")
    val values: List<Value>
)