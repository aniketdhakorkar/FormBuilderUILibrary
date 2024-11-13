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
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzMxNDg3ODAyLCJpYXQiOjE3MzE0ODQyMDIsImp0aSI6IjY2ZTYyMDQ0MmJmNTQ1MzI4YjlmNmRlYTJiN2EyYjJmIiwidXNlcl9pZCI6Mn0.U1KtUk-uevwMCGFW6jfFCq1NDk8LWZ_hQGqhAKphUTw"
        )
    }
}