package bg.pm.ui.wiki

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import bg.pm.network.ApiService
import bg.pm.network.GameOut
import bg.pm.network.SessionManager
import bg.pm.network.WikiIn
import bg.pm.network.WikiOut
import bg.pm.resolveAppImageUrl
import bg.pm.ui.common.LocalAppImageLoader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiScreen(
    juegos: List<GameOut>,
    isAdmin: Boolean,
) {
    var juegoWikiSeleccionado by remember { mutableStateOf<GameOut?>(null) }
    var busqueda by remember { mutableStateOf("") }
    var categoriaFiltro by remember { mutableStateOf<String?>(null) }
    var mostrarMenuFiltro by remember { mutableStateOf(false) }

    if (juegoWikiSeleccionado != null) {
        WikiGameScreen(
            juego = juegoWikiSeleccionado!!,
            isAdmin = isAdmin,
            onVolver = { juegoWikiSeleccionado = null }
        )
        return
    }

    val categorias = juegos.map { it.category }.distinct().sorted()

    val juegosFiltrados = juegos.filter { juego ->
        val matchBusqueda = busqueda.isBlank() || juego.name.contains(busqueda, ignoreCase = true)
        val matchCategoria = categoriaFiltro == null || juego.category == categoriaFiltro
        matchBusqueda && matchCategoria
    }

    Column(Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        1f to MaterialTheme.colorScheme.background
                    )
                )
                .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Wiki",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 38.sp
                    )
                    Text(
                        text = "Explora la guía de cada juego",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar juegos...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(
                    onClick = { mostrarMenuFiltro = true },
                    modifier = Modifier
                        .size(52.dp)
                        .background(
                            if (categoriaFiltro != null) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtrar por categoría",
                        tint = if (categoriaFiltro != null) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = mostrarMenuFiltro,
                    onDismissRequest = { mostrarMenuFiltro = false }
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Todas las categorías",
                                fontWeight = if (categoriaFiltro == null) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = { categoriaFiltro = null; mostrarMenuFiltro = false }
                    )
                    HorizontalDivider()
                    categorias.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    cat,
                                    fontWeight = if (categoriaFiltro == cat) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = { categoriaFiltro = cat; mostrarMenuFiltro = false }
                        )
                    }
                }
            }
        }

        if (categoriaFiltro != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { categoriaFiltro = null }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = categoriaFiltro!!,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "×",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (juegosFiltrados.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (busqueda.isBlank() && categoriaFiltro == null) "No hay juegos disponibles"
                    else "No se encontraron juegos",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(juegosFiltrados, key = { it.id_game }) { juego ->
                    WikiJuegoCard(
                        juego = juego,
                        onClick = { juegoWikiSeleccionado = juego }
                    )
                }
            }
        }
    }
}

@Composable
private fun WikiJuegoCard(juego: GameOut, onClick: () -> Unit) {
    val imageUrl = resolveAppImageUrl(juego.image)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    imageLoader = LocalAppImageLoader.current,
                    contentDescription = juego.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = juego.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = juego.gender,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = juego.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = "Ver wiki",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WikiGameScreen(
    juego: GameOut,
    isAdmin: Boolean,
    onVolver: () -> Unit,
) {
    val token = SessionManager.accessToken ?: ""
    var wikis by remember { mutableStateOf<List<WikiOut>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var wikiSearch by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(juego.id_game) {
        isLoading = true
        wikis = ApiService.obtenerWikiPorJuego(juego.id_game, token)
        isLoading = false
    }

    if (mostrarDialogoCrear) {
        CrearWikiDialog(
            idJuego = juego.id_game,
            onDismiss = { mostrarDialogoCrear = false },
            onCreate = { nueva ->
                wikis = wikis + nueva
                mostrarDialogoCrear = false
            }
        )
    }

    val wikisFiltradas = if (wikiSearch.isBlank()) wikis
    else wikis.filter {
        it.name.contains(wikiSearch, ignoreCase = true) ||
        it.category?.contains(wikiSearch, ignoreCase = true) == true ||
        it.description?.contains(wikiSearch, ignoreCase = true) == true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wiki — ${juego.name}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { mostrarDialogoCrear = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir entrada wiki")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = wikiSearch,
                onValueChange = { wikiSearch = it },
                placeholder = { Text("Buscar en esta wiki...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )

            when {
                isLoading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                wikisFiltradas.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (wikiSearch.isBlank()) "No hay entradas wiki para este juego"
                            else "Sin resultados para \"$wikiSearch\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isAdmin && wikiSearch.isBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Pulsa + para añadir la primera",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(wikisFiltradas, key = { it.id_wiki }) { wiki ->
                        WikiEntradaCard(wiki)
                    }
                }
            }
        }
    }
}

@Composable
private fun WikiEntradaCard(wiki: WikiOut) {
    var expandido by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expandido = !expandido },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = wiki.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (!wiki.category.isNullOrBlank()) {
                        Text(
                            text = wiki.category,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Text(
                    text = if (expandido) "▲" else "▼",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            if (expandido && !wiki.description.isNullOrBlank()) {
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(10.dp))
                Text(
                    text = wiki.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearWikiDialog(
    idJuego: Int,
    onDismiss: () -> Unit,
    onCreate: (WikiOut) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva entrada wiki") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isBlank()) { error = "El nombre es obligatorio"; return@TextButton }
                    scope.launch {
                        isLoading = true
                        try {
                            val token = SessionManager.accessToken ?: return@launch
                            val nueva = ApiService.crearWiki(
                                WikiIn(
                                    name = nombre.trim(),
                                    category = categoria.trim(),
                                    description = descripcion.trim(),
                                    id_forum = null
                                ),
                                token
                            )
                            onCreate(nueva)
                        } catch (e: Exception) {
                            error = "Error al crear: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(16.dp))
                else Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
