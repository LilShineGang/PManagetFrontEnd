package bg.pm

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getBaseUrl(): String = "http://127.0.0.1:8000"

actual suspend fun readPickedImageUpload(imagePath: String): PickedImageUpload? = null

actual fun pickImageFile(callback: (String?) -> Unit) { callback(null) }