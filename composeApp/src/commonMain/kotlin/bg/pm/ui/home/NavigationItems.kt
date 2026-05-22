package bg.pm.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Seccion(val route: String, val title: String, val icon: ImageVector) {
    data object Inicio : Seccion("inicio", "Principal", Icons.Default.Home)
    data object Mensajes : Seccion("mensajes", "Chats", Icons.Default.Chat)
    data object Comunidad : Seccion("comunidad", "Foros", Icons.Default.Forum)
    data object MiPerfil : Seccion("perfil", "Perfil", Icons.Default.Person)
}

val listaSecciones = listOf(
    Seccion.Inicio,
    Seccion.Mensajes,
    Seccion.Comunidad
)