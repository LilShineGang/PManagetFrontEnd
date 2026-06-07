package bg.pm.ui.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bg.pm.network.DiscussionOut
import bg.pm.network.ForumOut
import bg.pm.network.GameOut
import bg.pm.network.PostReplyOut
import bg.pm.network.SessionManager
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.text.font.FontStyle
import bg.pm.pickImageFile
import bg.pm.resolveAppImageUrl
import bg.pm.ui.common.LocalAppImageLoader
import coil3.compose.AsyncImage
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private sealed interface ForumNavState {
    data object ForumList : ForumNavState
    data class DiscussionList(val forum: ForumOut) : ForumNavState
    data class DiscussionDetail(val discussion: DiscussionOut, val forum: ForumOut) : ForumNavState
}

@Composable
fun ForumScreen(
    forums: List<ForumOut>,
    juegos: List<GameOut>,
    isAdmin: Boolean,
    initialForum: ForumOut? = null,
    onForoCreado: ((ForumOut) -> Unit)? = null,
) {
    var navState by remember {
        mutableStateOf<ForumNavState>(
            if (initialForum != null) ForumNavState.DiscussionList(initialForum)
            else ForumNavState.ForumList
        )
    }
    val viewModel = remember { ForumViewModel() }

    LaunchedEffect(initialForum) {
        if (initialForum != null) viewModel.cargarDiscusiones(initialForum.id_forum)
    }

    when (val state = navState) {
        ForumNavState.ForumList -> ForumListContent(
            forums = forums,
            juegos = juegos,
            isAdmin = isAdmin,
            viewModel = viewModel,
            onForumClick = { forum ->
                navState = ForumNavState.DiscussionList(forum)
                viewModel.cargarDiscusiones(forum.id_forum)
            },
            onForoCreado = onForoCreado,
        )

        is ForumNavState.DiscussionList -> DiscussionListContent(
            forum = state.forum,
            juego = juegos.find { it.id_game == state.forum.id_game },
            viewModel = viewModel,
            isAdmin = isAdmin,
            onVolver = { navState = ForumNavState.ForumList },
            onDiscussionClick = { disc ->
                navState = ForumNavState.DiscussionDetail(disc, state.forum)
                viewModel.cargarReplies(disc.id_discussion)
            },
        )

        is ForumNavState.DiscussionDetail -> DiscussionDetailContent(
            discussion = state.discussion,
            viewModel = viewModel,
            isAdmin = isAdmin,
            onVolver = {
                navState = ForumNavState.DiscussionList(state.forum)
                viewModel.cargarDiscusiones(state.forum.id_forum)
            },
        )
    }
}

@Composable
private fun ForumListContent(
    forums: List<ForumOut>,
    juegos: List<GameOut>,
    isAdmin: Boolean,
    viewModel: ForumViewModel,
    onForumClick: (ForumOut) -> Unit,
    onForoCreado: ((ForumOut) -> Unit)?,
) {
    var busqueda by remember { mutableStateOf("") }
    var mostrarCrearForo by remember { mutableStateOf(false) }
    var isCreatingForo by remember { mutableStateOf(false) }
    val actionError by viewModel.actionError.collectAsState()

    val filtrados = forums.filter { f ->
        busqueda.isBlank() ||
        f.name.contains(busqueda, ignoreCase = true) ||
        juegos.find { it.id_game == f.id_game }?.name?.contains(busqueda, ignoreCase = true) == true
    }

    if (mostrarCrearForo) {
        CrearForoDialog(
            juegos = juegos,
            isAdmin = isAdmin,
            isLoading = isCreatingForo,
            error = if (isCreatingForo) null else actionError,
            onDismiss = { if (!isCreatingForo) { mostrarCrearForo = false; viewModel.clearError() } },
            onCreate = { name, gameName, forumType ->
                isCreatingForo = true
                viewModel.clearError()
                viewModel.crearForo(name, gameName, forumType) { created ->
                    isCreatingForo = false
                    if (created != null) {
                        mostrarCrearForo = false
                        viewModel.clearError()
                        onForoCreado?.invoke(created)
                    }
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarCrearForo = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, "Nuevo foro") }
        }
    ) { padding ->
    Column(Modifier.fillMaxSize().padding(padding)) {
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
                        Icons.Default.Forum,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Foros",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 34.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 38.sp
                    )
                    Text(
                        "Discute, comparte y puntúa",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            placeholder = { Text("Buscar foros...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 10.dp)
        )

        if (filtrados.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay foros disponibles", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtrados, key = { it.id_forum }) { forum ->
                    ForumCard(
                        forum = forum,
                        gameName = juegos.find { it.id_game == forum.id_game }?.name,
                        onClick = { onForumClick(forum) }
                    )
                }
            }
        }
    } // Column
    } // Scaffold
}

@Composable
private fun ForumCard(forum: ForumOut, gameName: String?, onClick: () -> Unit) {
    val isOfficial = forum.forum_type == "official"
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isOfficial) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isOfficial) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.tertiary)
                } else {
                    Text(
                        forum.name.first().uppercaseChar().toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(forum.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    if (isOfficial) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("OFICIAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
                if (gameName != null) {
                    Text(
                        gameName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Text("›", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscussionListContent(
    forum: ForumOut,
    juego: GameOut?,
    viewModel: ForumViewModel,
    isAdmin: Boolean,
    onVolver: () -> Unit,
    onDiscussionClick: (DiscussionOut) -> Unit,
) {
    val discussions by viewModel.discussions.collectAsState()
    val myVotes by viewModel.myVotes.collectAsState()
    val isLoading by viewModel.discussionsLoading.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    var busqueda by remember { mutableStateOf("") }
    var mostrarCrear by remember { mutableStateOf(false) }
    var isCreating by remember { mutableStateOf(false) }
    var discAEliminar by remember { mutableStateOf<DiscussionOut?>(null) }

    val filtradas = discussions.filter { d ->
        busqueda.isBlank() ||
        d.name.contains(busqueda, ignoreCase = true) ||
        d.author_username?.contains(busqueda, ignoreCase = true) == true ||
        d.comments?.contains(busqueda, ignoreCase = true) == true
    }

    if (mostrarCrear) {
        CrearDiscusionDialog(
            isLoading = isCreating,
            error = if (isCreating) null else actionError,
            onDismiss = {
                if (!isCreating) {
                    mostrarCrear = false
                    viewModel.clearError()
                }
            },
            onCreate = { name, content, imagePath ->
                isCreating = true
                viewModel.clearError()
                viewModel.crearDiscusion(forum.id_forum, name, content, imagePath) { ok ->
                    isCreating = false
                    if (ok) {
                        mostrarCrear = false
                        viewModel.clearError()
                    }
                }
            }
        )
    }

    discAEliminar?.let { disc ->
        AlertDialog(
            onDismissRequest = { discAEliminar = null },
            title = { Text("Eliminar publicación") },
            text = { Text("¿Seguro que quieres eliminar \"${disc.name}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.eliminarDiscusion(disc.id_discussion)
                    discAEliminar = null
                }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { discAEliminar = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(forum.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (juego != null) Text(juego.name, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarCrear = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, "Nueva publicación") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = busqueda,
                onValueChange = { busqueda = it },
                placeholder = { Text("Buscar publicaciones...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, Modifier.size(18.dp)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
            )

            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                filtradas.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (busqueda.isBlank()) "No hay publicaciones en este foro"
                        else "Sin resultados para \"$busqueda\"",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtradas, key = { it.id_discussion }) { disc ->
                        DiscussionCard(
                            discussion = disc,
                            myVote = myVotes[disc.id_discussion] ?: 0,
                            isAdmin = isAdmin,
                            onVote = { vote -> viewModel.votar(disc, vote) },
                            onClick = { onDiscussionClick(disc) },
                            onDelete = if (isAdmin || disc.id_user == SessionManager.userId)
                                { { discAEliminar = disc } } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscussionCard(
    discussion: DiscussionOut,
    myVote: Int,
    isAdmin: Boolean,
    onVote: (Int) -> Unit,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val imageUrl = resolveAppImageUrl(discussion.image)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    imageLoader = LocalAppImageLoader.current,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                )
            }

            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Text(
                        discussion.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (onDelete != null) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                if (!discussion.comments.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        discussion.comments,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AuthorChip(discussion.author_username, resolveAppImageUrl(discussion.author_image))
                    if (discussion.created_at != null) {
                        Text(
                            " · ${discussion.created_at}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    VoteButton(
                        icon = Icons.Default.ThumbUp,
                        count = discussion.likes,
                        active = myVote == 1,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = { onVote(1) }
                    )
                    Spacer(Modifier.width(12.dp))
                    VoteButton(
                        icon = Icons.Default.ThumbDown,
                        count = discussion.dislikes,
                        active = myVote == -1,
                        activeColor = MaterialTheme.colorScheme.error,
                        onClick = { onVote(-1) }
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "${discussion.reply_count} comentarios",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun List<PostReplyOut>.toThreadedOrder(): List<PostReplyOut> {
    val byParent = groupBy { it.id_parent_reply }
    fun flatten(comment: PostReplyOut): List<PostReplyOut> =
        listOf(comment) + (byParent[comment.id_reply] ?: emptyList()).flatMap { flatten(it) }
    return filter { it.id_parent_reply == null }.flatMap { flatten(it) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscussionDetailContent(
    discussion: DiscussionOut,
    viewModel: ForumViewModel,
    isAdmin: Boolean,
    onVolver: () -> Unit,
) {
    val repliesRaw by viewModel.replies.collectAsState()
    val replies = repliesRaw.toThreadedOrder()
    val myVotes by viewModel.myVotes.collectAsState()
    val myCommentVotes by viewModel.myCommentVotes.collectAsState()
    val repliesLoading by viewModel.repliesLoading.collectAsState()
    val actionError by viewModel.actionError.collectAsState()

    // Find live version from discussions list (so votes update in real time)
    val discussions by viewModel.discussions.collectAsState()
    val liveDisc = discussions.find { it.id_discussion == discussion.id_discussion } ?: discussion
    val liveMyVote = myVotes[liveDisc.id_discussion] ?: 0
    var mostrarResponder by remember { mutableStateOf(false) }
    var replyingToComment by remember { mutableStateOf<PostReplyOut?>(null) }
    var isSendingReply by remember { mutableStateOf(false) }
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }
    val imageUrl = resolveAppImageUrl(liveDisc.image)

    expandedImageUrl?.let { url ->
        ImageLightbox(imageUrl = url, onDismiss = { expandedImageUrl = null })
    }

    if (mostrarResponder) {
        ComentarDialog(
            isLoading = isSendingReply,
            error = if (isSendingReply) null else actionError,
            replyingToAuthor = replyingToComment?.author_username,
            onDismiss = {
                if (!isSendingReply) {
                    mostrarResponder = false
                    replyingToComment = null
                    viewModel.clearError()
                }
            },
            onSend = { content, imagePath ->
                isSendingReply = true
                viewModel.clearError()
                viewModel.crearReply(
                    liveDisc.id_discussion, content, imagePath,
                    parentReplyId = replyingToComment?.id_reply
                ) { ok ->
                    isSendingReply = false
                    if (ok) {
                        mostrarResponder = false
                        replyingToComment = null
                        viewModel.clearError()
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(liveDisc.name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.Default.ArrowBack, "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { replyingToComment = null; mostrarResponder = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, "Comentar") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Column {
                    if (imageUrl != null) {
                        AsyncImage(
                            model = imageUrl,
                            imageLoader = LocalAppImageLoader.current,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                    }
                    Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                        Text(liveDisc.name, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 26.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AuthorChip(liveDisc.author_username, resolveAppImageUrl(liveDisc.author_image))
                            if (liveDisc.created_at != null) {
                                Text(
                                    " · ${liveDisc.created_at}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (!liveDisc.comments.isNullOrBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                liveDisc.comments,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VoteButton(
                                    icon = Icons.Default.ThumbUp,
                                    count = liveDisc.likes,
                                    active = liveMyVote == 1,
                                    activeColor = MaterialTheme.colorScheme.primary,
                                    large = true,
                                    onClick = { viewModel.votar(liveDisc, 1) }
                                )
                                Spacer(Modifier.width(20.dp))
                                VoteButton(
                                    icon = Icons.Default.ThumbDown,
                                    count = liveDisc.dislikes,
                                    active = liveMyVote == -1,
                                    activeColor = MaterialTheme.colorScheme.error,
                                    large = true,
                                    onClick = { viewModel.votar(liveDisc, -1) }
                                )
                                Spacer(Modifier.weight(1f))
                                HonorChip(liveDisc.likes - liveDisc.dislikes)
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                }
            }

            item {
                Text(
                    "${liveDisc.reply_count} comentarios",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            when {
                repliesLoading -> item {
                    Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                }
                replies.isEmpty() -> item {
                    Text(
                        "Sé el primero en comentar.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 20.dp, bottom = 12.dp)
                    )
                }
                else -> items(replies, key = { it.id_reply }) { reply ->
                    CommentCard(
                        reply = reply,
                        myVote = myCommentVotes[reply.id_reply] ?: 0,
                        canDelete = isAdmin || reply.id_user == SessionManager.userId,
                        onVote = { vote -> viewModel.votarComentario(reply, vote) },
                        onReply = {
                            replyingToComment = reply
                            mostrarResponder = true
                        },
                        onDelete = { viewModel.eliminarReply(liveDisc.id_discussion, reply.id_reply) },
                        onImageClick = { url -> expandedImageUrl = url }
                    )
                }
            }
        }
    }
}

@Composable
private fun VoteButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    active: Boolean,
    activeColor: Color,
    large: Boolean = false,
    onClick: () -> Unit,
) {
    val size = if (large) 22.dp else 18.dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(size),
            tint = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(4.dp))
        Text(
            count.toString(),
            fontSize = if (large) 14.sp else 12.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HonorChip(score: Int) {
    val color = when {
        score > 0  -> MaterialTheme.colorScheme.primary
        score < 0  -> MaterialTheme.colorScheme.error
        else       -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (score > 0) "+$score honor" else "$score honor",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun AuthorChip(username: String?, profileImageUrl: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (profileImageUrl != null) {
                AsyncImage(
                    model = profileImageUrl,
                    imageLoader = LocalAppImageLoader.current,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    (username ?: "?").take(1).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.width(6.dp))
        Text(username ?: "Anónimo", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ImageLightbox(imageUrl: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                imageLoader = LocalAppImageLoader.current,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(24.dp)
            )
        }
    }
}

@Composable
private fun CommentCard(
    reply: PostReplyOut,
    myVote: Int,
    canDelete: Boolean,
    onVote: (Int) -> Unit,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    onImageClick: (String) -> Unit = {},
) {
    val imageUrl = resolveAppImageUrl(reply.image)
    val profileImageUrl = resolveAppImageUrl(reply.author_image)
    val isNested = reply.id_parent_reply != null
    val startPadding = if (isNested) 36.dp else 12.dp

    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar comentario") },
            text = { Text("¿Seguro que quieres eliminar este comentario? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { showDeleteConfirm = false; onDelete() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = 12.dp, top = 8.dp, bottom = 2.dp)
    ) {
        if (isNested && reply.parent_author != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 36.dp, bottom = 3.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Reply,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "respondiendo a ${reply.parent_author}",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl != null) {
                    AsyncImage(
                        model = profileImageUrl,
                        imageLoader = LocalAppImageLoader.current,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        (reply.author_username ?: "?").take(1).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    reply.author_username ?: "Anónimo",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                if (reply.created_at != null) {
                    Text(
                        reply.created_at,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Responder") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        onClick = { menuExpanded = false; onReply() }
                    )
                    if (canDelete) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = { menuExpanded = false; showDeleteConfirm = true }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Card(
            shape = RoundedCornerShape(4.dp, 12.dp, 12.dp, 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.wrapContentWidth(Alignment.Start).widthIn(min = 80.dp, max = 320.dp)
        ) {
            Column {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        imageLoader = LocalAppImageLoader.current,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .heightIn(max = 180.dp)
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick(imageUrl) }
                    )
                }
                Text(
                    reply.content,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 2.dp, top = 4.dp)
        ) {
            VoteButton(
                icon = Icons.Default.ThumbUp,
                count = reply.likes,
                active = myVote == 1,
                activeColor = MaterialTheme.colorScheme.primary,
                onClick = { onVote(1) }
            )
            Spacer(Modifier.width(6.dp))
            VoteButton(
                icon = Icons.Default.ThumbDown,
                count = reply.dislikes,
                active = myVote == -1,
                activeColor = MaterialTheme.colorScheme.error,
                onClick = { onVote(-1) }
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        )
    }
}

@Composable
private fun CrearDiscusionDialog(
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (name: String, content: String?, imagePath: String?) -> Unit,
) {
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva publicación", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido (opcional)") },
                    minLines = 3,
                    maxLines = 6,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = { pickImageFile { path -> if (path != null) imagePath = path } }
                    ) { Text("Imagen") }
                    if (imagePath != null) {
                        Text(
                            imagePath!!.substringAfterLast('/').substringAfterLast('\\'),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = titulo.isNotBlank() && !isLoading,
                onClick = { onCreate(titulo, contenido.trim().ifBlank { null }, imagePath) }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isLoading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun CrearForoDialog(
    juegos: List<GameOut>,
    isAdmin: Boolean,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (name: String, gameName: String, forumType: String) -> Unit,
) {
    var nombre by remember { mutableStateOf("") }
    var juegoSeleccionado by remember { mutableStateOf<GameOut?>(null) }
    var menuJuegoExpandido by remember { mutableStateOf(false) }
    var forumType by remember { mutableStateOf("community") }
    var menuTipoExpandido by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = { Text("Nuevo foro", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del foro *") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { menuJuegoExpandido = true },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            juegoSeleccionado?.name ?: "Seleccionar juego *",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(
                        expanded = menuJuegoExpandido,
                        onDismissRequest = { menuJuegoExpandido = false },
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        juegos.forEach { juego ->
                            DropdownMenuItem(
                                text = { Text(juego.name) },
                                onClick = {
                                    juegoSeleccionado = juego
                                    menuJuegoExpandido = false
                                }
                            )
                        }
                    }
                }

                if (isAdmin) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { menuTipoExpandido = true },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (forumType == "official") "Oficial" else "Comunidad")
                        }
                        DropdownMenu(
                            expanded = menuTipoExpandido,
                            onDismissRequest = { menuTipoExpandido = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Comunidad") },
                                onClick = { forumType = "community"; menuTipoExpandido = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Oficial") },
                                onClick = { forumType = "official"; menuTipoExpandido = false }
                            )
                        }
                    }
                }

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = nombre.isNotBlank() && juegoSeleccionado != null && !isLoading,
                onClick = {
                    val juego = juegoSeleccionado ?: return@Button
                    onCreate(nombre.trim(), juego.name, forumType)
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isLoading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun ComentarDialog(
    isLoading: Boolean,
    error: String?,
    replyingToAuthor: String? = null,
    onDismiss: () -> Unit,
    onSend: (content: String, imagePath: String?) -> Unit,
) {
    var contenido by remember { mutableStateOf("") }
    var imagePath by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (replyingToAuthor != null) "Responder a $replyingToAuthor" else "Nuevo comentario",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text(if (replyingToAuthor != null) "Tu respuesta *" else "Tu comentario *") },
                    minLines = 3,
                    maxLines = 6,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = { pickImageFile { path -> if (path != null) imagePath = path } }
                    ) { Text("Imagen") }
                    if (imagePath != null) {
                        Text(
                            imagePath!!.substringAfterLast('/').substringAfterLast('\\'),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = contenido.isNotBlank() && !isLoading,
                onClick = { onSend(contenido, imagePath) }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Enviar")
                }
            }
        },
        dismissButton = {
            TextButton(enabled = !isLoading, onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
