package bg.pm

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun pickImageFile(callback: (String?) -> Unit) {
    ImagePickerHolder.pick(callback)
}

actual fun getBaseUrl(): String = "http://192.168.1.169:8000"