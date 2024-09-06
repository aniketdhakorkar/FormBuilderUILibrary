package ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ui.helper.CardContainer
import ui.helper.SupportingText
import ui.helper.bringIntoView
import ui.helper.getOutlinedTextFieldColors
import util.DependentValueCustomText
import util.InputWrapper
import model.parameters.Style

@OptIn(ExperimentalFoundationApi::class)
@Composable

fun CreateTextField(
    question: String,
    description: String,
    style: Style?,
    isMandatory: String,
    inputType: String,
    singleLine: Boolean = true,
    textFieldValue: InputWrapper,
    onValueChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    dependentValueCustomText: DependentValueCustomText,
    isVisible: Boolean,
    isEnable: Boolean,
    focusManager: FocusManager,
) {

    if (!isVisible) return

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }


    CardContainer {
        GenerateText(
            question = question,
            style = style,
            isMandatory = isMandatory,
            parameterDescription = description
        )

        OutlinedTextField(
            value = textFieldValue.value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusEvent {
                    if (it.isFocused)
                        bringIntoView(coroutineScope, bringIntoViewRequester)
                }
                .onFocusChanged { focusState ->
                    onFocusChange(focusState.isFocused)
                },
            shape = RoundedCornerShape(16.dp),
            colors = getOutlinedTextFieldColors(),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (inputType == "text") KeyboardType.Text else KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = {
                focusManager.moveFocus(
                    FocusDirection.Down
                )
            }),
            singleLine = singleLine,
            enabled = isEnable,
            isError = textFieldValue.errorMessage.isNotEmpty(),
            supportingText = {
                SupportingText(
                    dependentValueCustomText = dependentValueCustomText,
                    errorMessage = textFieldValue.errorMessage
                )
            }
        )
    }

    SideEffect {
        if (textFieldValue.isFocus) {
            textFieldValue.isFocus = false
            focusRequester.requestFocus()
            bringIntoView(coroutineScope, bringIntoViewRequester)
        }
    }
}
