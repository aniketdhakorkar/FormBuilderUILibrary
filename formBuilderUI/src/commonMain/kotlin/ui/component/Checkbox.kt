package ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import model.DropdownOption
import model.parameters.Style
import ui.helper.CardContainer
import util.InputWrapper
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CreateCheckbox(
    question: String,
    description: String,
    style: Style?,
    isMandatory: String,
    optionList: List<DropdownOption>,
    cbValue: InputWrapper,
    onCheckChanged: (DropdownOption) -> Unit,
    isVisible: Boolean,
    isEnabled: Boolean,
    focusManager: FocusManager,
    cardBackgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
) {

    if (!isVisible) return

    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    CardContainer(cardBackgroundColor = cardBackgroundColor) {
        GenerateText(
            question = question,
            style = style,
            isMandatory = isMandatory,
            parameterDescription = description
        )

        optionList.forEachIndexed { index, option ->

            val isChecked = cbValue.value
                .split(",")
                .map { it.trim() }
                .contains(option.pValue.toString())

            if ((optionList.size / 2).toDouble().roundToInt() == index)
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier
                        .focusRequester(focusRequester = focusRequester)
                        .size(width = 1.dp, height = 1.dp)
                        .onFocusEvent {
                            if (it.isFocused)
                                coroutineScope.launch {
                                    bringIntoViewRequester.bringIntoView()
                                }
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(
                            FocusDirection.Down
                        )
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        cursorColor = Color.Transparent,
                        errorCursorColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Transparent
                    ),
                    enabled = isEnabled
                )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = {
                        onCheckChanged(option.copy(isChecked = !isChecked))
                    },
                    enabled = isEnabled,
                    colors = CheckboxDefaults.colors(uncheckedColor = MaterialTheme.colorScheme.outlineVariant),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option.optionName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }

}