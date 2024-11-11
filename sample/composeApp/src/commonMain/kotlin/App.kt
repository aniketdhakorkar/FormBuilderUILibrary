import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import util.InputWrapper
import model.parameters.ChildrenX

@Composable
@Preview
fun App(
    parameterValueMap: Map<Int, InputWrapper>,
    parameterMap: Map<Int, ChildrenX>,
    visibilityMap: Map<Int, Boolean>,
    enabledStatusMap: Map<Int, Boolean>,
) {
    MaterialTheme {
        FormScreen(
            parameterValueMap = parameterValueMap,
            parameterMap = parameterMap,
            visibilityMap = visibilityMap,
            enabledStatusMap = enabledStatusMap,
            onClick = {},
            isSubmitButtonVisible = true,
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzMxMzI2OTk2LCJpYXQiOjE3MzEzMjMzOTYsImp0aSI6IjYxYmZkNzQ2MGIyNTQ1NzU4ZjIwZmNlZTY5MDhjMDhmIiwidXNlcl9pZCI6Mn0.w_ftS70wRKECwdKW3RrU91mNekpPjFI33EFOICOXFoc"
        )
    }
}