package bg.pm

import android.content.Context

object ImagePickerHolder {
    var pendingCallback: ((String?) -> Unit)? = null
    var appContext: Context? = null

    fun pick(callback: (String?) -> Unit) {
        pendingCallback = callback
        launchPicker?.invoke()
    }

    var launchPicker: (() -> Unit)? = null
}
