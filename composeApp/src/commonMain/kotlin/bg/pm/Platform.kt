package bg.pm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun pickImageFile(callback: (String?) -> Unit)

expect fun getBaseUrl(): String