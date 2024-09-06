package ui.helper

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable

@Composable
fun getOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    disabledTextColor = MaterialTheme.colorScheme.onBackground,
    focusedContainerColor = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    disabledContainerColor = MaterialTheme.colorScheme.inverseOnSurface,
    errorContainerColor = MaterialTheme.colorScheme.background,
    unfocusedTrailingIconColor = MaterialTheme.colorScheme.outline,
    focusedTrailingIconColor = MaterialTheme.colorScheme.outline,
    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = .5f),
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
)