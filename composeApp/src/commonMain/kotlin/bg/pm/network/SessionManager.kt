package bg.pm.network

object SessionManager {
    var accessToken: String? = null
    var username: String? = null

    fun isLoggedIn(): Boolean = accessToken != null

    fun clear() {
        accessToken = null
        username = null
    }
}
