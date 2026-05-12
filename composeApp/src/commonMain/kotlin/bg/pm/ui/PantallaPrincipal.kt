package bg.pm.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.ApiService
import bg.pm.network.ChatOut
import bg.pm.network.ForumOut
import bg.pm.network.GameOut
import bg.pm.network.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PantallaPrincipalViewModel : ViewModel() {
    private val _juegos = MutableStateFlow<List<GameOut>>(emptyList())
    val juegos: StateFlow<List<GameOut>> = _juegos.asStateFlow()

    private val _foros = MutableStateFlow<List<ForumOut>>(emptyList())
    val foros: StateFlow<List<ForumOut>> = _foros.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatOut>>(emptyList())
    val chats: StateFlow<List<ChatOut>> = _chats.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun cargarDatos() {
        val token = SessionManager.accessToken ?: return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _juegos.value = ApiService.obtenerJuegos(token)
                _foros.value = ApiService.obtenerForos(token)
                _chats.value = ApiService.obtenerChats(token)
            } catch (e: Exception) {
                _error.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PantallaPrincipal(onCerrarSesion: () -> Unit) {
    val viewModel = remember { PantallaPrincipalViewModel() }
    val juegos by viewModel.juegos.collectAsState()
    val foros by viewModel.foros.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarDatos() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Glyph",
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 22.sp
                    )
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
                    SectionHeader("Juegos")
                }
                item {
                    if (juegos.isEmpty()) {
                        EmptyHint("No hay juegos disponibles")
                    } else {
                        val pagerState = rememberPagerState { juegos.size }
                        Column {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentPadding = PaddingValues(horizontal = 32.dp)
                            ) { page ->
                                GameCarouselCard(juegos[page])
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
private fun GameCarouselCard(juego: GameOut) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
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

