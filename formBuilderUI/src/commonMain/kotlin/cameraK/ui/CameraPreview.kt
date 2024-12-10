package cameraK.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cameraK.builder.CameraControllerBuilder
import cameraK.controller.CameraController


/**
 * Cross-platform composable function to display the camera preview.
 *
 * @param modifier Modifier to be applied to the camera preview.
 * @param cameraConfiguration Lambda to configure the [CameraControllerBuilder].
 * @param onCameraControllerReady Callback invoked with the initialized [CameraController].
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    cameraConfiguration: CameraControllerBuilder.() -> Unit,
    onCameraControllerReady: (CameraController) -> Unit,
) {
    expectCameraPreview(modifier, cameraConfiguration, onCameraControllerReady)
}

/**
 * Expects platform-specific implementation of [CameraPreview].
 */
@Composable
expect fun expectCameraPreview(
    modifier: Modifier,
    cameraConfiguration: CameraControllerBuilder.() -> Unit,
    onCameraControllerReady: (CameraController) -> Unit
)