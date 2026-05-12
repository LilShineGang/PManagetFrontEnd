package bg.pm.network

actual object TokenStorage {
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var username: String? = null
    private var accessSavedAt: Long = 0L
    private var refreshSavedAt: Long = 0L

    actual fun saveTokens(accessToken: String, refreshToken: String, username: String) {
        this.accessToken = accessToken
        this.refreshToken = refreshToken
        this.username = username
        this.accessSavedAt = System.currentTimeMillis()
        this.refreshSavedAt = System.currentTimeMillis()
    }

    actual fun updateAccessToken(accessToken: String) {
        this.accessToken = accessToken
        this.accessSavedAt = System.currentTimeMillis()
    }

    actual fun getAccessToken(): String? = accessToken
    actual fun getRefreshToken(): String? = refreshToken
    actual fun getUsername(): String? = username

    actual fun isAccessTokenValid(): Boolean =
        accessToken != null && System.currentTimeMillis() - accessSavedAt < ACCESS_TOKEN_EXPIRE_MS

    actual fun isRefreshTokenValid(): Boolean =
        refreshToken != null && System.currentTimeMillis() - refreshSavedAt < REFRESH_TOKEN_EXPIRE_MS

    actual fun clear() {
        accessToken = null
        refreshToken = null
        username = null
        accessSavedAt = 0L
        refreshSavedAt = 0L
    }
}
