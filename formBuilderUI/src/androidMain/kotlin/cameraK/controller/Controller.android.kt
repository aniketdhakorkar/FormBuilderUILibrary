package cameraK.controller

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import cameraK.enums.CameraLens
import cameraK.enums.Directory
import cameraK.enums.FlashMode
import cameraK.enums.ImageFormat
import cameraK.enums.Rotation
import cameraK.enums.TorchMode
import cameraK.plugins.CameraPlugin
import cameraK.result.ImageCaptureResult
import cameraK.utils.InvalidConfigurationException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Android-specific implementation of [CameraController] using CameraX.
 *
 * @param context The Android [Context].
 * @param lifecycleOwner The [LifecycleOwner] to bind the camera lifecycle.
 * @param flashMode The desired [FlashMode].
 * @param cameraLens The desired [CameraLens].
 * @param rotation The desired [Rotation].
 * @param imageFormat The desired [ImageFormat].
 * @param directory The desired [Directory] to save images.
 */
actual class CameraController(
    val context: Context,
    val lifecycleOwner: LifecycleOwner,
    internal var flashMode: FlashMode,
    internal var torchMode: TorchMode,
    internal var cameraLens: CameraLens,
    var rotation: Rotation,
    internal var imageFormat: ImageFormat,
    internal var directory: Directory,
    internal var plugins: MutableList<CameraPlugin>
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var previewView: PreviewView? = null

    private val imageCaptureListeners = mutableListOf<(ByteArray) -> Unit>()

    fun bindCamera(previewView: PreviewView, onCameraReady: () -> Unit = {}) {
        this.previewView = previewView
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                cameraProvider?.unbindAll()

                preview = Preview.Builder()
                    .setTargetRotation(rotation.toSurfaceRotation())
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraLens.toCameraXLensFacing())
                    .build()

                // Setup ImageCapture Use Case
                imageCapture = ImageCapture.Builder()
                    .setFlashMode(flashMode.toCameraXFlashMode())
                    .setTargetRotation(rotation.toSurfaceRotation())
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Setup ImageAnalysis Use Case only if needed
                val useCases = mutableListOf(preview!!, imageCapture!!)
                imageAnalyzer?.let { useCases.add(it) }


                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    *useCases.toTypedArray()
                )

                onCameraReady()

            } catch (exc: Exception) {
                println("Use case binding failed: ${exc.message}")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    fun updateImageAnalyzer() {
        camera?.let {
            cameraProvider?.unbind(imageAnalyzer)
            imageAnalyzer?.let { analyzer ->
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.Builder().requireLensFacing(cameraLens.toCameraXLensFacing())
                        .build(),
                    analyzer // Only bind the analyzer without touching the rest of the setup
                )
            }
        } ?: throw InvalidConfigurationException("Camera not initialized.")
    }


    /*actual suspend fun takePicture(): ImageCaptureResult =
        suspendCancellableCoroutine { cont ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(createTempFile()).build()

            imageCapture?.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        // Read the image data into a ByteArray
                        val byteArray = try {
                            BitmapFactory.decodeFile(output.savedUri?.path)
                                ?.let { bitmap ->
                                    val stream = ByteArrayOutputStream()
                                    bitmap.compress(
                                        when (imageFormat) {
                                            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
                                            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
                                        },
                                        80,
                                        stream
                                    )
                                    stream.toByteArray()
                                }
                        } catch (e: Exception) {
                            null
                        }

                        if (byteArray != null) {
                            imageCaptureListeners.forEach { it(byteArray) }
                            cont.resume(ImageCaptureResult.Success(byteArray))
                        } else {
                            cont.resume(ImageCaptureResult.Error(Exception("Failed to convert image to ByteArray.")))
                        }
                    }

                    override fun onError(exc: ImageCaptureException) {
                        cont.resume(ImageCaptureResult.Error(exc))
                    }
                }
            )
                ?: cont.resume(ImageCaptureResult.Error(Exception("ImageCapture use case is not initialized.")))
        }*/

    actual suspend fun takePicture(): ImageCaptureResult =
        suspendCancellableCoroutine { cont ->
            val outputOptions = ImageCapture.OutputFileOptions.Builder(createTempFile()).build()

            imageCapture?.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        val byteArray = try {
                            output.savedUri?.path?.let { path ->
                                // Read Exif orientation
                                val exif = ExifInterface(path)
                                val rotationDegrees = when (exif.getAttributeInt(
                                    ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_NORMAL
                                )) {
                                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                                    else -> 0
                                }

                                // Decode Bitmap and apply rotation
                                BitmapFactory.decodeFile(path)?.let { bitmap ->
                                    val matrix =
                                        Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                    val rotatedBitmap = Bitmap.createBitmap(
                                        bitmap,
                                        0,
                                        0,
                                        bitmap.width,
                                        bitmap.height,
                                        matrix,
                                        true
                                    )

                                    // Convert the rotated Bitmap to ByteArray
                                    val stream = ByteArrayOutputStream()
                                    rotatedBitmap.compress(
                                        when (imageFormat) {
                                            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
                                            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
                                        },
                                        80,
                                        stream
                                    )
                                    stream.toByteArray()
                                }
                            }
                        } catch (e: Exception) {
                            null
                        }

                        if (byteArray != null) {
                            imageCaptureListeners.forEach { it(byteArray) }
                            cont.resume(ImageCaptureResult.Success(byteArray))
                        } else {
                            cont.resume(ImageCaptureResult.Error(Exception("Failed to convert image to ByteArray.")))
                        }
                    }

                    override fun onError(exc: ImageCaptureException) {
                        cont.resume(ImageCaptureResult.Error(exc))
                    }
                }
            )
                ?: cont.resume(ImageCaptureResult.Error(Exception("ImageCapture use case is not initialized.")))
        }

    actual fun toggleFlashMode() {
        flashMode = when (flashMode) {
            FlashMode.OFF -> FlashMode.ON
            FlashMode.ON -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.OFF
        }
        imageCapture?.flashMode = flashMode.toCameraXFlashMode()
    }

    actual fun setFlashMode(mode: FlashMode) {
        imageCapture?.flashMode = mode.toCameraXFlashMode()
    }

    actual fun getFlashMode(): FlashMode? {
        fun Int.toCameraKFlashMode(): FlashMode? {
            return when (this) {
                ImageCapture.FLASH_MODE_ON -> FlashMode.ON
                ImageCapture.FLASH_MODE_OFF -> FlashMode.OFF
                ImageCapture.FLASH_MODE_AUTO -> FlashMode.AUTO
                else -> null
            }
        }

        return imageCapture?.flashMode?.toCameraKFlashMode()
    }

    actual fun toggleTorchMode() {
        torchMode = when (torchMode) {
            TorchMode.OFF -> TorchMode.ON
            TorchMode.ON -> TorchMode.OFF
            else -> TorchMode.OFF
        }
        camera?.cameraControl?.enableTorch(torchMode == TorchMode.ON)
    }

    actual fun toggleCameraLens() {
        cameraLens = if (cameraLens == CameraLens.BACK) CameraLens.FRONT else CameraLens.BACK
        previewView?.let { bindCamera(it) }
    }


    actual fun setCameraRotation(rotation: Rotation) {
        this.rotation = rotation
        imageCapture?.targetRotation = rotation.toSurfaceRotation()
        preview?.targetRotation = rotation.toSurfaceRotation()
    }

    actual fun startSession() {
        // CameraX handles session start based on lifecycle
    }

    actual fun stopSession() {
        cameraProvider?.unbindAll()
    }

    actual fun addImageCaptureListener(listener: (ByteArray) -> Unit) {
        imageCaptureListeners.add(listener)
    }

    actual fun initializeControllerPlugins() {
        plugins.forEach { it.initialize(this) }
    }

    // Helper Methods
    private fun createTempFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.externalCacheDir?.absolutePath ?: context.cacheDir.absolutePath
        return File.createTempFile(
            "IMG_$timeStamp",
            ".${imageFormat.extension}",
            File(storageDir)
        )
    }

    private fun FlashMode.toCameraXFlashMode(): Int = when (this) {
        FlashMode.ON -> ImageCapture.FLASH_MODE_ON
        FlashMode.OFF -> ImageCapture.FLASH_MODE_OFF
        FlashMode.AUTO -> ImageCapture.FLASH_MODE_AUTO
    }

    private fun CameraLens.toCameraXLensFacing(): Int = when (this) {
        CameraLens.FRONT -> CameraSelector.LENS_FACING_FRONT
        CameraLens.BACK -> CameraSelector.LENS_FACING_BACK
    }

    fun Rotation.toSurfaceRotation(): Int = when (this) {
        Rotation.ROTATION_0 -> android.view.Surface.ROTATION_0
        Rotation.ROTATION_90 -> android.view.Surface.ROTATION_90
        Rotation.ROTATION_180 -> android.view.Surface.ROTATION_180
        Rotation.ROTATION_270 -> android.view.Surface.ROTATION_270
    }
}