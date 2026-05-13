package bg.pm

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun pickImageFile(callback: (String?) -> Unit) { callback(null) }