package bg.pm.network

actual object TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var username: String? = null

    actual fun saveTokens(accessToken: String, refreshToken: String, username: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.username = username
    }

    actual fun updateAccessToken(accessToken: String) {
        this.accessToken = accessToken
    }

    actual fun getAccessToken(): String? = accessToken
    actual fun getRefreshToken(): String? = refreshToken
    actual fun getUsername(): String? = username
    actual fun isAccessTokenValid(): Boolean = accessToken != null
    actual fun isRefreshTokenValid(): Boolean = refreshToken != null

    actual fun clear() {
        accessToken = null
        refreshToken = null
        username = null
    }
}
