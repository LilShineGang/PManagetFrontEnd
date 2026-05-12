package bg.pm

import androidx.compose.runtime.*
import bg.pm.ui.PantallaPrincipal
import bg.pm.ui.login.LoginAdministrador
import bg.pm.ui.login.LoginViewModel
import bg.pm.ui.login.RegisterUsuario
import bg.pm.ui.login.RegisterViewModel
import bg.pm.ui.theme.PManagerTheme

private enum class Pantalla { LOGIN, REGISTRO, PRINCIPAL }

@Composable
fun App() {
    PManagerTheme {
        var pantalla by remember { mutableStateOf(Pantalla.LOGIN) }
        val loginViewModel = remember { LoginViewModel() }
        val registerViewModel = remember { RegisterViewModel() }

        when (pantalla) {
            Pantalla.LOGIN -> LoginAdministrador(
                viewModel = loginViewModel,
                onLoginExitoso = { pantalla = Pantalla.PRINCIPAL },
                onCancelar = { pantalla = Pantalla.REGISTRO }
            )
            Pantalla.REGISTRO -> RegisterUsuario(
                viewModel = registerViewModel,
                onRegistroExitoso = {
                    loginViewModel.clear()
                    pantalla = Pantalla.LOGIN
                },
                onCancelar = { pantalla = Pantalla.LOGIN }
            )
            Pantalla.PRINCIPAL -> PantallaPrincipal(
                onCerrarSesion = {
                    loginViewModel.clear()
                    pantalla = Pantalla.LOGIN
                }
            )
        }
    }
}
