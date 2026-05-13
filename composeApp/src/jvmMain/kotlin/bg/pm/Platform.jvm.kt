package bg.pm

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

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