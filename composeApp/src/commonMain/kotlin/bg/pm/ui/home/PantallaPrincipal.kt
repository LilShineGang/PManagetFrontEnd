package bg.pm.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import bg.pm.network.ApiService
import bg.pm.network.ChatOut
import bg.pm.network.ForumOut
import bg.pm.network.GameIn
import bg.pm.network.GameOut
import bg.pm.network.UserOut
import bg.pm.readPickedImageUpload
import bg.pm.resolveAppImageUrl
import bg.pm.network.SessionManager
import bg.pm.pickImageFile
import bg.pm.ui.common.LocalAppImageLoader
import bg.pm.ui.common.RuneBrand
import bg.pm.ui.game.GameDetailScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaPrincipal(onCerrarSesion: () -> Unit) {
    var seccionActual by remember { mutableStateOf<Seccion>(Seccion.Inicio) }
    var juegoSeleccionado by remember { mutableStateOf<bg.pm.network.GameOut?>(null) }
    var mostrarPerfil by remember { mutableStateOf(false) }

    if (juegoSeleccionado != null) {
        GameDetailScreen(
            juego = juegoSeleccionado!!,
            onVolver = { juegoSeleccionado = null }
        )
        return
    }

    val viewModel = remember { PantallaPrincipalViewModel() }
    val perfil by viewModel.perfil.collectAsState()
    val juegos by viewModel.juegos.collectAsState()
    val foros by viewModel.foros.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val likedGameIds by viewModel.likedGameIds.collectAsState()
    val juegosLiked by viewModel.juegosLiked.collectAsState()
    
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var menuPerfilExpandido by remember { mutableStateOf(false) }
    var juegoAEliminar by remember { mutableStateOf<GameOut?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    if (mostrarPerfil) {
        PerfilScreen(
            perfil = perfil,
            onVolver = { mostrarPerfil = false },
            onPerfilActualizado = { viewModel.actualizarPerfilLocal(it) },
            onCerrarSesion = {
                SessionManager.clear()
                onCerrarSesion()
            },
            onCuentaEliminada = {
                onCerrarSesion()
            }
        )
        return
    }

    // ── Diálogo crear juego ───────────────────────────────────────────
    juegoAEliminar?.let { juego ->
        AlertDialog(
            onDismissRequest = { juegoAEliminar = null },
            title = { Text("Eliminar juego") },
            text = { Text("¿Seguro que quieres eliminar \"${juego.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarJuego(juego.id_game)
                        juegoAEliminar = null
                    }
                ) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { juegoAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    if (mostrarDialogoCrear) {
        CrearJuegoDialog(
            onDismiss = { mostrarDialogoCrear = false },
            onCreate = { game, imagePath ->
                scope.launch {
                    try {
                        val token = SessionManager.accessToken ?: return@launch
                        val createdGame = ApiService.crearJuego(game.copy(image = null), token)
                        val imageUpload = imagePath?.takeIf { it.isNotBlank() }?.let { readPickedImageUpload(it) }
                        if (imageUpload != null) {
                            ApiService.subirImagenJuego(createdGame.id_game, imageUpload, token)
                        }
                        mostrarDialogoCrear = false
                        viewModel.cargarDatos()
                    } catch (e: Exception) { }
                }
            }
        )
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val esEscritorio = maxWidth > 600.dp
        Row(Modifier.fillMaxSize()) {
            if (esEscritorio) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Spacer(Modifier.weight(1f))
                    listaSecciones.forEach { seccion ->
                        NavigationRailItem(
                            selected = seccionActual == seccion,
                            onClick = { seccionActual = seccion },
                            icon = { Icon(seccion.icon, contentDescription = seccion.title) },
                            label = { Text(seccion.title) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                }
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    TopAppBar(
                        title = {
                            RuneBrand(compact = true)
                        },
                        actions = {
                            Box {
                                UserAvatar(
                                    perfil = perfil,
                                    onClick = { menuPerfilExpandido = true },
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                DropdownMenu(
                                    expanded = menuPerfilExpandido,
                                    onDismissRequest = { menuPerfilExpandido = false },
                                    offset = DpOffset(x = (-8).dp, y = 4.dp)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Mi perfil") },
                                        onClick = {
                                            menuPerfilExpandido = false
                                            mostrarPerfil = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cerrar sesión") },
                                        onClick = {
                                            menuPerfilExpandido = false
                                            SessionManager.clear()
                                            onCerrarSesion()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                bottomBar = {
                    if (!esEscritorio) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            listaSecciones.forEach { seccion ->
                                NavigationBarItem(
                                    selected = seccionActual == seccion,
                                    onClick = { seccionActual = seccion },
                                    icon = { Icon(seccion.icon, contentDescription = seccion.title) },
                                    label = { Text(seccion.title) }
                                )
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { paddingValues ->
                if (isLoading) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (error != null) {
                    Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(bottom = 16.dp))
                            Button(onClick = { viewModel.cargarDatos() }) { Text("Reintentar") }
                        }
                    }
                } else {
                    when (seccionActual) {
                        Seccion.Inicio -> LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item { SectionHeader("Mis favoritos") }
                            if (juegosLiked.isEmpty()) {
                                item { EmptyHint("Aún no tienes favoritos. Dále al ♥ en un juego!") }
                            } else {
                                item {
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        items(juegosLiked, key = { it.id_game }) { juego ->
                                            Box(Modifier.width(260.dp).height(160.dp)) {
                                                GameCarouselCard(
                                                    juego = juego,
                                                    onClick = { juegoSeleccionado = juego },
                                                    isLiked = true,
                                                    onToggleLike = { viewModel.toggleLike(juego) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            item { SectionHeader("Foros") }
                            if (foros.isEmpty()) { item { EmptyHint("No hay foros disponibles") } }
                            else { items(foros) { ForoCard(it) } }

                            item { SectionHeader("Chats") }
                            if (chats.isEmpty()) { item { EmptyHint("No hay chats disponibles") } }
                            else { items(chats) { ChatCard(it) } }
                        }
                        Seccion.Juegos -> LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(paddingValues),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Juegos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    if (isAdmin) {
                                        Spacer(Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier.size(30.dp).clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .clickable { mostrarDialogoCrear = true },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("+", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            if (juegos.isEmpty()) {
                                item { EmptyHint("No hay juegos disponibles") }
                            } else {
                                items(juegos, key = { it.id_game }) { juego ->
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                    ) {
                                        GameCarouselCard(
                                            juego = juego,
                                            onClick = { juegoSeleccionado = juego },
                                            isLiked = juego.id_game in likedGameIds,
                                            onToggleLike = { viewModel.toggleLike(juego) },
                                            onDelete = if (isAdmin) { { juegoAEliminar = juego } } else null
                                        )
                                    }
                                }
                            }
                        }
                        Seccion.Mensajes -> PlaceholderPantalla("Chats")
                        Seccion.Comunidad -> PlaceholderPantalla("Foros")
                        else -> PlaceholderPantalla("")
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderPantalla(texto: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(texto, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun EmptyHint(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun UserAvatar(
    perfil: UserOut?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageUrl = resolveAppImageUrl(perfil?.image)

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                imageLoader = LocalAppImageLoader.current,
                contentDescription = perfil?.username ?: "Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = (perfil?.username ?: SessionManager.username ?: "?").take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

private fun parseHexColor(hex: String): Color? {
    return try {
        val h = hex.trimStart('#')
        if (h.length == 6) {
            val r = h.substring(0, 2).toInt(16)
            val g = h.substring(2, 4).toInt(16)
            val b = h.substring(4, 6).toInt(16)
            Color(red = r / 255f, green = g / 255f, blue = b / 255f)
        } else null
    } catch (_: Exception) { null }
}

private fun Color.toHex(): String {
    val r = (red * 255 + 0.5f).toInt().coerceIn(0, 255)
    val g = (green * 255 + 0.5f).toInt().coerceIn(0, 255)
    val b = (blue * 255 + 0.5f).toInt().coerceIn(0, 255)
    return "#${r.toString(16).padStart(2, '0').uppercase()}${g.toString(16).padStart(2, '0').uppercase()}${b.toString(16).padStart(2, '0').uppercase()}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PerfilScreen(
    perfil: UserOut?,
    onVolver: () -> Unit,
    onPerfilActualizado: (UserOut) -> Unit = {},
    onCerrarSesion: () -> Unit,
    onCuentaEliminada: () -> Unit,
) {
    var perfilActual by remember(perfil) { mutableStateOf(perfil) }
    var showEdit by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }
    val imageUrl = resolveAppImageUrl(perfilActual?.image)
    val scope = rememberCoroutineScope()
    var bannerImagePath by remember { mutableStateOf<String?>(null) }
    var bannerColor by remember { mutableStateOf<Color?>(null) }

    // Restore banner state from DB value on initial load
    LaunchedEffect(perfilActual?.banner) {
        val b = perfilActual?.banner ?: return@LaunchedEffect
        if (b.startsWith("#")) {
            bannerColor = parseHexColor(b)
            bannerImagePath = null
        } else {
            bannerImagePath = resolveAppImageUrl(b)
            bannerColor = null
        }
    }

    // ── Diálogo editar ────────────────────────────────────────────────
    if (showEdit) {
        EditPerfilDialog(
            perfil = perfilActual,
            onDismiss = { showEdit = false },
            onBannerChange = { path, color ->
                bannerImagePath = path
                bannerColor = color
            },
            onSave = { name, email, imagePath, onError ->
                scope.launch {
                    val token = SessionManager.accessToken
                    if (token == null) {
                        onError("No hay sesión activa. Vuelve a iniciar sesión.")
                        return@launch
                    }
                    try {
                        val imageUpload = imagePath?.takeIf { it.isNotBlank() }
                            ?.let { readPickedImageUpload(it) }
                        if (imageUpload != null) {
                            perfilActual = ApiService.subirImagenPerfil(imageUpload, token)
                        }
                        // Upload banner image if it's a local file path (not a server URL)
                        val localBannerPath = bannerImagePath?.takeIf { it.isNotBlank() && !it.startsWith("http") }
                        if (localBannerPath != null) {
                            val bannerUpload = readPickedImageUpload(localBannerPath)
                            if (bannerUpload != null) {
                                perfilActual = ApiService.subirBannerPerfil(bannerUpload, token)
                            }
                        }
                        val trimmedName = name.trim().takeIf { it.isNotBlank() }
                        val trimmedEmail = email.trim().takeIf { it.isNotBlank() }
                        // Include banner color hex if a color is selected (no image upload)
                        val bannerHex = if (bannerColor != null && localBannerPath == null) bannerColor!!.toHex() else null
                        if (trimmedName != null || trimmedEmail != null || bannerHex != null) {
                            perfilActual = ApiService.actualizarPerfil(
                                bg.pm.network.UserUpdate(name = trimmedName, email = trimmedEmail, banner = bannerHex),
                                token
                            )
                        }
                        perfilActual?.let { onPerfilActualizado(it) }
                        showEdit = false
                    } catch (e: Exception) {
                        onError(e.message ?: "Error desconocido al guardar")
                    }
                }
            }
        )
    }

    // ── Diálogo confirmar borrado ─────────────────────────────────────
    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold) },
            text = {
                Text("¿Seguro que quieres eliminar tu cuenta? Esta acción es irreversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val token = SessionManager.accessToken ?: return@launch
                            val username = SessionManager.username ?: return@launch
                            try {
                                ApiService.eliminarCuenta(username, token)
                                SessionManager.clear()
                                onCuentaEliminada()
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onVolver) {
                        Text("←", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Banner + avatar superpuesto ───────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                // Banner (imagen, color sólido o degradado por defecto)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    if (bannerImagePath != null) {
                        AsyncImage(
                            model = bannerImagePath,
                            imageLoader = LocalAppImageLoader.current,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (bannerColor != null)
                                        Brush.linearGradient(listOf(bannerColor!!, bannerColor!!.copy(alpha = 0.6f)))
                                    else
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                )
                        )
                    }
                    // Settings – editar perfil (sombra)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp, end = 8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable { showEdit = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                // Avatar superpuesto
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomCenter)
                        .clip(CircleShape)
                        .border(4.dp, MaterialTheme.colorScheme.background, CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            imageLoader = LocalAppImageLoader.current,
                            contentDescription = perfilActual?.username ?: "Perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = (perfilActual?.username ?: SessionManager.username ?: "?").take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 36.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ProfileInfoRow("Nombre", perfilActual?.name ?: "-")
                    ProfileInfoRow("Usuario", perfilActual?.username ?: SessionManager.username ?: "-")
                    ProfileInfoRow("Email", perfilActual?.email ?: "-")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Rol",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RoleBadge(perfilActual?.role ?: SessionManager.role ?: "user")
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Botón cerrar sesión ───────────────────────────────
                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, MaterialTheme.colorScheme.outline
                    )
                ) {
                    Text("Cerrar sesión")
                }

                // ── Zona de peligro ──────────────────────────────────
                Button(
                    onClick = { showConfirmDelete = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Eliminar cuenta")
                }
            }
        }
    }
}

@Composable
private fun EditPerfilDialog(
    perfil: UserOut?,
    onDismiss: () -> Unit,
    onBannerChange: (imagePath: String?, color: Color?) -> Unit,
    onSave: (name: String, email: String, imagePath: String?, onError: (String) -> Unit) -> Unit,
) {
    var name by remember { mutableStateOf(perfil?.name ?: "") }
    var email by remember { mutableStateOf(perfil?.email ?: "") }
    var imagePath by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val presetColors = listOf(
        Color(0xFF7B2FBE) to Color(0xFF4C1D95),
        Color(0xFF1D4ED8) to Color(0xFF0EA5E9),
        Color(0xFF065F46) to Color(0xFF10B981),
        Color(0xFFB45309) to Color(0xFFF59E0B),
        Color(0xFF9D174D) to Color(0xFFEC4899),
        Color(0xFF111827) to Color(0xFF374151),
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustes de perfil", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = {
                        pickImageFile { path -> if (path != null) imagePath = path }
                    }) {
                        Text("Cambiar foto")
                    }
                    if (imagePath != null) {
                        Text(
                            text = imagePath!!.substringAfterLast('/').substringAfterLast('\\'),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (saveError != null) {
                    Text(
                        text = saveError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Text(
                    "Fondo del perfil",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presetColors.forEach { (c1, c2) ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(c1, c2)))
                                .clickable { onBannerChange(null, c1) }
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        pickImageFile { path ->
                            if (path != null) onBannerChange(path, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Imagen de fondo") }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    saving = true
                    saveError = null
                    onSave(name, email, imagePath) { errorMsg ->
                        saving = false
                        saveError = errorMsg
                    }
                }
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun RoleBadge(role: String) {
    val isAdmin = role.trim().lowercase() == "admin"
    val bgColor = if (isAdmin) Color(0xFFFFD700) else Color(0xFFB0BEC5)
    val textColor = if (isAdmin) Color(0xFF4A3000) else Color(0xFF37474F)
    val label = if (isAdmin) "Admin" else "Usuario"
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GameCarouselCard(
    juego: GameOut,
    onClick: () -> Unit = {},
    isLiked: Boolean = false,
    onToggleLike: () -> Unit = {},
    onDelete: (() -> Unit)? = null
) {
    val imageUrl = resolveAppImageUrl(juego.image)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    imageLoader = LocalAppImageLoader.current,
                    contentDescription = juego.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { println("Coil error loading '$imageUrl': ${it.result.throwable}") }
                )
                // overlay oscuro para legibilidad del texto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.40f))
                )
            } else {
                // placeholder letra grande
                Text(
                    text = juego.name.first().uppercaseChar().toString(),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                    modifier = Modifier.align(Alignment.CenterEnd).padding(20.dp)
                )
            }
            IconButton(
                onClick = onToggleLike,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.Black.copy(alpha = 0.40f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isLiked) "Quitar favorito" else "Añadir favorito",
                        tint = if (isLiked) Color(0xFFFF4444) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(alpha = 0.40f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar juego",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            val textColor = if (imageUrl != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(
                    text = juego.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = textColor
                )
                Text(
                    text = "${juego.category} · ${juego.gender}",
                    fontSize = 13.sp,
                    color = textColor.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = juego.difficulty,
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                    juego.rating?.let {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "★ $it",
                            fontSize = 13.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ForoCard(foro: ForumOut) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = foro.name.first().uppercaseChar().toString(),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = foro.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                foro.id_game?.let {
                    Text(
                        text = "Juego #$it",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatCard(chat: ChatOut) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#",
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.content,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                chat.timestamp?.let {
                    Text(
                        text = it,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


@Composable
private fun CrearJuegoDialog(onDismiss: () -> Unit, onCreate: (GameIn, String?) -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var dificultad by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var imagen by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo juego", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre, onValueChange = { nombre = it },
                    label = { Text("Nombre *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = genero, onValueChange = { genero = it },
                    label = { Text("Género *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dificultad, onValueChange = { dificultad = it },
                    label = { Text("Dificultad *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria, onValueChange = { categoria = it },
                    label = { Text("Categoría *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Selector de imagen
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Imagen (opcional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = {
                            pickImageFile { path -> if (path != null) imagen = path }
                        }) {
                            Text("Seleccionar archivo")
                        }
                        if (imagen.isNotBlank()) {
                            Text(
                                text = imagen.substringAfterLast('/').substringAfterLast('\\'),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = rating, onValueChange = { rating = it },
                    label = { Text("Rating (0-5, opcional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isBlank() || genero.isBlank() || dificultad.isBlank() || categoria.isBlank()) return@Button
                    onCreate(
                        GameIn(
                            name = nombre.trim(),
                            gender = genero.trim(),
                            difficulty = dificultad.trim(),
                            category = categoria.trim(),
                            image = null,
                            rating = rating.trim().toDoubleOrNull()
                        ),
                        imagen.trim().ifBlank { null }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
