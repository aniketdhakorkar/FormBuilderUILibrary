package cameraK.builder

import cameraK.builder.CameraControllerBuilder

/**
 * Creates an iOS-specific [CameraControllerBuilder].
 *
 * @return An instance of [CameraControllerBuilder].
 */
fun createIOSCameraControllerBuilder(): CameraControllerBuilder = IOSCameraControllerBuilder()
