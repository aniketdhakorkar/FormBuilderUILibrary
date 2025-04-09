package ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import com.kashif.cameraK.permissions.Permissions

@Composable
fun PermissionsHandler(permissions: Permissions, cameraPermissionState: MutableState<Boolean>) {
    if (!cameraPermissionState.value) {
        permissions.RequestCameraPermission(
            onGranted = { cameraPermissionState.value = true },
            onDenied = { println("Camera Permission Denied") }
        )
    }
}