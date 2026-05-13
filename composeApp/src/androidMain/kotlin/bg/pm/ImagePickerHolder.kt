package bg.pm

object ImagePickerHolder {
    var pendingCallback: ((String?) -> Unit)? = null

    fun pick(callback: (String?) -> Unit) {
        pendingCallback = callback
        launchPicker?.invoke()
    }

    var launchPicker: (() -> Unit)? = null
}
