package model

data class DropdownOption(
    val optionId: Int,
    val optionName: String,
    val pValue: Int,
    val isChecked: Boolean = false
)
