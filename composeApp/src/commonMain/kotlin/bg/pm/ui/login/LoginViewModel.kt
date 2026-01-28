package ies.sequeros.com.dam.pmdm.administrador.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bg.pm.modelo.IUsuarioRepositorio
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val usuarioRepositorio: IUsuarioRepositorio,
) : ViewModel() {
    
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
        when {
            _usuario.value.isBlank() || _contrasena.value.isBlank() -> {
                _mensajeError.value = "Completa todos los campos"
            }
            _usuario.value.trim().lowercase() == "admin" && _contrasena.value == "admin" -> {
                onSuccess()
            }
            else -> {
                // Validar contra la base de datos
                _isValidating.value = true
                _mensajeError.value = null
                viewModelScope.launch {
                    try {
                        // Buscar por nombre (trim para eliminar espacios)
                        val usuario = usuarioRepositorio.findByName(_usuario.value.trim())
                        
                        _isValidating.value = false
                        
                        when {
                            usuario == null -> {
                                _mensajeError.value = "Usuario '${_usuario.value}' no encontrado"
                            }

                            else -> {
                                // Todo correcto
                                _mensajeError.value = null
                                onSuccess()
                            }
                        }
                    } catch (e: Exception) {
                        _isValidating.value = false
                        _mensajeError.value = "Error: ${e.message}"
                        e.printStackTrace()
                    }
                }
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
