package bg.pm.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.ApiService
import bg.pm.network.LoginRequest
import bg.pm.network.SessionManager
import bg.pm.network.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()

    private val _contrasena = MutableStateFlow("")
    val contrasena: StateFlow<String> = _contrasena.asStateFlow()

    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    fun onUsuarioChange(valor: String) {
        _usuario.value = valor
        _mensajeError.value = null
    }

    fun onContrasenaChange(valor: String) {
        _contrasena.value = valor
        _mensajeError.value = null
    }

    fun validar(onSuccess: () -> Unit) {
        if (_usuario.value.isBlank() || _contrasena.value.isBlank()) {
            _mensajeError.value = "Completa todos los campos"
            return
        }

        _isValidating.value = true
        _mensajeError.value = null

        viewModelScope.launch {
            try {
                val response = ApiService.validarLogin(
                    LoginRequest(
                        username = _usuario.value.trim(),
                        password = _contrasena.value
                    )
                )
                _isValidating.value = false

                if (response.access_token != null && response.refresh_token != null) {
                    val uname = _usuario.value.trim()
                    TokenStorage.saveTokens(response.access_token, response.refresh_token, uname)
                    SessionManager.accessToken = response.access_token
                    SessionManager.username = uname
                    onSuccess()
                } else {
                    _mensajeError.value = response.detail ?: "Credenciales incorrectas"
                }
            } catch (e: Exception) {
                _isValidating.value = false
                _mensajeError.value = "Error de conexión: ${e.message}"
            }
        }
    }

    fun clear() {
        _usuario.value = ""
        _contrasena.value = ""
        _mensajeError.value = null
        _isValidating.value = false
    }
}

