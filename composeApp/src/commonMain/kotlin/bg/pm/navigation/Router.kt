package bg.pm.navigation

import kotlinx.serialization.Serializable

/**
 * Define todas las rutas de navegación de la aplicación.
 * Cada objeto representa una pantalla accesible mediante NavController.
 */
sealed interface Screen {

    /** Pantalla de carga inicial (comprobación de sesión). */
    @Serializable
    data object Loading : Screen

    /** Pantalla de inicio de sesión. */
    @Serializable
    data object Login : Screen

    /** Pantalla de registro de usuario. */
    @Serializable
    data object Register : Screen

    /** Pantalla principal (home) tras autenticarse. */
    @Serializable
    data object Home : Screen
}
