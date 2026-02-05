package bg.pm

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import bg.pm.ui.login.LoginAdministrador
import bg.pm.ui.login.LoginViewModel
import bg.pm.ui.login.RegisterUsuario
import bg.pm.ui.login.RegisterViewModel

@Composable
fun App() {
    MaterialTheme {
        val loginViewModel = remember { LoginViewModel() }
        val registerViewModel = remember { RegisterViewModel() }

        RegisterUsuario(
            viewModel = registerViewModel,
            onRegistroExitoso = {
            },
            onCancelar = {
            }
        )
    }
}