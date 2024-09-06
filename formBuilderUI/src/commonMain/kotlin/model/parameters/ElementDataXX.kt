package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElementDataXX(
    @SerialName("element_subtitle")
    val elementSubtitle: String,
    @SerialName("element_title")
    val elementTitle: String
)