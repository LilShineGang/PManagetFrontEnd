package bg.pm

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun getApiBaseUrl(): String = "http://127.0.0.1:8000"

actual suspend fun readPickedImageUpload(imagePath: String): PickedImageUpload? = null

actual fun pickImageFile(callback: (String?) -> Unit) { callback(null) }