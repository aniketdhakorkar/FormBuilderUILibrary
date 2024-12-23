package model

data class ImageModel(
    val byteImage: ByteArray?,
    val resourcePath: String = "",
    val preSignedUrl: String = "",
    val isLoading: Boolean = true
)
