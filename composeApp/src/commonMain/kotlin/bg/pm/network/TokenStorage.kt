package bg.pm.network

// Expiry constants matching the backend
const val ACCESS_TOKEN_EXPIRE_MS  = 7L  * 24 * 60 * 60 * 1000   // 7 días
const val REFRESH_TOKEN_EXPIRE_MS = 90L * 24 * 60 * 60 * 1000   // 90 días

expect object TokenStorage {
    fun saveTokens(accessToken: String, refreshToken: String, username: String)
    fun updateAccessToken(accessToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun getUsername(): String?
    fun isAccessTokenValid(): Boolean
    fun isRefreshTokenValid(): Boolean
    fun clear()
}
