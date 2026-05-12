package bg.pm.network

import android.content.Context
import android.content.SharedPreferences

actual object TokenStorage {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("pm_tokens", Context.MODE_PRIVATE)
    }

    actual fun saveTokens(accessToken: String, refreshToken: String, username: String) {
        prefs?.edit()?.apply {
            putString("access_token", accessToken)
            putString("refresh_token", refreshToken)
            putString("username", username)
            putLong("access_saved_at", System.currentTimeMillis())
            putLong("refresh_saved_at", System.currentTimeMillis())
            apply()
        }
    }

    actual fun updateAccessToken(accessToken: String) {
        prefs?.edit()?.apply {
            putString("access_token", accessToken)
            putLong("access_saved_at", System.currentTimeMillis())
            apply()
        }
    }

    actual fun getAccessToken(): String? = prefs?.getString("access_token", null)
    actual fun getRefreshToken(): String? = prefs?.getString("refresh_token", null)
    actual fun getUsername(): String? = prefs?.getString("username", null)

    actual fun isAccessTokenValid(): Boolean {
        val savedAt = prefs?.getLong("access_saved_at", 0L) ?: return false
        return System.currentTimeMillis() - savedAt < ACCESS_TOKEN_EXPIRE_MS
    }

    actual fun isRefreshTokenValid(): Boolean {
        val savedAt = prefs?.getLong("refresh_saved_at", 0L) ?: return false
        return System.currentTimeMillis() - savedAt < REFRESH_TOKEN_EXPIRE_MS
    }

    actual fun clear() {
        prefs?.edit()?.clear()?.apply()
    }
}
