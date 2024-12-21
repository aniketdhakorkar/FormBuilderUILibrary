package model

data class ImageModel(
    val byteImage: ByteArray?,
    val resourcePath: String = "",
    val resourceId: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String = ""
)
