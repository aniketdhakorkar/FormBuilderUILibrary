package cameraK.builder

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import cameraK.builder.CameraControllerBuilder
import cameraK.controller.CameraController
import cameraK.enums.CameraLens
import cameraK.enums.Directory
import cameraK.enums.FlashMode
import cameraK.enums.ImageFormat
import cameraK.enums.Rotation
import cameraK.enums.TorchMode
import cameraK.plugins.CameraPlugin
import cameraK.utils.InvalidConfigurationException


/**
 * Android-specific implementation of [CameraControllerBuilder].
 *
 * @param context The Android [Context], typically an Activity or Application context.
 * @param lifecycleOwner The [LifecycleOwner], usually the hosting Activity or Fragment.
 */
class AndroidCameraControllerBuilder(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : CameraControllerBuilder {

    private var flashMode: FlashMode = FlashMode.OFF
    private var cameraLens: CameraLens = CameraLens.BACK
    private var rotation: Rotation = Rotation.ROTATION_0
    private var imageFormat: ImageFormat? = null
    private var directory: Directory? = null
    private var torchMode: TorchMode = TorchMode.AUTO
    private val plugins = mutableListOf<CameraPlugin>()

    override fun setFlashMode(flashMode: FlashMode): CameraControllerBuilder {
        this.flashMode = flashMode
        return this
    }

    override fun setCameraLens(cameraLens: CameraLens): CameraControllerBuilder {
        this.cameraLens = cameraLens
        return this
    }

    override fun setRotation(rotation: Rotation): CameraControllerBuilder {
        this.rotation = rotation
        return this
    }

    override fun setTorchMode(torchMode: TorchMode): CameraControllerBuilder {
        this.torchMode = torchMode
        return this
    }

    override fun setImageFormat(imageFormat: ImageFormat): CameraControllerBuilder {
        this.imageFormat = imageFormat
        return this
    }

    override fun setDirectory(directory: Directory): CameraControllerBuilder {
        this.directory = directory
        return this
    }

    override fun addPlugin(plugin: CameraPlugin): CameraControllerBuilder {
        plugins.add(plugin)
        return this
    }

    override fun build(): CameraController {

        val format = imageFormat ?: throw InvalidConfigurationException("ImageFormat must be set.")
        val dir = directory ?: throw InvalidConfigurationException("Directory must be set.")


        /* if (flashMode == FlashMode.ON && cameraLens == CameraLens.FRONT) {
             throw InvalidConfigurationException("Flash mode ON is not supported with the front camera.")
         }*/
        val cameraController = CameraController(
            context = context,
            lifecycleOwner = lifecycleOwner,
            flashMode = flashMode,
            cameraLens = cameraLens,
            rotation = rotation,
            imageFormat = format,
            directory = dir,
            plugins = plugins,
            torchMode = torchMode
        )
        plugins.forEach {
            it.initialize(cameraController)
        }


        return cameraController
    }
}