package bg.pm

class JsPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun pickImageFile(callback: (String?) -> Unit) { callback(null) }