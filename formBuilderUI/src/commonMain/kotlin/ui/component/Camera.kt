package ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cameraK.controller.CameraController
import cameraK.enums.CameraLens
import cameraK.enums.Directory
import cameraK.enums.FlashMode
import cameraK.enums.ImageFormat
import cameraK.enums.Rotation
import cameraK.permissions.Permissions
import cameraK.permissions.providePermissions
import cameraK.result.ImageCaptureResult
import cameraK.ui.CameraPreview
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.parameters.Style
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import ui.helper.CardContainer

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CreateCamera(
    question: String,
    description: String,
    style: Style?,
    isMandatory: String,
    imageList: List<String>,
    onCameraButtonClicked: (String) -> Unit,
    onPhotoDeleteButtonClicked: (Int) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val permissions: Permissions = providePermissions()
    val cameraPermissionState = remember {
        mutableStateOf(
            permissions.hasCameraPermission()
        )
    }
    val cameraController = remember {
        mutableStateOf<CameraController?>(
            null
        )
    }

    if (!cameraPermissionState.value) {
        permissions.RequestCameraPermission(   // Request Camera Permission
            onGranted = {
                cameraPermissionState.value = true
            }, onDenied = {
                println("Camera Permission Denied")
            })
    }
    val isOpenCamera = remember {
        mutableStateOf(false)
    }
    val isViewCamera = remember {
        mutableStateOf(false)
    }

    CardContainer(cardBackgroundColor = MaterialTheme.colorScheme.secondaryContainer) {
        GenerateText(
            question = question,
            style = style,
            isMandatory = isMandatory,
            parameterDescription = description
        )
        LazyRow(verticalAlignment = Alignment.CenterVertically) {
            itemsIndexed(imageList) { index, image ->
                if (image.isNotEmpty()) {
                    val bitmap =
                        Json.decodeFromString<ByteArray>(string = image).decodeToImageBitmap()

                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(280.dp)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                isViewCamera.value = true
                            }.background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            contentScale = ContentScale.FillBounds,
                            bitmap = bitmap,
                            contentDescription = null,
                        )

                        IconButton(
                            modifier = Modifier.align(Alignment.TopEnd),
                            onClick = { onPhotoDeleteButtonClicked(index) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    if (isViewCamera.value) {

                        Dialog(
                            onDismissRequest = { isViewCamera.value = false },
                            properties = DialogProperties(
                                dismissOnClickOutside = true,
                                dismissOnBackPress = true,
                                usePlatformDefaultWidth = false
                            )
                        ) {
                            BoxWithConstraints(contentAlignment = Alignment.Center) {
                                val state =
                                    rememberTransformableState { zoomChange, panChange, rotationChange ->
                                        scale = (scale * zoomChange).coerceIn(1f, 5f)

                                        val extraWidth = (scale - 1) * constraints.maxWidth
                                        val extraHeight = (scale - 1) * constraints.maxHeight

                                        val maxX = extraWidth / 2
                                        val maxY = extraHeight / 2

                                        offset = Offset(
                                            x = (offset.x + scale * panChange.x).coerceIn(
                                                -maxX,
                                                maxX
                                            ),
                                            y = (offset.y + scale * panChange.y).coerceIn(
                                                -maxY,
                                                maxY
                                            ),
                                        )
                                    }

                                Image(modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        translationX = offset.x
                                        translationY = offset.y
                                    }
                                    .transformable(state),
                                    bitmap = bitmap,
                                    contentDescription = null)

                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        isOpenCamera.value = true
                    },
                    modifier = Modifier.padding(top = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.outline
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            modifier = Modifier.size(80.dp),
                            imageVector = Icons.Filled.CameraAlt,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.outlineVariant)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                text = "Add Photo",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }

    if (isOpenCamera.value) {
        Dialog(
            onDismissRequest = { isOpenCamera.value = false },
            properties = DialogProperties(
                dismissOnClickOutside = true,
                dismissOnBackPress = true,
                usePlatformDefaultWidth = false
            )
        ) {
            CameraPreview(modifier = Modifier.fillMaxSize(), cameraConfiguration = {
                setCameraLens(CameraLens.BACK)
                setFlashMode(FlashMode.OFF)
                setImageFormat(ImageFormat.JPEG)
                setDirectory(Directory.PICTURES)
            }, onCameraControllerReady = {
                cameraController.value = it
                println("Camera Controller Ready ${cameraController.value}")
            })
            cameraController.value?.let { controller ->
                CameraScreen(
                    cameraController = controller, onCloseButtonClicked = {
                        isOpenCamera.value = false
                    },
                    onCameraButtonClicked = onCameraButtonClicked
                )
            }
        }
    }
}

@Composable
fun CameraScreen(
    cameraController: CameraController,
    onCloseButtonClicked: () -> Unit,
    onCameraButtonClicked: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isFlashOn by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        IconButton(onClick = onCloseButtonClicked) {
            Icon(
                modifier = Modifier.size(32.dp),
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                when (val result = cameraController.takePicture()) {
                                    is ImageCaptureResult.Success -> {
                                        val res = Json.encodeToString(result.byteArray)
                                        onCameraButtonClicked(res)
                                        onCloseButtonClicked()
                                    }

                                    is ImageCaptureResult.Error -> {
                                        println("Image Capture Error: ${result.exception.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(128.dp),
                            imageVector = Icons.Filled.Camera,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }

                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            cameraController.toggleFlashMode()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 88.dp)
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = null,
                            tint = if (isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        )
                    }
                }
            }
        }
    }
}