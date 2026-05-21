package bg.pm

interface Platform {
    val name: String
}

data class PickedImageUpload(
    val fileName: String,
    val bytes: ByteArray,
    val contentType: String,
)

expect fun getPlatform(): Platform

expect fun pickImageFile(callback: (String?) -> Unit)

expect fun getBaseUrl(): String
