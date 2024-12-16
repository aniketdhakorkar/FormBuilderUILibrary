import model.DropdownOption
import util.InputWrapper

sealed class FormScreenEvent {

    data class OnTextFieldValueChanged(val elementId: Int, val value: String) : FormScreenEvent()

    data class OnTextFieldFocusChanged(val elementId: Int, val isFocused: Boolean) :
        FormScreenEvent()

    data class OnDropdownValueChanged(val elementId: Int, val option: DropdownOption) :
        FormScreenEvent()

    data class OnSubmitButtonClicked(val onClick: (Map<Int, InputWrapper>) -> Unit) :
        FormScreenEvent()

    data class OnPhotoTaken(val elementId: Int, val data: String) : FormScreenEvent()

    data class OnPhotoDeleteButtonClicked(val elementId: Int, val index: Int) : FormScreenEvent()

    data class OnSearchValueChanged(val elementId: Int, val searchText: String) : FormScreenEvent()
}