import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.lifecycle.viewmodel.compose.viewModel
import ui.component.CreateDropdown
import ui.component.CreateLabel
import ui.component.CreateTextField
import ui.component.SubmitButton
import ui.theme.AppTheme
import util.DependentValueCustomText
import util.InputWrapper
import model.parameters.ChildrenX
import model.parameters.toDropdown

@Composable
fun FormScreen(
    parameterValueMap: MutableMap<Int, InputWrapper>,
    parameterMap: MutableMap<Int, ChildrenX>,
    visibilityMap: MutableMap<Int, Boolean>,
    onClick: (MutableMap<Int, InputWrapper>) -> Unit
) {

    val viewModel: FormScreenViewModel = viewModel()
    val localParameterValueMap by viewModel.localParameterValueMap.collectAsState()
    val localParameterMap by viewModel.localParameterMap.collectAsState()
    val localVisibilityMap by viewModel.localVisibilityMap.collectAsState()
    val dependentValueMap by viewModel.dependentValueMap.collectAsState()
    val focusManager = LocalFocusManager.current
    val showProgressIndicator by viewModel.showProgressIndicator.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(parameterMap) {
        if (parameterMap.isNotEmpty())
            viewModel.initData(
                parameterValueMap = parameterValueMap,
                parameterMap = parameterMap,
                visibilityMap = visibilityMap
            )
    }

    LaunchedEffect(true) {
        viewModel.uiEvent.collect { event ->
            snackbarHostState.showSnackbar(message = event)
        }
    }

    AppTheme {
        Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(localParameterMap.toList()) { parameter ->

                        val parameterValue =
                            localParameterValueMap[parameter.first] ?: InputWrapper("")
                        val isVisible = localVisibilityMap[parameter.first] ?: true
                        val question = parameter.second.elementLabel.en ?: ""
                        val description = parameter.second.elementTooltip.en ?: ""
                        val isMandatory = parameter.second.isRequired
                        val style = parameter.second.style

                        when (parameter.second.elementType) {
                            "ElementLabel" -> CreateLabel(
                                question = question,
                                style = style,
                                isVisible = isVisible
                            )

                            "ElementText" -> {

                                CreateTextField(
                                    question = question,
                                    description = description,
                                    textFieldValue = parameterValue,
                                    onValueChange = { value ->
                                        viewModel.onEvent(
                                            FormScreenEvent.OnTextFieldValueChanged(
                                                elementId = parameter.second.elementId,
                                                value = value
                                            )
                                        )
                                    },
                                    isMandatory = parameter.second.isRequired,
                                    style = parameter.second.style,
                                    inputType = parameter.second.inputType,
                                    isVisible = isVisible,
                                    isEnable = true,
                                    focusManager = focusManager,
                                    onFocusChange = {
                                        if (parameter.second.inputType == "number") {
                                            viewModel.onEvent(
                                                FormScreenEvent.OnTextFieldValueFocusChanged(
                                                    elementId = parameter.second.elementId,
                                                    isFocused = it
                                                )
                                            )
                                        }
                                    },
                                    dependentValueCustomText = dependentValueMap[parameter.second.elementId]
                                        ?: DependentValueCustomText()
                                )
                            }

                            "ElementDropDown" -> {

                                CreateDropdown(
                                    question = question,
                                    description = description,
                                    isMandatory = isMandatory,
                                    style = style,
                                    optionList = parameter.second.elementData.options.map { it.toDropdown() },
                                    dropdownValue = parameterValue,
                                    onValueChanged = { option ->
                                        viewModel.onEvent(
                                            FormScreenEvent.OnDropdownValueChanged(
                                                elementId = parameter.second.elementId,
                                                option = option
                                            )
                                        )
                                    },
                                    isVisible = isVisible,
                                    isEnabled = true,
                                    focusManager = focusManager
                                )
                            }
                        }
                    }
                }

                SubmitButton(showProgressIndicator = showProgressIndicator, onClick = {
                    viewModel.onEvent(FormScreenEvent.OnSubmitButtonClicked)
                })
            }
        }
    }
}