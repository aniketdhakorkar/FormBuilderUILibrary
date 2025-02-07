package ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import model.parameters.Style
import ui.helper.CardContainer
import ui.helper.SupportingText
import ui.helper.bringIntoView
import ui.helper.getOutlinedTextFieldColors
import util.InputWrapper

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateDatePicker(
    question: String,
    description: String,
    style: Style?,
    dateValue: InputWrapper,
    onDateValueChanged: (String) -> Unit,
    isMandatory: String,
    isVisible: Boolean,
    isEnabled: Boolean,
    focusManager: FocusManager,
    cardBackgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {
    if (!isVisible) return

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    val state = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= Clock.System.now().toEpochMilliseconds()
            }
        })
    val showDatePicker = remember { mutableStateOf(false) }

    CardContainer(cardBackgroundColor = cardBackgroundColor) {
        GenerateText(
            question = question,
            style = style,
            isMandatory = isMandatory,
            parameterDescription = description
        )

        Box {
            OutlinedTextField(
                value = dateValue.value,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (isEnabled && it.isFocused) {
                            showDatePicker.value = true
                            bringIntoView(coroutineScope, bringIntoViewRequester)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = getOutlinedTextFieldColors(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = "calendar",
                    )
                },
                enabled = isEnabled,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Down
                    )
                }),
                isError = dateValue.errorMessage.isNotEmpty(),
                supportingText = {
                    SupportingText(
                        errorMessage = dateValue.errorMessage
                    )
                }
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .clickable {
                        if (isEnabled) {
                            focusManager.clearFocus()
                            focusRequester.requestFocus()
                        }
                    }
            )
        }

        if (showDatePicker.value) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker.value = false },
                confirmButton = {
                    Button(
                        onClick = {
                            onDateValueChanged(convertMillisToDate(state.selectedDateMillis ?: 0))
                            focusManager.clearFocus()
                            focusRequester.requestFocus()
                            showDatePicker.value = false
                        },
                        enabled = state.selectedDateMillis != null
                    ) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        focusManager.clearFocus()
                        focusRequester.requestFocus()
                        showDatePicker.value = false
                    }) {
                        Text(text = "Cancel")
                    }
                }) {

                DatePicker(
                    title = {},
                    state = state,
                    showModeToggle = false,
                )
            }
        }
    }

    // Auto-focus on error
    SideEffect {
        if (dateValue.isFocus) {
            dateValue.isFocus = false
            focusRequester.requestFocus()
            bringIntoView(coroutineScope, bringIntoViewRequester)
        }
    }
}

private fun convertMillisToDate(millis: Long): String {
    val instant = Instant.fromEpochMilliseconds(millis)
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${localDate.dayOfMonth}/${localDate.monthNumber}/${localDate.year}"
}
