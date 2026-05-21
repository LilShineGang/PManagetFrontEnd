package bg.pm.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.AchievementOut
import bg.pm.network.AchievementIn
import bg.pm.network.ApiService
import bg.pm.network.BuildIn
import bg.pm.network.BuildOut
import bg.pm.network.SessionManager
import bg.pm.network.WikiIn
import bg.pm.network.WikiOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameDetailViewModel : ViewModel() {
    private val _wikiEntries = MutableStateFlow<List<WikiOut>>(emptyList())
    val wikiEntries: StateFlow<List<WikiOut>> = _wikiEntries.asStateFlow()

    private val _buildEntries = MutableStateFlow<List<BuildOut>>(emptyList())
    val buildEntries: StateFlow<List<BuildOut>> = _buildEntries.asStateFlow()

    private val _achievements = MutableStateFlow<List<AchievementOut>>(emptyList())
    val achievements: StateFlow<List<AchievementOut>> = _achievements.asStateFlow()

    private val _forumId = MutableStateFlow<Int?>(null)
    val forumId: StateFlow<Int?> = _forumId.asStateFlow()

    private val _dataLoading = MutableStateFlow(true)
    val dataLoading: StateFlow<Boolean> = _dataLoading.asStateFlow()

    private val _actionLoading = MutableStateFlow(false)
    val actionLoading: StateFlow<Boolean> = _actionLoading.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    fun cargarDatos(gameId: Int) {
        val token = SessionManager.accessToken ?: return
        _dataLoading.value = true
        viewModelScope.launch {
            val forums = ApiService.obtenerForos(token)
            _forumId.value = forums.firstOrNull { it.id_game == gameId }?.id_forum
            reloadGameData(gameId, token)
        }
    }

    suspend fun recargarContenido(gameId: Int) {
        val token = SessionManager.accessToken ?: return
        reloadGameData(gameId, token)
    }

    fun clearActionError() {
        _actionError.value = null
    }

    fun crearWiki(
        name: String,
        category: String,
        description: String,
        onSuccess: () -> Unit,
    ) {
        val token = SessionManager.accessToken ?: return
        val currentForumId = _forumId.value
        if (currentForumId == null) {
            _actionError.value = "No hay foro asociado a este juego."
            return
        }

        viewModelScope.launch {
            _actionLoading.value = true
            _actionError.value = null
            try {
                val createdWiki = ApiService.crearWiki(
                    WikiIn(
                        name = name,
                        category = category,
                        description = description,
                        id_forum = currentForumId,
                    ),
                    token,
                )
                _wikiEntries.value = _wikiEntries.value + createdWiki
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = "No se pudo crear la entrada de wiki: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun crearBuild(
        name: String,
        planner: String,
        category: String,
        description: String,
        onSuccess: () -> Unit,
    ) {
        val token = SessionManager.accessToken ?: return
        val currentForumId = _forumId.value
        if (currentForumId == null) {
            _actionError.value = "No hay foro asociado a este juego."
            return
        }

        viewModelScope.launch {
            _actionLoading.value = true
            _actionError.value = null
            try {
                val createdBuild = ApiService.crearBuild(
                    BuildIn(
                        name = name,
                        planner = planner,
                        category = category,
                        description = description,
                        id_forum = currentForumId,
                    ),
                    token,
                )
                _buildEntries.value = _buildEntries.value + createdBuild
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = "No se pudo crear la build: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    fun crearLogro(
        difficulty: String,
        description: String,
        gameId: Int,
        onSuccess: () -> Unit,
    ) {
        val token = SessionManager.accessToken ?: return

        viewModelScope.launch {
            _actionLoading.value = true
            _actionError.value = null
            try {
                ApiService.crearLogro(
                    AchievementIn(
                        difficulty = difficulty,
                        description = description,
                        id_game = gameId,
                    ),
                    token,
                )
                _achievements.value = ApiService.obtenerLogrosPorJuego(gameId, token)
                onSuccess()
            } catch (e: Exception) {
                _actionError.value = "No se pudo crear el logro: ${e.message}"
            } finally {
                _actionLoading.value = false
            }
        }
    }

    private suspend fun reloadGameData(gameId: Int, token: String) {
        _wikiEntries.value = ApiService.obtenerWikiPorJuego(gameId, token)
        _buildEntries.value = ApiService.obtenerBuildsPorJuego(gameId, token)
        _achievements.value = ApiService.obtenerLogrosPorJuego(gameId, token)
        _dataLoading.value = false
    }
}
