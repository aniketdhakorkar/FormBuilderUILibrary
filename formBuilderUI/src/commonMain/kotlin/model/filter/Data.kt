package model.filter


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DropdownOption

@Serializable
data class Data(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
)

fun Data.toDropdown() = DropdownOption(optionId = 0, optionName = name, pValue = id)