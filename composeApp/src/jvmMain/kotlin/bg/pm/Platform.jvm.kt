package bg.pm

import java.io.File

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getApiBaseUrl(): String {
    return System.getProperty("bg.pm.api.baseUrl")
        ?: System.getenv("PM_API_BASE_URL")
        ?: "http://127.0.0.1:8000"
}

actual suspend fun readPickedImageUpload(imagePath: String): PickedImageUpload? {
    val file = File(imagePath)
    if (!file.exists() || !file.isFile) {
        return null
    }

    val contentType = when (file.extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "bmp" -> "image/bmp"
        else -> "application/octet-stream"
    }

    return PickedImageUpload(
        fileName = file.name,
        bytes = file.readBytes(),
        contentType = contentType,
    )
}

actual fun pickImageFile(callback: (String?) -> Unit) {
    javax.swing.SwingUtilities.invokeLater {
        val chooser = javax.swing.JFileChooser().apply {
            fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
                "Imágenes", "jpg", "jpeg", "png", "gif", "webp", "bmp"
            )
            dialogTitle = "Seleccionar imagen"
        }
        val result = chooser.showOpenDialog(null)
        callback(
            if (result == javax.swing.JFileChooser.APPROVE_OPTION) chooser.selectedFile.absolutePath
            else null
        )
    }
}