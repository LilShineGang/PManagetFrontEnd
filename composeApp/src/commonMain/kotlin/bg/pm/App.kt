package bg.pm

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bg.pm.network.ApiService
import bg.pm.network.SessionManager
import bg.pm.network.TokenStorage
import bg.pm.ui.PantallaPrincipal
import bg.pm.ui.login.LoginAdministrador
import bg.pm.ui.login.LoginViewModel
import bg.pm.ui.login.RegisterUsuario
import bg.pm.ui.login.RegisterViewModel
import bg.pm.ui.theme.PManagerTheme
import coil3.ImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.compose.LocalPlatformContext
import androidx.compose.runtime.CompositionLocalProvider
import bg.pm.ui.LocalAppImageLoader

private enum class Pantalla { CARGANDO, LOGIN, REGISTRO, PRINCIPAL }

@Composable
fun App() {
    val platformContext = LocalPlatformContext.current
    val imageLoader = remember(platformContext) {
        ImageLoader.Builder(platformContext)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
    CompositionLocalProvider(LocalAppImageLoader provides imageLoader) {
    PManagerTheme {
        var pantalla by remember { mutableStateOf(Pantalla.CARGANDO) }
        val loginViewModel = remember { LoginViewModel() }
        val registerViewModel = remember { RegisterViewModel() }

        when (pantalla) {
            Pantalla.CARGANDO -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                LaunchedEffect(Unit) {
                    pantalla = if (tryAutoLogin()) Pantalla.PRINCIPAL else Pantalla.LOGIN
                }
            }
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
                    TokenStorage.clear()
                    SessionManager.clear()
                    loginViewModel.clear()
                    pantalla = Pantalla.LOGIN
                }
            )
        }
    }
    } // CompositionLocalProvider
}

private suspend fun tryAutoLogin(): Boolean {
    val accessToken = TokenStorage.getAccessToken() ?: return false
    val username = TokenStorage.getUsername() ?: return false

    return when {
        TokenStorage.isAccessTokenValid() -> {
            SessionManager.accessToken = accessToken
            SessionManager.username = username
            true
        }
        TokenStorage.isRefreshTokenValid() -> {
            val refreshToken = TokenStorage.getRefreshToken() ?: return false
            try {
                val response = ApiService.refreshToken(refreshToken)
                if (response.access_token != null && response.refresh_token != null) {
                    TokenStorage.saveTokens(response.access_token, response.refresh_token, username)
                    SessionManager.accessToken = response.access_token
                    SessionManager.username = username
                    true
                } else {
                    TokenStorage.clear()
                    false
                }
            } catch (e: Exception) {
                TokenStorage.clear()
                false
            }
        }
        else -> {
            TokenStorage.clear()
            false
        }
    }
}

