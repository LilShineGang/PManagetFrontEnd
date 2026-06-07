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

expect suspend fun readPickedImageUpload(imagePath: String): PickedImageUpload?

fun resolveAppImageUrl(imagePath: String?): String? {
    imagePath ?: return null
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath
    if (imagePath.startsWith("/")) return "${getBaseUrl()}$imagePath"
    return imagePath
}
