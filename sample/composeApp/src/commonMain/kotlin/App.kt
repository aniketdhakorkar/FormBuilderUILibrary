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
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzMyOTY3MjU4LCJpYXQiOjE3MzI5NjM2NTgsImp0aSI6ImZjN2NmNTBmOWQyMTRmN2I5NWNlOWUzNDEzOTcxNjY0IiwidXNlcl9pZCI6Mn0.fmF8Ti8C1ZLDM1zJRRR9glP6zfNvcgyqrH985pFwFJI"
        )
    }
}