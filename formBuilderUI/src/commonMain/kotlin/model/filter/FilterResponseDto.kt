package model.filter


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilterResponseDto(
    @SerialName("data")
    val `data`: List<Data>?,
    @SerialName("errors")
    val errors: Errors?,
    @SerialName("status")
    val status: String
)