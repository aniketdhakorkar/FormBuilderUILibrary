package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DependentApi(
    @SerialName("dependent")
    val dependent: Int,
    @SerialName("method")
    val method: String,
    @SerialName("parameter")
    val parameter: Map<String, Int>,
    @SerialName("url")
    val url: String
)