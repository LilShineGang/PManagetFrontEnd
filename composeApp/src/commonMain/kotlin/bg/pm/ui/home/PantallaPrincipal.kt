package bg.pm.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import bg.pm.network.ApiService
import bg.pm.network.ChatOut
import bg.pm.network.ForumOut
import bg.pm.network.GameIn
import bg.pm.network.GameOut
import bg.pm.network.SessionManager
import bg.pm.pickImageFile
import bg.pm.ui.common.LocalAppImageLoader
import bg.pm.ui.game.GameDetailScreen
import androidx.compose.runtime.*
import bg.pm.ui.theme.PManagerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaPrincipal(onCerrarSesion: () -> Unit) {
    var seccionActual by remember { mutableStateOf<Seccion>(Seccion.Inicio) }
    var juegoSeleccionado by remember { mutableStateOf<bg.pm.network.GameOut?>(null) }

    if (juegoSeleccionado != null) {
        GameDetailScreen(
            juego = juegoSeleccionado!!,
            onVolver = { juegoSeleccionado = null }
        )
        return
    }

    val viewModel = remember { PantallaPrincipalViewModel() }
    val juegos by viewModel.juegos.collectAsState()
    val foros by viewModel.foros.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    BoxWithConstraints {
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
                topBar = {
                    TopAppBar(
                        title = { Text("Glyph", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary) },
                        actions = {
                            Text(text = SessionManager.username ?: "", modifier = Modifier.padding(end = 8.dp))
                            TextButton(onClick = { SessionManager.clear(); onCerrarSesion() }) {
                                Text("Salir", fontWeight = FontWeight.SemiBold)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                }
            ) { paddingValues ->
                Box(Modifier.fillMaxSize().padding(paddingValues)) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    } else if (error != null) {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(error!!, color = MaterialTheme.colorScheme.error)
                            Button(onClick = { viewModel.cargarDatos() }) { Text("Reintentar") }
                        }
                    } else {
                        when (seccionActual) {
                            Seccion.Inicio -> ContenidoInicio(
                                juegos = juegos,
                                foros = foros,
                                chats = chats,
                                isAdmin = isAdmin,
                                onCrearClick = { mostrarDialogoCrear = true },
                                onJuegoClick = { juegoSeleccionado = it }
                            )
                            Seccion.Mensajes -> PlaceholderPantalla("Pantalla de Chats")
                            Seccion.Comunidad -> PlaceholderPantalla("Pantalla de Foros")
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        CrearJuegoDialog(
            onDismiss = { mostrarDialogoCrear = false },
            onCreate = { game ->
                scope.launch {
                    try {
                        val token = SessionManager.accessToken ?: return@launch
                        ApiService.crearJuego(game, token)
                        mostrarDialogoCrear = false
                        viewModel.cargarDatos()
                    } catch (e: Exception) { }
                }
            }
        )
    }
}

@Composable
fun ContenidoInicio(
    juegos: List<bg.pm.network.GameOut>,
    foros: List<bg.pm.network.ForumOut>,
    chats: List<bg.pm.network.ChatOut>,
    isAdmin: Boolean,
    onCrearClick: () -> Unit,
    onJuegoClick: (bg.pm.network.GameOut) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Juegos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (isAdmin) {
                    Box(
                        modifier = Modifier.size(30.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { onCrearClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            if (juegos.isEmpty()) {
                EmptyHint("No hay juegos disponibles")
            } else {
                val pagerState = rememberPagerState { juegos.size }
                HorizontalPager(state = pagerState, contentPadding = PaddingValues(horizontal = 48.dp)) { page ->
                    GameCarouselCard(juego = juegos[page], onClick = { onJuegoClick(juegos[page]) })
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
private fun GameCarouselCard(juego: GameOut, onClick: () -> Unit = {}) {
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
            if (juego.image != null) {
                AsyncImage(
                    model = juego.image,
                    imageLoader = LocalAppImageLoader.current,
                    contentDescription = juego.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { println("Coil error loading '${juego.image}': ${it.result.throwable}") }
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
            val textColor = if (juego.image != null) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
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
private fun CrearJuegoDialog(onDismiss: () -> Unit, onCreate: (GameIn) -> Unit) {
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
                            image = imagen.trim().ifBlank { null },
                            rating = rating.trim().toDoubleOrNull()
                        )
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
