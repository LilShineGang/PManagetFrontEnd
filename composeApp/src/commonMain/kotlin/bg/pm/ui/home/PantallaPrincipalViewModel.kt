package bg.pm.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.ApiService
import bg.pm.network.ChatOut
import bg.pm.network.ForumOut
import bg.pm.network.GameOut
import bg.pm.network.SessionManager
import bg.pm.network.UserOut
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
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

    private val _perfil = MutableStateFlow<UserOut?>(null)
    val perfil: StateFlow<UserOut?> = _perfil.asStateFlow()

    private val _likedGameIds = MutableStateFlow<Set<Int>>(emptySet())
    val likedGameIds: StateFlow<Set<Int>> = _likedGameIds.asStateFlow()

    private val _juegosLiked = MutableStateFlow<List<GameOut>>(emptyList())
    val juegosLiked: StateFlow<List<GameOut>> = _juegosLiked.asStateFlow()

    private val _isAdmin = MutableStateFlow(SessionManager.isAdmin())
    val isAdmin: StateFlow<Boolean> = SessionManager.roleFlow
        .map { SessionManager.isAdminRole(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.isAdmin())

    fun actualizarPerfilLocal(user: UserOut) {
        _perfil.value = user
    }

    fun cargarDatos() {
        val token = SessionManager.accessToken ?: return
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                _perfil.value = ApiService.obtenerPerfil(token)
                _juegos.value = ApiService.obtenerJuegos(token)
                _foros.value = ApiService.obtenerForos(token)
                _chats.value = ApiService.obtenerChats(token)
                val liked = ApiService.obtenerFavoritos(token)
                _juegosLiked.value = liked
                _likedGameIds.value = liked.map { it.id_game }.toSet()
            } catch (e: Exception) {
                _error.value = "Error al cargar datos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike(juego: GameOut) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            val wasLiked = juego.id_game in _likedGameIds.value
            // Optimistic update
            if (wasLiked) {
                _likedGameIds.value = _likedGameIds.value - juego.id_game
                _juegosLiked.value = _juegosLiked.value.filter { it.id_game != juego.id_game }
            } else {
                _likedGameIds.value = _likedGameIds.value + juego.id_game
                _juegosLiked.value = _juegosLiked.value + juego
            }
            try {
                if (wasLiked) ApiService.quitarLike(juego.id_game, token)
                else ApiService.darLike(juego.id_game, token)
            } catch (_: Exception) {
                // Revert on error
                if (wasLiked) {
                    _likedGameIds.value = _likedGameIds.value + juego.id_game
                    _juegosLiked.value = _juegosLiked.value + juego
                } else {
                    _likedGameIds.value = _likedGameIds.value - juego.id_game
                    _juegosLiked.value = _juegosLiked.value.filter { it.id_game != juego.id_game }
                }
            }
        }
    }

    fun eliminarJuego(gameId: Int) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            try {
                ApiService.eliminarJuego(gameId, token)
                _juegos.value = _juegos.value.filter { it.id_game != gameId }
                _likedGameIds.value = _likedGameIds.value - gameId
                _juegosLiked.value = _juegosLiked.value.filter { it.id_game != gameId }
            } catch (_: Exception) { }
        }
    }
}
