package bg.pm.ui.home

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

    private val _isAdmin = MutableStateFlow(SessionManager.isAdmin())
    val isAdmin: StateFlow<Boolean> = SessionManager.roleFlow
        .map { SessionManager.isAdminRole(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionManager.isAdmin())

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
