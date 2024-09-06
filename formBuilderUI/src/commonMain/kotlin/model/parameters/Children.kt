package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Children(
    @SerialName("childrens")
    val children: List<ChildrenX>,
    @SerialName("element_data")
    val elementData: ElementDataXX,
    @SerialName("element_id")
    val elementId: Int,
    @SerialName("element_size")
    val elementSize: Int,
    @SerialName("element_type")
    val elementType: String
)