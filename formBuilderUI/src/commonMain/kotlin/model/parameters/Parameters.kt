package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Parameters(
    @SerialName("childrens")
    val children: List<Children>,
    @SerialName("element_data")
    val elementData: ElementDataXX,
    @SerialName("element_id")
    val elementId: Int,
    @SerialName("element_type")
    val elementType: String
)