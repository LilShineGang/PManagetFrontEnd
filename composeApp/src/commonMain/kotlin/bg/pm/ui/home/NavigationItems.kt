package bg.pm.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Seccion(val route: String, val title: String, val icon: ImageVector) {
    data object Inicio : Seccion("inicio", "Principal", Icons.Default.Home)
    data object Juegos : Seccion("juegos", "Juegos", Icons.Default.SportsEsports)
    data object Wiki : Seccion("wiki", "Wiki", Icons.Default.MenuBook)
    data object Mensajes : Seccion("mensajes", "Chats", Icons.Default.Chat)
    data object Comunidad : Seccion("comunidad", "Foros", Icons.Default.Forum)
    data object MiPerfil : Seccion("perfil", "Perfil", Icons.Default.Person)
}

val listaSecciones = listOf(
    Seccion.Inicio,
    Seccion.Juegos,
    Seccion.Wiki,
    Seccion.Mensajes,
    Seccion.Comunidad
)