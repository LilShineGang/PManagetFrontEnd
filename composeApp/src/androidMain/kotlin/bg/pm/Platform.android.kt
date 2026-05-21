package bg.pm

import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getApiBaseUrl(): String = "http://10.160.81.1:8000"

actual suspend fun readPickedImageUpload(imagePath: String): PickedImageUpload? {
    val context = ImagePickerHolder.appContext ?: return null
    val uri = Uri.parse(imagePath)
    val resolver = context.contentResolver
    val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
    val contentType = resolver.getType(uri) ?: "application/octet-stream"

    val fileName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            } else {
                null
            }
        }
        ?: "image"

    return PickedImageUpload(
        fileName = fileName,
        bytes = bytes,
        contentType = contentType,
    )
}

actual fun pickImageFile(callback: (String?) -> Unit) {
    ImagePickerHolder.pick(callback)
}