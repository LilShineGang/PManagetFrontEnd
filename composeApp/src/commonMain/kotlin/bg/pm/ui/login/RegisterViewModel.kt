package bg.pm.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.network.ApiService
import bg.pm.network.SignupRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {
    
    private val _usuario = MutableStateFlow("")
    val usuario: StateFlow<String> = _usuario.asStateFlow()
    
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    
    private val _contrasena = MutableStateFlow("")
    val contrasena: StateFlow<String> = _contrasena.asStateFlow()
    
    private val _confirmarContrasena = MutableStateFlow("")
    val confirmarContrasena: StateFlow<String> = _confirmarContrasena.asStateFlow()
    
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    
    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()
    
    private val _isRegistrando = MutableStateFlow(false)
    val isRegistrando: StateFlow<Boolean> = _isRegistrando.asStateFlow()
    
    fun onUsuarioChange(valor: String) {
        _usuario.value = valor
        _mensajeError.value = null
        _mensajeExito.value = null
    }
    
    fun onEmailChange(valor: String) {
        _email.value = valor
        _mensajeError.value = null
        _mensajeExito.value = null
    }
    
    fun onContrasenaChange(valor: String) {
        _contrasena.value = valor
        _mensajeError.value = null
        _mensajeExito.value = null
    }
    
    fun onConfirmarContrasenaChange(valor: String) {
        _confirmarContrasena.value = valor
        _mensajeError.value = null
        _mensajeExito.value = null
    }
    
    fun registrar(onSuccess: () -> Unit) {
        when {
            _usuario.value.isBlank() || _email.value.isBlank() || 
            _contrasena.value.isBlank() || _confirmarContrasena.value.isBlank() -> {
                _mensajeError.value = "Completa todos los campos"
            }
            !isEmailValido(_email.value) -> {
                _mensajeError.value = "El email no es válido"
            }
            _contrasena.value.length < 6 -> {
                _mensajeError.value = "La contraseña debe tener al menos 6 caracteres"
            }
            _contrasena.value != _confirmarContrasena.value -> {
                _mensajeError.value = "Las contraseñas no coinciden"
            }
            else -> {
                _isRegistrando.value = true
                _mensajeError.value = null
                _mensajeExito.value = null
                
                viewModelScope.launch {
                    try {
                        // Llamar a la API para registrar
                        val signupRequest = SignupRequest(
                            username = _usuario.value.trim(),
                            password = _contrasena.value,
                            name = _usuario.value.trim(),
                            email = _email.value.trim()
                        )
                        
                        val response = ApiService.registrarUsuario(signupRequest)
                        
                        _isRegistrando.value = false
                        
                        if (response.id != null) {
                            _mensajeExito.value = "¡Registro exitoso! Bienvenido ${_usuario.value}"
                            
                            // Limpiar después de 1 segundo y ejecutar callback
                            kotlinx.coroutines.delay(1000)
                            clear()
                            onSuccess()
                        } else {
                            _mensajeError.value = response.message ?: "Error desconocido en el registro"
                        }
                    } catch (e: Exception) {
                        _isRegistrando.value = false
                        _mensajeError.value = "Error: ${e.message}"
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun isEmailValido(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
        return email.matches(emailRegex)
    }
    
    fun clear() {
        _usuario.value = ""
        _email.value = ""
        _contrasena.value = ""
        _confirmarContrasena.value = ""
        _mensajeError.value = null
        _mensajeExito.value = null
        _isRegistrando.value = false
    }
}
