package bg.pm.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import bg.pm.network.ApiService
import bg.pm.network.SessionManager
import bg.pm.network.TokenStorage
import bg.pm.ui.home.PantallaPrincipal
import bg.pm.ui.login.LoginAdministrador
import bg.pm.ui.login.LoginViewModel
import bg.pm.ui.login.RegisterUsuario
import bg.pm.ui.login.RegisterViewModel

/**
 * Grafo de navegación principal de la aplicación.
 *
 * @param navController Controlador de navegación.
 * @param loginViewModel ViewModel compartido para la pantalla de login.
 * @param registerViewModel ViewModel compartido para la pantalla de registro.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Loading,
    ) {
        // ── Pantalla de carga ──────────────────────────────────────────
        composable<Screen.Loading> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            LaunchedEffect(Unit) {
                if (tryAutoLogin()) {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Loading> { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Loading> { inclusive = true }
                    }
                }
            }
        }

        // ── Pantalla de inicio de sesión ───────────────────────────────
        composable<Screen.Login> {
            LoginAdministrador(
                viewModel = loginViewModel,
                onLoginExitoso = {
                    navController.navigate(Screen.Home) {
                        popUpTo<Screen.Login> { inclusive = true }
                    }
                },
                onCancelar = {
                    navController.navigate(Screen.Register)
                }
            )
        }

        // ── Pantalla de registro ───────────────────────────────────────
        composable<Screen.Register> {
            RegisterUsuario(
                viewModel = registerViewModel,
                onRegistroExitoso = {
                    loginViewModel.clear()
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Register> { inclusive = true }
                    }
                },
                onCancelar = {
                    navController.popBackStack()
                }
            )
        }

        // ── Pantalla principal ─────────────────────────────────────────
        composable<Screen.Home> {
            PantallaPrincipal(
                onCerrarSesion = {
                    TokenStorage.clear()
                    SessionManager.clear()
                    loginViewModel.clear()
                    navController.navigate(Screen.Login) {
                        popUpTo<Screen.Home> { inclusive = true }
                    }
                }
            )
        }
    }
}

/** Intenta restaurar la sesión desde los tokens almacenados. */
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
                if (response.access_token != null && response.refresh_token != null) {
                    TokenStorage.saveTokens(response.access_token, response.refresh_token, username)
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
