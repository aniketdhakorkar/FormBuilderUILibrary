package model.filter


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DropdownOption
import model.parameters.OptionName
import model.parameters.Options

@Serializable
data class Data(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
)

fun Data.toDropdown() = DropdownOption(optionId = 0, optionName = name, pValue = id)

fun Data.toOption() = Options(
    optionId = id,
    optionName = OptionName(
        en = name,
        asX = name,
        bn = name,
        gu = name,
        hi = name,
        kn = name,
        mr = name,
        or = name,
        pa = name,
        ta = name,
        te = name,
        ur = name
    ),
    pValue = id
)