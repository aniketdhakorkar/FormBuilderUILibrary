package ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cameraK.controller.CameraController
import cameraK.enums.CameraLens
import cameraK.enums.Directory
import cameraK.enums.FlashMode
import cameraK.enums.ImageFormat
import cameraK.permissions.providePermissions
import cameraK.result.ImageCaptureResult
import cameraK.ui.CameraPreview
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import model.ImageModel
import model.parameters.Style
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import ui.helper.CardContainer

@Composable
fun CreateCamera(
    question: String,
    description: String,
    style: Style?,
    isMandatory: String,
    imageList: List<ImageModel>,
    action: String,
    onPhotoTaken: (ImageModel) -> Unit,
    onPhotoDeleteButtonClicked: (Int) -> Unit,
    onImageViewButtonClicked: (ImageModel) -> Unit
) {
    val permissions = providePermissions()
    val cameraPermissionState = remember { mutableStateOf(permissions.hasCameraPermission()) }
    val cameraController = remember { mutableStateOf<CameraController?>(null) }
    val isOpenCamera = remember { mutableStateOf(false) }
    val isViewCamera = remember { mutableStateOf(false) }

    if (!cameraPermissionState.value) {
        permissions.RequestCameraPermission(
            onGranted = { cameraPermissionState.value = true },
            onDenied = { println("Camera Permission Denied") }
        )
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
                DisplayImageItem(
                    image = image,
                    index = index,
                    action = action,
                    onPhotoDeleteButtonClicked = onPhotoDeleteButtonClicked,
                    onPhotoClicked = { isViewCamera.value = true },
                    onImageViewButtonClicked = onImageViewButtonClicked
                )

                if (isViewCamera.value) {
                    ImagePreviewDialog(
                        imageModel = image,
                        onDismiss = { isViewCamera.value = false }
                    )
                }
            }

            item {
                AddPhotoButton { isOpenCamera.value = true }
            }
        }
    }

    if (isOpenCamera.value) {
        CameraDialog(
            isOpenCamera = isOpenCamera,
            cameraController = cameraController,
            onCameraButtonClicked = onPhotoTaken
        )
    }
}

@Composable
fun DisplayImageItem(
    image: ImageModel,
    index: Int,
    action: String,
    onPhotoDeleteButtonClicked: (Int) -> Unit,
    onPhotoClicked: () -> Unit,
    onImageViewButtonClicked: (ImageModel) -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(280.dp)
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onPhotoClicked() }
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        ImageContent(image = image)

        when (action) {
            "view", "edit" -> {
                Button(
                    onClick = {
                        onImageViewButtonClicked(image)
                        onPhotoClicked()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.RemoveRedEye, contentDescription = "View")
                        Text(text = "View")
                    }
                }
            }
        }

        if (action != "view") {
            IconButton(
                onClick = { onPhotoDeleteButtonClicked(index) },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImageContent(image: ImageModel) {
    if (image.byteImage != null) {
        Image(
            bitmap = image.byteImage.decodeToImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        AsyncImage(
            model = image.preSignedUrl,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (image.isLoading) {
        CircularProgressIndicator()
    }
}

@Composable
fun AddPhotoButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.padding(top = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.outline
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier.size(80.dp),
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant
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

@Composable
fun CameraDialog(
    isOpenCamera: MutableState<Boolean>,
    cameraController: MutableState<CameraController?>,
    onCameraButtonClicked: (ImageModel) -> Unit
) {
    CreateDialogBox(
        onDismissRequest = { isOpenCamera.value = false },
        content = {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                cameraConfiguration = {
                    setCameraLens(CameraLens.BACK)
                    setFlashMode(FlashMode.OFF)
                    setImageFormat(ImageFormat.JPEG)
                    setDirectory(Directory.PICTURES)
                },
                onCameraControllerReady = {
                    cameraController.value = it
                }
            )

            cameraController.value?.let { controller ->
                CameraScreen(
                    cameraController = controller,
                    onCloseButtonClicked = { isOpenCamera.value = false },
                    onCameraButtonClicked = onCameraButtonClicked
                )
            }
        }
    )
}

@Composable
fun ImagePreviewDialog(imageModel: ImageModel, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    CreateDialogBox(
        onDismissRequest = onDismiss,
        content = {
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                val state = rememberTransformableState { zoomChange, panChange, _ ->
                    val newScale = (scale * zoomChange).coerceIn(1f, 5f)
                    val extraWidth = (newScale - 1) * constraints.maxWidth
                    val extraHeight = (newScale - 1) * constraints.maxHeight
                    val maxX = extraWidth / 2
                    val maxY = extraHeight / 2

                    scale = newScale
                    offset = Offset(
                        x = (offset.x + newScale * panChange.x).coerceIn(-maxX, maxX),
                        y = (offset.y + newScale * panChange.y).coerceIn(-maxY, maxY)
                    )
                }

                ImageContentWithTransform(imageModel, state, scale, offset)
            }
        }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ImageContentWithTransform(
    imageModel: ImageModel,
    state: TransformableState,
    scale: Float,
    offset: Offset
) {
    if (imageModel.byteImage != null) {
        Image(
            bitmap = imageModel.byteImage.decodeToImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .transformable(state)
        )
    } else {
        AsyncImage(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .transformable(state),
            model = imageModel.preSignedUrl,
            contentDescription = null
        )
    }
}

@Composable
fun CameraScreen(
    cameraController: CameraController,
    onCloseButtonClicked: () -> Unit,
    onCameraButtonClicked: (ImageModel) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isFlashOn by remember { mutableStateOf(false) }

    /*Box(modifier = Modifier.fillMaxSize()) {
        IconButton(
            onClick = onCloseButtonClicked,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.surface
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            IconButton(onClick = {
                isFlashOn = !isFlashOn
                cameraController.toggleFlashMode()
            }) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                    contentDescription = "Toggle Flash",
                    tint = if (isFlashOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            }

            IconButton(onClick = {
                scope.launch {
                    val result = cameraController.takePicture()
                    if (result is ImageCaptureResult.Success) {
                        onCameraButtonClicked(ImageModel(byteImage = result.byteArray))
                        onCloseButtonClicked()
                    } else if (result is ImageCaptureResult.Error) {
                        println("Image Capture Error: ${result.exception.message}")
                    }
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Camera,
                    contentDescription = "Capture",
                    tint = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
    }*/

    Box(modifier = Modifier.fillMaxSize()) {

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
                Box(contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                when (val result = cameraController.takePicture()) {
                                    is ImageCaptureResult.Success -> {
                                        onCameraButtonClicked(ImageModel(byteImage = result.byteArray))
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