package bg.pm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import bg.pm.network.TokenStorage

class MainActivity : ComponentActivity() {
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val callback = ImagePickerHolder.pendingCallback
        ImagePickerHolder.pendingCallback = null
        callback?.invoke(uri?.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        TokenStorage.init(this)
        ImagePickerHolder.appContext = applicationContext
        ImagePickerHolder.launchPicker = { pickImage.launch("image/*") }
        setContent {
            App()
        }
    }
}