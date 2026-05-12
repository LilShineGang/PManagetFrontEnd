package bg.pm.network

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val username: String,
    val password: String,
    val name: String,
    val email: String,
    val image: String = "",
    val role: String = "user"
)

@Serializable
data class SignupResponse(
    val id: Int? = null,
    val username: String? = null,
    val email: String? = null,
    val name: String? = null,
    val role: String? = null,
    val image: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val access_token: String? = null,
    val refresh_token: String? = null,
    val token_type: String? = null,
    val detail: String? = null
)

@Serializable
data class RefreshRequest(
    val refresh_token: String
)

@Serializable
data class GameOut(
    val id_game: Int,
    val name: String,
    val gender: String,
    val difficulty: String,
    val rating: Double? = null,
    val image: String? = null,
    val category: String
)
