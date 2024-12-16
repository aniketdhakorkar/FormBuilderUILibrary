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
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM0MzQ2Mjc1LCJpYXQiOjE3MzQzNDI2NzUsImp0aSI6Ijg2NjNjZDAzYThhMTQ1MGZhNmVhM2JkNTI4ZTgzNmE4IiwidXNlcl9pZCI6NjZ9.gqPhdJCloAKP9DdV1mxGkN5lIA6mSTNN_uRjeN0lx_k"
        )
    }
}