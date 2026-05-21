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

expect fun getApiBaseUrl(): String

fun resolveAppImageUrl(image: String?): String? {
    val normalized = image?.trim()?.takeIf { it.isNotEmpty() } ?: return null

    if (normalized.startsWith("content://")) {
        return null
    }

    if (normalized.startsWith("/static/")) {
        return "${getApiBaseUrl()}$normalized"
    }

    val staticPathIndex = normalized.indexOf("/static/")
    if (staticPathIndex >= 0) {
        return getApiBaseUrl() + normalized.substring(staticPathIndex)
    }

    return normalized
}

expect suspend fun readPickedImageUpload(imagePath: String): PickedImageUpload?

expect fun pickImageFile(callback: (String?) -> Unit)