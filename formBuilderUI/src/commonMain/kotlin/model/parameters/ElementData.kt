package model.parameters


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ElementData(
    @SerialName("options")
    val options: List<Options>,
    @SerialName("data_url")
    val dataUrl: String? = null,
    @SerialName("method")
    val method: String? = null,
    @SerialName("dependentApi")
    val dependentApi: List<DependentApi>? = null
)