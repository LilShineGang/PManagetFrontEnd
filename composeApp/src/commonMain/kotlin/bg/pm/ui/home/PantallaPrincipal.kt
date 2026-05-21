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

    // ── Diálogo crear juego ───────────────────────────────────────────
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
                    } catch (e: Exception) { /* ignore */ }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    RuneBrand(compact = true)
                },
                actions = {
                    Text(
                        text = SessionManager.username ?: "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 8.dp).align(Alignment.CenterVertically)
                    )
                    TextButton(
                        onClick = { SessionManager.clear(); onCerrarSesion() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Salir", fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
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
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── CARRUSEL DE JUEGOS ──────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 20.dp, bottom = 8.dp, end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Juegos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (isAdmin) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { mostrarDialogoCrear = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 18.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                item {
                    if (juegos.isEmpty()) {
                        EmptyHint("No hay juegos disponibles")
                    } else {
                        val pagerState = rememberPagerState { juegos.size }
                        val scope = rememberCoroutineScope()
                        Column {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 48.dp)
                                ) { page ->
                                    GameCarouselCard(
                                        juego = juegos[page],
                                        onClick = { juegoSeleccionado = juegos[page] }
                                    )
                                }
                                // ← botón izquierdo
                                if (pagerState.currentPage > 0) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .padding(start = 6.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                            .clickable { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("‹", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                // → botón derecho
                                if (pagerState.currentPage < juegos.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(end = 6.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                            .clickable { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("›", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(juegos.size) { idx ->
                                    val selected = pagerState.currentPage == idx
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 3.dp)
                                            .size(if (selected) 9.dp else 6.dp)
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant,
                                                CircleShape
                                            )
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                // ── FOROS ───────────────────────────────────────────────
                item { SectionHeader("Foros") }
                if (foros.isEmpty()) {
                    item { EmptyHint("No hay foros disponibles") }
                } else {
                    items(foros) { foro -> ForoCard(foro) }
                }

                // ── CHATS ───────────────────────────────────────────────
                item { SectionHeader("Chats") }
                if (chats.isEmpty()) {
                    item { EmptyHint("No hay chats disponibles") }
                } else {
                    items(chats) { chat -> ChatCard(chat) }
                }
            }
        }
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
