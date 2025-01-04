import model.DropdownOption
import model.ImageModel
import util.InputWrapper

sealed class FormScreenEvent {

    data class OnTextFieldValueChanged(val elementId: Int, val value: String) : FormScreenEvent()

    data class OnTextFieldFocusChanged(val elementId: Int, val isFocused: Boolean) :
        FormScreenEvent()

    data class OnDropdownValueChanged(val elementId: Int, val option: DropdownOption) :
        FormScreenEvent()

    data class OnSubmitButtonClicked(val onClick: (Map<Int, InputWrapper>, Map<Int, Boolean>) -> Unit) :
        FormScreenEvent()

    data class OnPhotoTaken(val elementId: Int, val image: ImageModel) : FormScreenEvent()

    data class OnPhotoDeleteButtonClicked(val elementId: Int, val index: Int) : FormScreenEvent()

    data class OnSearchValueChanged(val elementId: Int, val searchText: String) : FormScreenEvent()

    data class OnImageViewButtonClicked(val elementId: Int, val image: ImageModel) :
        FormScreenEvent()

    data class OnCheckboxValueChanged(val elementId: Int, val option: DropdownOption) :
        FormScreenEvent()
}