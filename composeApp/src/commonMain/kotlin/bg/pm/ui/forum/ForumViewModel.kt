package bg.pm.ui.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.ApiService
import bg.pm.network.DiscussionIn
import bg.pm.network.DiscussionOut
import bg.pm.network.ForumIn
import bg.pm.network.ForumOut
import bg.pm.network.PostReplyIn
import bg.pm.network.PostReplyOut
import bg.pm.network.SessionManager
import bg.pm.PickedImageUpload
import bg.pm.readPickedImageUpload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ForumViewModel : ViewModel() {

    private val _discussions = MutableStateFlow<List<DiscussionOut>>(emptyList())
    val discussions: StateFlow<List<DiscussionOut>> = _discussions.asStateFlow()

    private val _replies = MutableStateFlow<List<PostReplyOut>>(emptyList())
    val replies: StateFlow<List<PostReplyOut>> = _replies.asStateFlow()

    private val _myVotes = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val myVotes: StateFlow<Map<Int, Int>> = _myVotes.asStateFlow()

    private val _myCommentVotes = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val myCommentVotes: StateFlow<Map<Int, Int>> = _myCommentVotes.asStateFlow()

    private val _discussionsLoading = MutableStateFlow(false)
    val discussionsLoading: StateFlow<Boolean> = _discussionsLoading.asStateFlow()

    private val _repliesLoading = MutableStateFlow(false)
    val repliesLoading: StateFlow<Boolean> = _repliesLoading.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun clearError() { _actionError.value = null }

    fun cargarDiscusiones(forumId: Int) {
        val token = SessionManager.accessToken ?: return
        _discussionsLoading.value = true
        viewModelScope.launch {
            try {
                _discussions.value = ApiService.obtenerDiscusionesPorForo(forumId, token)
                val votes = mutableMapOf<Int, Int>()
                _discussions.value.forEach { d ->
                    try {
                        val r = ApiService.obtenerMiVoto(d.id_discussion, token)
                        votes[d.id_discussion] = r.my_vote
                    } catch (_: Exception) {}
                }
                _myVotes.value = votes
            } catch (e: Exception) {
                _actionError.value = "Error al cargar discusiones: ${e.message}"
            } finally {
                _discussionsLoading.value = false
            }
        }
    }

    fun crearDiscusion(
        forumId: Int,
        name: String,
        content: String?,
        imagePath: String?,
        onDone: (Boolean) -> Unit,
    ) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            try {
                var disc = ApiService.crearDiscusion(
                    DiscussionIn(name = name.trim(), comments = content?.trim(), id_forum = forumId),
                    token,
                )
                val upload = imagePath?.takeIf { it.isNotBlank() }?.let { readPickedImageUpload(it) }
                if (upload != null) {
                    disc = ApiService.subirImagenDiscusion(disc.id_discussion, upload, token)
                }
                _discussions.value = listOf(disc) + _discussions.value
                onDone(true)
            } catch (e: Exception) {
                _actionError.value = "Error al crear: ${e.message}"
                onDone(false)
            }
        }
    }

    fun eliminarDiscusion(discussionId: Int) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            try {
                ApiService.eliminarDiscusion(discussionId, token)
                _discussions.value = _discussions.value.filter { it.id_discussion != discussionId }
            } catch (e: Exception) {
                _actionError.value = "Error al eliminar: ${e.message}"
            }
        }
    }

    fun votar(discussion: DiscussionOut, vote: Int) {
        val token = SessionManager.accessToken ?: return
        val id = discussion.id_discussion
        val currentVote = _myVotes.value[id] ?: 0
        val newVote = if (currentVote == vote) 0 else vote
        val likeDelta = countLikeDelta(currentVote, newVote)
        val dislikeDelta = countDislikeDelta(currentVote, newVote)

        _myVotes.value = _myVotes.value + (id to newVote)
        _discussions.value = _discussions.value.map { d ->
            if (d.id_discussion == id)
                d.copy(likes = (d.likes + likeDelta).coerceAtLeast(0),
                       dislikes = (d.dislikes + dislikeDelta).coerceAtLeast(0))
            else d
        }

        viewModelScope.launch {
            try {
                val resp = ApiService.votar(id, vote, token)
                _myVotes.value = _myVotes.value + (id to resp.my_vote)
                _discussions.value = _discussions.value.map { d ->
                    if (d.id_discussion == id) d.copy(likes = resp.likes, dislikes = resp.dislikes)
                    else d
                }
            } catch (_: Exception) {
                _myVotes.value = _myVotes.value + (id to currentVote)
                _discussions.value = _discussions.value.map { d ->
                    if (d.id_discussion == id)
                        d.copy(likes = (d.likes - likeDelta).coerceAtLeast(0),
                               dislikes = (d.dislikes - dislikeDelta).coerceAtLeast(0))
                    else d
                }
            }
        }
    }

    fun cargarReplies(discussionId: Int) {
        val token = SessionManager.accessToken ?: return
        _repliesLoading.value = true
        viewModelScope.launch {
            try {
                val loaded = ApiService.obtenerRespuestas(discussionId, token)
                _replies.value = loaded
                val votes = mutableMapOf<Int, Int>()
                loaded.forEach { r ->
                    try {
                        val resp = ApiService.obtenerMiVotoComentario(discussionId, r.id_reply, token)
                        votes[r.id_reply] = resp.my_vote
                    } catch (_: Exception) {}
                }
                _myCommentVotes.value = votes
            } finally {
                _repliesLoading.value = false
            }
        }
    }

    fun votarComentario(reply: PostReplyOut, vote: Int) {
        val token = SessionManager.accessToken ?: return
        val id = reply.id_reply
        val discussionId = reply.id_discussion

        val currentVote = _myCommentVotes.value[id] ?: 0
        val newVote = if (currentVote == vote) 0 else vote
        val likeDelta = countLikeDelta(currentVote, newVote)
        val dislikeDelta = countDislikeDelta(currentVote, newVote)

        _myCommentVotes.value = _myCommentVotes.value + (id to newVote)
        _replies.value = _replies.value.map { r ->
            if (r.id_reply == id)
                r.copy(
                    likes = (r.likes + likeDelta).coerceAtLeast(0),
                    dislikes = (r.dislikes + dislikeDelta).coerceAtLeast(0)
                )
            else r
        }

        viewModelScope.launch {
            try {
                val resp = ApiService.votarComentario(discussionId, id, vote, token)
                _myCommentVotes.value = _myCommentVotes.value + (id to resp.my_vote)
                _replies.value = _replies.value.map { r ->
                    if (r.id_reply == id) r.copy(likes = resp.likes, dislikes = resp.dislikes) else r
                }
            } catch (_: Exception) {
                _myCommentVotes.value = _myCommentVotes.value + (id to currentVote)
                _replies.value = _replies.value.map { r ->
                    if (r.id_reply == id)
                        r.copy(
                            likes = (r.likes - likeDelta).coerceAtLeast(0),
                            dislikes = (r.dislikes - dislikeDelta).coerceAtLeast(0)
                        )
                    else r
                }
            }
        }
    }

    fun crearForo(
        name: String,
        gameName: String,
        forumType: String,
        onDone: (ForumOut?) -> Unit,
    ) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            try {
                val forum = ApiService.crearForo(ForumIn(name = name.trim(), game_name = gameName, forum_type = forumType), token)
                onDone(forum)
            } catch (e: Exception) {
                _actionError.value = "Error al crear foro: ${e.message}"
                onDone(null)
            }
        }
    }

    fun crearReply(
        discussionId: Int,
        content: String,
        imagePath: String?,
        parentReplyId: Int? = null,
        onDone: (Boolean) -> Unit,
    ) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            try {
                var reply = ApiService.crearRespuesta(
                    discussionId, PostReplyIn(content = content.trim(), id_parent_reply = parentReplyId), token
                )
                val upload = imagePath?.takeIf { it.isNotBlank() }?.let { readPickedImageUpload(it) }
                if (upload != null) {
                    reply = ApiService.subirImagenRespuesta(discussionId, reply.id_reply, upload, token)
                }
                _replies.value = _replies.value + reply
                _discussions.value = _discussions.value.map { d ->
                    if (d.id_discussion == discussionId) d.copy(reply_count = d.reply_count + 1) else d
                }
                onDone(true)
            } catch (e: Exception) {
                _actionError.value = "Error al responder: ${e.message}"
                onDone(false)
            }
        }
    }

    fun eliminarReply(discussionId: Int, replyId: Int) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            try {
                ApiService.eliminarRespuesta(discussionId, replyId, token)
                _replies.value = _replies.value.filter { it.id_reply != replyId }
                _discussions.value = _discussions.value.map { d ->
                    if (d.id_discussion == discussionId)
                        d.copy(reply_count = (d.reply_count - 1).coerceAtLeast(0))
                    else d
                }
            } catch (e: Exception) {
                _actionError.value = "Error al eliminar respuesta: ${e.message}"
            }
        }
    }

    private fun countLikeDelta(old: Int, new: Int): Int = when {
        old != 1 && new == 1 -> +1
        old == 1 && new != 1 -> -1
        else -> 0
    }
    private fun countDislikeDelta(old: Int, new: Int): Int = when {
        old != -1 && new == -1 -> +1
        old == -1 && new != -1 -> -1
        else -> 0
    }
}
