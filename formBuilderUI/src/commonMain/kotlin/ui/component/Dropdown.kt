package ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.*
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize
import ui.helper.DropdownIcon
import ui.helper.DropdownMenuComponent
import ui.helper.bringIntoView
import ui.helper.getOutlinedTextFieldColors
import util.DropdownOption
import util.InputWrapper
import model.parameters.Style
import ui.helper.CardContainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateDropdown(
    question: String,
    description: String,
    style: Style?,
    isMandatory: String,
    optionList: List<DropdownOption>,
    dropdownValue: InputWrapper,
    onValueChanged: (DropdownOption) -> Unit,
    isVisible: Boolean,
    isEnabled: Boolean,
    focusManager: FocusManager,
) {
    if (!isVisible) return

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    var selectedText by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    // Set selected text based on dropdown value
    optionList.find { dropdownValue.value.toIntOrNull() == it.optionId }?.let {
        selectedText = it.optionName
    }

    CardContainer {
        GenerateText(
            question = question,
            style = style,
            isMandatory = isMandatory,
            parameterDescription = description
        )

        Box {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        textFieldSize = coordinates.size.toSize()
                    }
                    .focusRequester(focusRequester)
                    .onFocusChanged { if (it.isFocused && isEnabled) expanded = true }
                    .onFocusEvent {
                        if (it.isFocused)
                            bringIntoView(coroutineScope, bringIntoViewRequester)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = getOutlinedTextFieldColors(),
                trailingIcon = {
                    DropdownIcon(expanded = expanded)
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    focusManager.moveFocus(
                        FocusDirection.Down
                    )
                }),
                enabled = isEnabled,
                isError = dropdownValue.errorMessage.isNotEmpty(),
                supportingText = {
                    dropdownValue.errorMessage.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            text = it,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .alpha(0f)
                    .clickable {
                        if (isEnabled) {
                            focusRequester.requestFocus()
                            expanded = true
                        }
                    }
            )

            DropdownMenuComponent(
                dropdownValue = dropdownValue.value,
                expanded = expanded,
                optionList = optionList,
                textFieldSize = textFieldSize,
                onClick = {
                    onValueChanged(it.copy(isChecked = true))
                    expanded = false
                    focusRequester.requestFocus()
                    bringIntoView(coroutineScope, bringIntoViewRequester)
                },
                onDismissRequest = { expanded = false }
            )
        }
    }

    // Auto-focus on error
    SideEffect {
        if (dropdownValue.isFocus) {
            dropdownValue.isFocus = false
            focusRequester.requestFocus()
            bringIntoView(coroutineScope, bringIntoViewRequester)
        }
    }
}