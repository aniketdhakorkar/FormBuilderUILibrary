package cameraK.plugins

import cameraK.controller.CameraController

/**
 * Interface that all camera plugins must implement.
 */
interface CameraPlugin {
    /**
     * Initializes the plugin with the provided [CameraController].
     *
     * @param cameraController The [CameraController] instance.
     */
    fun initialize(cameraController: CameraController)
}