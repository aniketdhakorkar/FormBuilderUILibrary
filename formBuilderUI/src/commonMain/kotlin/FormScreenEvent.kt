import util.DropdownOption
import util.InputWrapper

sealed class FormScreenEvent {

    data class OnTextFieldValueChanged(val elementId: Int, val value: String) : FormScreenEvent()

    data class OnTextFieldValueFocusChanged(val elementId: Int, val isFocused: Boolean) :
        FormScreenEvent()

    data class OnDropdownValueChanged(val elementId: Int, val option: DropdownOption) :
        FormScreenEvent()

    data class OnSubmitButtonClicked(val onClick: (Map<Int, InputWrapper>) -> Unit) :
        FormScreenEvent()
}