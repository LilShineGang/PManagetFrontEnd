package bg.pm.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.AchievementOut
import bg.pm.network.ApiService
import bg.pm.network.BuildOut
import bg.pm.network.SessionManager
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

    private val _dataLoading = MutableStateFlow(true)
    val dataLoading: StateFlow<Boolean> = _dataLoading.asStateFlow()

    fun cargarDatos(gameId: Int) {
        val token = SessionManager.accessToken ?: return
        viewModelScope.launch {
            _wikiEntries.value = ApiService.obtenerWikiPorJuego(gameId, token)
            _buildEntries.value = ApiService.obtenerBuildsPorJuego(gameId, token)
            _achievements.value = ApiService.obtenerLogrosPorJuego(gameId, token)
            _dataLoading.value = false
        }
    }
}
