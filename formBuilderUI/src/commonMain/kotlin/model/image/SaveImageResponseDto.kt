package model.image


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.filter.Errors

@Serializable
data class SaveImageResponseDto(
    @SerialName("activity")
    val activity: String,
    @SerialName("form")
    val form: String,
    @SerialName("resource_id")
    val resourceId: Int,
    @SerialName("resource_paths")
    val resourcePaths: String,
    @SerialName("status")
    val status: String,
    @SerialName("errors")
    val errors: Errors? = null
)