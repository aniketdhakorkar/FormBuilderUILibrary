package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElementData(
    @SerialName("options")
    val options: List<Options>
)