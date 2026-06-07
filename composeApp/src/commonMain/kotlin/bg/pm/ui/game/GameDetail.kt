package bg.pm.ui.game

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import bg.pm.resolveAppImageUrl
import bg.pm.network.AchievementOut
import bg.pm.network.BuildOut
import bg.pm.network.GameOut
import bg.pm.network.SessionManager
import bg.pm.network.WikiOut
import bg.pm.ui.common.LocalAppImageLoader
import coil3.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetail(juego: GameOut, onVolver: () -> Unit) {
    val viewModel = remember { GameDetailViewModel() }
    val wikiEntries by viewModel.wikiEntries.collectAsState()
    val buildEntries by viewModel.buildEntries.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val myAchievementIds by viewModel.myAchievementIds.collectAsState()
    val forumId by viewModel.forumId.collectAsState()
    val dataLoading by viewModel.dataLoading.collectAsState()
    val actionLoading by viewModel.actionLoading.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val role by SessionManager.roleFlow.collectAsState()
    val scrollState = rememberScrollState()
    var wikiSearch by remember { mutableStateOf("") }
    var buildSearch by remember { mutableStateOf("") }
    var wikiExpanded by remember { mutableStateOf(true) }
    var buildsExpanded by remember { mutableStateOf(true) }
    var logrosExpanded by remember { mutableStateOf(true) }
    var showCreateAchievementDialog by remember { mutableStateOf(false) }
    var showCreateWikiDialog by remember { mutableStateOf(false) }
    var showCreateBuildDialog by remember { mutableStateOf(false) }
    val isLoggedIn = SessionManager.isLoggedIn()
    val isAdmin = SessionManager.isAdminRole(role)
    val canCreateAdminEntries = isAdmin && forumId != null && !actionLoading
    val canCreateBuilds = isLoggedIn && forumId != null && !actionLoading
    val plannerName = SessionManager.username?.trim().orEmpty()
    val gameImageUrl = resolveAppImageUrl(juego.image)

    LaunchedEffect(juego.id_game) {
        viewModel.cargarDatos(juego.id_game)
    }

    if (showCreateAchievementDialog) {
        CrearLogroDialog(
            loading = actionLoading,
            error = actionError,
            onDismiss = {
                viewModel.clearActionError()
                showCreateAchievementDialog = false
            },
            onCreate = { difficulty, description ->
                viewModel.crearLogro(difficulty, description, juego.id_game) {
                    showCreateAchievementDialog = false
                }
            }
        )
    }

    if (showCreateWikiDialog) {
        CrearWikiDialog(
            loading = actionLoading,
            error = actionError,
            onDismiss = {
                viewModel.clearActionError()
                showCreateWikiDialog = false
            },
            onCreate = { name, category, description ->
                viewModel.crearWiki(name, category, description) {
                    showCreateWikiDialog = false
                }
            }
        )
    }

    if (showCreateBuildDialog) {
        CrearBuildDialog(
            defaultPlanner = plannerName,
            loading = actionLoading,
            error = actionError,
            onDismiss = {
                viewModel.clearActionError()
                showCreateBuildDialog = false
            },
            onCreate = { name, planner, category, description ->
                viewModel.crearBuild(name, planner, category, description) {
                    showCreateBuildDialog = false
                }
            }
        )
    }

    val filteredWiki = wikiEntries.filter {
        wikiSearch.isBlank() ||
        it.name.contains(wikiSearch, ignoreCase = true) ||
        it.category?.contains(wikiSearch, ignoreCase = true) == true ||
        it.description?.contains(wikiSearch, ignoreCase = true) == true
    }

    val filteredBuilds = buildEntries.filter {
        buildSearch.isBlank() ||
        it.name.contains(buildSearch, ignoreCase = true) ||
        it.category.contains(buildSearch, ignoreCase = true) ||
        it.description.contains(buildSearch, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (gameImageUrl != null) {
                    AsyncImage(
                        model = gameImageUrl,
                        imageLoader = LocalAppImageLoader.current,
                        contentDescription = juego.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = juego.name.first().uppercaseChar().toString(),
                        fontSize = 120.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.15f),
                                0.55f to Color.Black.copy(alpha = 0.30f),
                                1f to MaterialTheme.colorScheme.background
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 28.dp, end = 80.dp)
                ) {
                    Text(
                        text = juego.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = Color.White,
                        lineHeight = 32.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ChipBadge(
                            label = juego.category,
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                        ChipBadge(
                            label = juego.gender,
                            containerColor = Color.White.copy(alpha = 0.12f),
                            contentColor = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-20).dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    juego.rating?.let { rating ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            repeat(5) { i ->
                                val filled = i < rating.toInt()
                                Text(
                                    text = if (filled) "★" else "☆",
                                    fontSize = 22.sp,
                                    color = if (filled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "$rating / 5",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    InfoRow(label = "Categoría", value = juego.category)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 10.dp))
                    InfoRow(label = "Género", value = juego.gender)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 10.dp))
                    InfoRow(label = "Dificultad", value = juego.difficulty, valueColor = MaterialTheme.colorScheme.primary)
                }
            }

            actionError?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(8.dp))

            CollapsibleSectionHeader(
                title = "Logros",
                count = achievements.size,
                expanded = logrosExpanded,
                onToggle = { logrosExpanded = !logrosExpanded },
                onAdd = if (isAdmin) ({
                    viewModel.clearActionError()
                    showCreateAchievementDialog = true
                }) else null,
                addEnabled = isAdmin && !actionLoading
            )

            AnimatedVisibility(
                visible = logrosExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    if (dataLoading) {
                        Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                    } else if (achievements.isEmpty()) {
                        Text(
                            text = "No hay logros para este juego.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 20.dp, bottom = 12.dp, top = 4.dp)
                        )
                    } else {
                        val grouped = achievements.groupBy { it.difficulty }
                        val order = listOf("Bronce", "Plata", "Oro")
                        (order + grouped.keys.filterNot { it in order }).forEach { diff ->
                            val entries = grouped[diff] ?: return@forEach
                            Text(
                                text = diff.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (diff) {
                                    "Oro" -> Color(0xFFFFD700)
                                    "Plata" -> Color(0xFFC0C0C0)
                                    else -> Color(0xFFCD7F32)
                                },
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 4.dp)
                            )
                            entries.forEach { logro ->
                                AchievementCard(
                                    achievement = logro,
                                    isEarned = logro.id_achievement in myAchievementIds,
                                    onToggle = { viewModel.toggleLogro(logro.id_achievement) }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            CollapsibleSectionHeader(
                title = "Wiki",
                count = filteredWiki.size,
                expanded = wikiExpanded,
                onToggle = { wikiExpanded = !wikiExpanded },
                onAdd = if (isAdmin) ({
                    viewModel.clearActionError()
                    showCreateWikiDialog = true
                }) else null,
                addEnabled = canCreateAdminEntries
            )

            AnimatedVisibility(
                visible = wikiExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    if (isAdmin && forumId == null) {
                        Text(
                            text = "No hay foro asociado a este juego; el administrador no puede crear entradas de wiki todavía.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    OutlinedTextField(
                        value = wikiSearch,
                        onValueChange = { wikiSearch = it },
                        placeholder = { Text("Buscar en la wiki...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )

                    if (dataLoading) {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    } else if (filteredWiki.isEmpty()) {
                        Text(
                            text = if (wikiSearch.isBlank()) "No hay entradas de wiki para este juego." else "Sin resultados para \"$wikiSearch\".",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 20.dp, bottom = 16.dp, top = 4.dp)
                        )
                    } else {
                        val grouped = filteredWiki.groupBy { it.category ?: "General" }
                        grouped.forEach { (category, entries) ->
                            Text(
                                text = category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 4.dp)
                            )
                            entries.forEach { wiki -> WikiEntryCard(wiki) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            CollapsibleSectionHeader(
                title = "Builds",
                count = filteredBuilds.size,
                expanded = buildsExpanded,
                onToggle = { buildsExpanded = !buildsExpanded },
                onAdd = if (isLoggedIn) ({
                    viewModel.clearActionError()
                    showCreateBuildDialog = true
                }) else null,
                addEnabled = canCreateBuilds && plannerName.isNotBlank()
            )

            AnimatedVisibility(
                visible = buildsExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    if (isLoggedIn && forumId == null) {
                        Text(
                            text = "No hay foro asociado a este juego; todavía no se pueden crear builds.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                    OutlinedTextField(
                        value = buildSearch,
                        onValueChange = { buildSearch = it },
                        placeholder = { Text("Buscar builds...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )

                    if (dataLoading) {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        }
                    } else if (filteredBuilds.isEmpty()) {
                        Text(
                            text = if (buildSearch.isBlank()) "No hay builds para este juego." else "Sin resultados para \"$buildSearch\".",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 20.dp, bottom = 16.dp, top = 4.dp)
                        )
                    } else {
                        val grouped = filteredBuilds.groupBy { it.category }
                        grouped.forEach { (category, entries) ->
                            Text(
                                text = category.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.5.sp,
                                modifier = Modifier.padding(start = 20.dp, top = 10.dp, bottom = 4.dp)
                            )
                            entries.forEach { build -> BuildCard(build) }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
                .zIndex(10f),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "←",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CollapsibleSectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    onAdd: (() -> Unit)? = null,
    addEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onAdd != null) {
                Box(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (addEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        )
                        .clickable(enabled = addEnabled, onClick = onAdd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Text(
                text = if (expanded) "▲" else "▼",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrearLogroDialog(
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    val difficultyOptions = listOf("Bronce", "Plata", "Oro")
    var difficulty by remember { mutableStateOf(difficultyOptions.first()) }
    var difficultyExpanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo logro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(
                    expanded = difficultyExpanded,
                    onExpandedChange = { difficultyExpanded = !difficultyExpanded }
                ) {
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dificultad *") },
                        singleLine = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = difficultyExpanded,
                        onDismissRequest = { difficultyExpanded = false }
                    ) {
                        difficultyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    difficulty = option
                                    difficultyExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !loading && difficulty.isNotBlank() && description.isNotBlank(),
                onClick = {
                    onCreate(difficulty.trim(), description.trim())
                }
            ) {
                Text(if (loading) "Creando..." else "Crear")
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CrearWikiDialog(
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva entrada de wiki", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !loading && nombre.isNotBlank() && categoria.isNotBlank() && descripcion.isNotBlank(),
                onClick = {
                    onCreate(nombre.trim(), categoria.trim(), descripcion.trim())
                }
            ) {
                Text(if (loading) "Creando..." else "Crear")
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CrearBuildDialog(
    defaultPlanner: String,
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var planner by remember(defaultPlanner) { mutableStateOf(defaultPlanner) }
    var categoria by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva build", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = planner,
                    onValueChange = { planner = it },
                    label = { Text("Planner *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !loading && nombre.isNotBlank() && categoria.isNotBlank() && descripcion.isNotBlank() && planner.isNotBlank(),
                onClick = {
                    onCreate(nombre.trim(), planner.trim(), categoria.trim(), descripcion.trim())
                }
            ) {
                Text(if (loading) "Creando..." else "Crear")
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun BuildCard(build: BuildOut) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = build.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = build.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "por ${build.planner}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = build.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun AchievementCard(achievement: AchievementOut, isEarned: Boolean, onToggle: () -> Unit) {
    val medalColor = when (achievement.difficulty) {
        "Oro"   -> Color(0xFFFFD700)
        "Plata" -> Color(0xFFC0C0C0)
        else    -> Color(0xFFCD7F32)
    }
    val medalSymbol = when (achievement.difficulty) {
        "Oro"   -> "★"
        "Plata" -> "◆"
        else    -> "●"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(medalColor.copy(alpha = 0.15f), CircleShape)
                    .padding(end = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = medalSymbol,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = medalColor
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.difficulty,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = medalColor,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = achievement.description,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Text(
                    text = if (isEarned) "✓" else "○",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isEarned) medalColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WikiEntryCard(wiki: WikiOut) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                text = wiki.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            wiki.description?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = it,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ChipBadge(label: String, containerColor: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Normal)
        Text(text = value, fontSize = 14.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}
