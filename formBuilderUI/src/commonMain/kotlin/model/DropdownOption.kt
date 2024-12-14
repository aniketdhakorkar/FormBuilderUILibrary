package model

import kotlinx.serialization.Serializable

@Serializable
data class DropdownOption(
    val optionId: Int,
    val optionName: String,
    val pValue: Int,
    val isChecked: Boolean = false
)
