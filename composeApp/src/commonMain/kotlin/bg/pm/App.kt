package bg.pm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import bg.pm.navigation.NavGraph
import bg.pm.ui.common.LocalAppImageLoader
import bg.pm.ui.login.LoginViewModel
import bg.pm.ui.login.RegisterViewModel
import bg.pm.ui.theme.GlyphTheme
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory

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
        GlyphTheme {
            val navController = rememberNavController()
            val loginViewModel = remember { LoginViewModel() }
            val registerViewModel = remember { RegisterViewModel() }

            NavGraph(
                navController = navController,
                loginViewModel = loginViewModel,
                registerViewModel = registerViewModel,
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
            val perfil = ApiService.obtenerPerfil(accessToken)
            SessionManager.role = perfil?.role
            true
        }
        TokenStorage.isRefreshTokenValid() -> {
            val refreshToken = TokenStorage.getRefreshToken() ?: return false
            try {
                val response = ApiService.refreshToken(refreshToken)
                if (response.access_token != null) {
                    TokenStorage.saveTokens(response.access_token, response.refresh_token ?: "", username)
                    SessionManager.accessToken = response.access_token
                    SessionManager.username = username
                    val perfil = ApiService.obtenerPerfil(response.access_token)
                    SessionManager.role = perfil?.role
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

