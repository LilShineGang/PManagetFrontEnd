package bg.pm.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    var accessToken: String? = null
    var username: String? = null

    private val _role = MutableStateFlow<String?>(null)
    val roleFlow: StateFlow<String?> = _role.asStateFlow()

    var role: String?
        get() = _role.value
        set(value) { _role.value = value }

    fun isLoggedIn(): Boolean = accessToken != null
    fun isAdmin(): Boolean = role == "admin"

    fun clear() {
        accessToken = null
        username = null
        role = null
    }
}
