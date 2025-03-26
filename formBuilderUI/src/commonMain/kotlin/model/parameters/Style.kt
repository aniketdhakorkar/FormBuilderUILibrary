package model.parameters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Style(
    @SerialName("text_style")
    val textStyle: String? = null,
    @SerialName("background_color")
    val backgroundColor: String? = null,
    @SerialName("font_color")
    val fontColor: String? = null,
    @SerialName("font_size")
    val fontSize: Int? = null
)
