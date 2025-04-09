package ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.TransformableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.FlashMode
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.enums.TorchMode
import com.kashif.cameraK.permissions.Permissions
import com.kashif.cameraK.permissions.providePermissions
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.ui.CameraPreview
import kotlinx.coroutines.launch
import model.ImageModel
import model.parameters.Style
import ui.helper.CardContainer

@Composable
fun CreateCamera(
    question: String,
    description: String,
    style: Style?,
    isMandatory: String,
    imageList: List<ImageModel>,
    action: String,
    isViewCamera: Boolean,
    onPhotoTaken: (ImageModel) -> Unit,
    onPhotoDeleteButtonClicked: (Int) -> Unit,
    onImageViewButtonClicked: (ImageModel) -> Unit,
    onImagePreviewDialogDismiss: () -> Unit
) {
    val isOpenCamera = remember { mutableStateOf(false) }
    val cameraController = remember { mutableStateOf<CameraController?>(null) }
    val permissions: Permissions = providePermissions()
    val cameraPermissionState =
        remember { mutableStateOf(permissions.hasCameraPermission()) }
    val selectedImageIndex = remember { mutableStateOf<Int?>(null) }

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
                    onPhotoClicked = {
                        selectedImageIndex.value = it
                    },
                    onImageViewButtonClicked = onImageViewButtonClicked
                )
            }

            item {
                AddPhotoButton { isOpenCamera.value = true }
            }
        }
    }

    if (isViewCamera) {
        selectedImageIndex.value?.let { index ->
            ImagePreviewDialog(
                imageModel = imageList.getOrNull(index),
                onDismiss = onImagePreviewDialogDismiss
            )
        } ?: run {
            onImagePreviewDialogDismiss
        }
    }

    if (isOpenCamera.value) {
        PermissionsHandler(
            permissions = permissions,
            cameraPermissionState = cameraPermissionState
        )
        if (cameraPermissionState.value) {
            CameraDialog(
                isOpenCamera = isOpenCamera,
                cameraController = cameraController,
                onPhotoTaken = onPhotoTaken,
                onCloseButtonClick = { isOpenCamera.value = false }
            )
        }
    }
}

@Composable
fun DisplayImageItem(
    image: ImageModel,
    index: Int,
    action: String,
    onPhotoDeleteButtonClicked: (Int) -> Unit,
    onPhotoClicked: (Int) -> Unit,
    onImageViewButtonClicked: (ImageModel) -> Unit
) {
    Box(
        modifier = Modifier
            .width(220.dp)
            .height(280.dp)
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onPhotoClicked(index)
                onImageViewButtonClicked(image)
            }
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        ImageContent(image = image)

        when (action) {
            "view", "edit" -> {
                if ((image.preSignedUrl.isEmpty() && image.byteImage == null))
                    Button(
                        onClick = {
                            onPhotoClicked(index)
                            onImageViewButtonClicked(image)
                        },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = Icons.Default.RemoveRedEye,
                                contentDescription = "View"
                            )
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

@Composable
fun ImageContent(image: ImageModel) {
    if (image.byteImage != null) {
        Image(
            bitmap = image.byteImage.decodeToImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    } else if (image.preSignedUrl.isNotEmpty()) {
        AsyncImage(
            model = image.preSignedUrl,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Image(imageVector = Icons.Filled.Image, contentDescription = null)
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
fun ImagePreviewDialog(imageModel: ImageModel?, onDismiss: () -> Unit) {
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

@Composable
fun ImageContentWithTransform(
    imageModel: ImageModel?,
    state: TransformableState,
    scale: Float,
    offset: Offset
) {
    if (imageModel?.byteImage != null) {
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
            model = imageModel?.preSignedUrl ?: "",
            contentDescription = null
        )
    }
}

@Composable
fun CameraDialog(
    isOpenCamera: MutableState<Boolean>,
    cameraController: MutableState<CameraController?>,
    onPhotoTaken: (ImageModel) -> Unit,
    onCloseButtonClick: () -> Unit
) {
    CreateDialogBox(
        onDismissRequest = { isOpenCamera.value = false },
        content = {
            CameraContent(
                cameraController = cameraController,
                onPhotoTaken = onPhotoTaken,
                onCloseButtonClick = onCloseButtonClick
            )
        }
    )
}

@Composable
fun CameraContent(
    cameraController: MutableState<CameraController?>,
    onPhotoTaken: (ImageModel) -> Unit,
    onCloseButtonClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.primary)) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            cameraConfiguration = {
                setCameraLens(CameraLens.BACK)
                setFlashMode(FlashMode.OFF)
                setImageFormat(ImageFormat.JPEG)
                setDirectory(Directory.PICTURES)
                setTorchMode(TorchMode.OFF)
            },
            onCameraControllerReady = {
                print("==> Camera Controller Ready")
                cameraController.value = it

            }
        )

        cameraController.value?.let { controller ->
            EnhancedCameraScreen(
                cameraController = controller,
                onPhotoTaken = onPhotoTaken,
                onCloseButtonClick = onCloseButtonClick
            )
        }
    }
}

@Composable
fun EnhancedCameraScreen(
    cameraController: CameraController,
    onPhotoTaken: (ImageModel) -> Unit,
    onCloseButtonClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isTorchOn by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        TopControlsBar(
            isTorchOn = isTorchOn,
            onTorchToggle = {
                isTorchOn = !isTorchOn
                cameraController.toggleTorchMode()
            },
            onCloseButtonClick = onCloseButtonClick
        )


        BottomControls(
            modifier = Modifier.align(Alignment.BottomCenter),
            onCapture = {
                scope.launch {
                    handleImageCapture(
                        cameraController = cameraController,
                        onImageCaptured = onPhotoTaken,
                        onCloseButtonClick = onCloseButtonClick
                    )
                }
            }
        )
    }
}

@Composable
private fun TopControlsBar(
    isTorchOn: Boolean,
    onTorchToggle: () -> Unit,
    onCloseButtonClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        color = Color.Black.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CameraControlSwitch(
                    isTorchOn = isTorchOn,
                    onCheckedChange = onTorchToggle,
                    onCloseButtonClick = onCloseButtonClick
                )
            }
        }
    }
}

@Composable
private fun CameraControlSwitch(
    isTorchOn: Boolean,
    onCheckedChange: () -> Unit,
    onCloseButtonClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.size(24.dp),
            onClick = onCloseButtonClick
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White
            )
        }

        IconButton(
            modifier = Modifier.size(24.dp),
            onClick = onCheckedChange
        ) {
            Icon(
                imageVector = if (isTorchOn) Flash_on else Flash_off,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
private fun BottomControls(modifier: Modifier = Modifier, onCapture: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        FilledTonalButton(
            onClick = onCapture,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Camera,
                contentDescription = "Capture",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

private suspend fun handleImageCapture(
    cameraController: CameraController,
    onImageCaptured: (ImageModel) -> Unit,
    onCloseButtonClick: () -> Unit
) {
    when (val result = cameraController.takePicture()) {
        is ImageCaptureResult.Success -> {
            onImageCaptured(ImageModel(byteImage = result.byteArray))
            onCloseButtonClick()
        }

        is ImageCaptureResult.Error -> {
            println("Image Capture Error: ${result.exception.message}")
        }
    }
}


val Flash_on: ImageVector
    get() {
        if (_Flash_on != null) {
            return _Flash_on!!
        }
        _Flash_on = ImageVector.Builder(
            name = "Flash_on",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(480f, 624f)
                lineToRelative(128f, -184f)
                horizontalLineTo(494f)
                lineToRelative(80f, -280f)
                horizontalLineTo(360f)
                verticalLineToRelative(320f)
                horizontalLineToRelative(120f)
                close()
                moveTo(400f, 880f)
                verticalLineToRelative(-320f)
                horizontalLineTo(280f)
                verticalLineToRelative(-480f)
                horizontalLineToRelative(400f)
                lineToRelative(-80f, 280f)
                horizontalLineToRelative(160f)
                close()
                moveToRelative(80f, -400f)
                horizontalLineTo(360f)
                close()
            }
        }.build()
        return _Flash_on!!
    }

private var _Flash_on: ImageVector? = null


val Flash_off: ImageVector
    get() {
        if (_Flash_off != null) {
            return _Flash_off!!
        }
        _Flash_off = ImageVector.Builder(
            name = "Flash_off",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 0.5f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(280f, 80f)
                horizontalLineToRelative(400f)
                lineToRelative(-80f, 280f)
                horizontalLineToRelative(160f)
                lineTo(643f, 529f)
                lineToRelative(-57f, -57f)
                lineToRelative(22f, -32f)
                horizontalLineToRelative(-54f)
                lineToRelative(-47f, -47f)
                lineToRelative(67f, -233f)
                horizontalLineTo(360f)
                verticalLineToRelative(86f)
                lineToRelative(-80f, -80f)
                close()
                moveTo(400f, 880f)
                verticalLineToRelative(-320f)
                horizontalLineTo(280f)
                verticalLineToRelative(-166f)
                lineTo(55f, 169f)
                lineToRelative(57f, -57f)
                lineToRelative(736f, 736f)
                lineToRelative(-57f, 57f)
                lineToRelative(-241f, -241f)
                close()
                moveToRelative(73f, -521f)
            }
        }.build()
        return _Flash_off!!
    }

private var _Flash_off: ImageVector? = null

val Camera: ImageVector
    get() {
        if (_Camera != null) {
            return _Camera!!
        }
        _Camera = ImageVector.Builder(
            name = "Camera",
            defaultWidth = 15.dp,
            defaultHeight = 15.dp,
            viewportWidth = 15f,
            viewportHeight = 15f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(2f, 3f)
                curveTo(1.4477f, 3f, 1f, 3.4477f, 1f, 4f)
                verticalLineTo(11f)
                curveTo(1f, 11.5523f, 1.4477f, 12f, 2f, 12f)
                horizontalLineTo(13f)
                curveTo(13.5523f, 12f, 14f, 11.5523f, 14f, 11f)
                verticalLineTo(4f)
                curveTo(14f, 3.4477f, 13.5523f, 3f, 13f, 3f)
                horizontalLineTo(2f)
                close()
                moveTo(0f, 4f)
                curveTo(0f, 2.8954f, 0.8954f, 2f, 2f, 2f)
                horizontalLineTo(13f)
                curveTo(14.1046f, 2f, 15f, 2.8954f, 15f, 4f)
                verticalLineTo(11f)
                curveTo(15f, 12.1046f, 14.1046f, 13f, 13f, 13f)
                horizontalLineTo(2f)
                curveTo(0.8954f, 13f, 0f, 12.1046f, 0f, 11f)
                verticalLineTo(4f)
                close()
                moveTo(2f, 4.25f)
                curveTo(2f, 4.1119f, 2.1119f, 4f, 2.25f, 4f)
                horizontalLineTo(4.75f)
                curveTo(4.8881f, 4f, 5f, 4.1119f, 5f, 4.25f)
                verticalLineTo(5.75454f)
                curveTo(5f, 5.8926f, 4.8881f, 6.0045f, 4.75f, 6.0045f)
                horizontalLineTo(2.25f)
                curveTo(2.1119f, 6.0045f, 2f, 5.8926f, 2f, 5.7545f)
                verticalLineTo(4.25f)
                close()
                moveTo(12.101f, 7.58421f)
                curveTo(12.101f, 9.0207f, 10.9365f, 10.1853f, 9.5f, 10.1853f)
                curveTo(8.0635f, 10.1853f, 6.8989f, 9.0207f, 6.8989f, 7.5842f)
                curveTo(6.8989f, 6.1477f, 8.0635f, 4.9832f, 9.5f, 4.9832f)
                curveTo(10.9365f, 4.9832f, 12.101f, 6.1477f, 12.101f, 7.5842f)
                close()
                moveTo(13.101f, 7.58421f)
                curveTo(13.101f, 9.573f, 11.4888f, 11.1853f, 9.5f, 11.1853f)
                curveTo(7.5112f, 11.1853f, 5.8989f, 9.573f, 5.8989f, 7.5842f)
                curveTo(5.8989f, 5.5954f, 7.5112f, 3.9832f, 9.5f, 3.9832f)
                curveTo(11.4888f, 3.9832f, 13.101f, 5.5954f, 13.101f, 7.5842f)
                close()
            }
        }.build()
        return _Camera!!
    }

private var _Camera: ImageVector? = null