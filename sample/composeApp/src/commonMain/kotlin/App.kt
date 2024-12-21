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
            action = "add",
            activity = "10",
            form = "3",
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM0Nzk2MzAwLCJpYXQiOjE3MzQ3OTI3MDAsImp0aSI6IjJiYjYyZmFkNmY3YzQwNDM4ZmE4ZjczNzJlMDY0MTM2IiwidXNlcl9pZCI6NjZ9.FgRtm3THyDzwnhhxMZ8an3W4tXYzQRMAy9LrRPJk7w8"
        )
    }
}