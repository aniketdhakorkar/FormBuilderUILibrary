package ui.helper

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import model.DropdownOption

@Composable
fun DropdownMenuComponent(
    dropdownValue: String,
    expanded: Boolean,
    optionList: List<DropdownOption>,
    textFieldSize: Size,
    onClick: (DropdownOption) -> Unit,
    onDismissRequest: () -> Unit

) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.width(with(LocalDensity.current) { textFieldSize.width.toDp() }),
        properties = PopupProperties(focusable = false)
    ) {
        optionList.forEach { option ->
            val isSelected = dropdownValue.toIntOrNull() == option.optionId
            DropdownMenuItem(
                onClick = {
                    onClick(option)
                },
                text = {
                    Text(
                        text = option.optionName,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                        fontSize = if (isSelected) 17.sp else 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun DropdownIcon(expanded: Boolean) {
    Icon(
        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
        contentDescription = null
    )
}