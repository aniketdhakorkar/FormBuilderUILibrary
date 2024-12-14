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
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM0MTYxMjUxLCJpYXQiOjE3MzQxNTc2NTEsImp0aSI6ImY2YTNiNGFlZjdjZDQ0YWE5ZWQ0MTcxZDk0ODk4NmRmIiwidXNlcl9pZCI6NjZ9.rH2dNkLf4y7VYog__ZvTwDbDj9X0XWKJF1LQ4CU64po"
        )
    }
}