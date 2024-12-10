package cameraK.builder

import cameraK.controller.CameraController
import cameraK.enums.CameraLens
import cameraK.enums.Directory
import cameraK.enums.FlashMode
import cameraK.enums.ImageFormat
import cameraK.enums.Rotation
import cameraK.enums.TorchMode
import cameraK.plugins.CameraPlugin

/**
 * Builder interface for constructing a [CameraController] with customizable configurations and plugins.
 */
interface CameraControllerBuilder {
    fun setFlashMode(flashMode: FlashMode): CameraControllerBuilder
    fun setCameraLens(cameraLens: CameraLens): CameraControllerBuilder
    fun setRotation(rotation: Rotation): CameraControllerBuilder
    fun setImageFormat(imageFormat: ImageFormat): CameraControllerBuilder
    fun setDirectory(directory: Directory): CameraControllerBuilder

    /**
     * Adds a [CameraPlugin] to the [CameraController].
     *
     * @param plugin The plugin to add.
     * @return The current instance of [CameraControllerBuilder].
     */
    fun addPlugin(plugin: CameraPlugin): CameraControllerBuilder

    /**
     * Builds and returns a configured instance of [CameraController].
     *
     * @throws InvalidConfigurationException If mandatory parameters are missing or configurations are incompatible.
     * @return A fully configured [CameraController] instance.
     */
    fun build(): CameraController
    fun setTorchMode(torchMode: TorchMode): CameraControllerBuilder
}