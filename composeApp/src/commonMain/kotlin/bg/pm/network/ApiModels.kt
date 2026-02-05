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
    val id: String? = null,
    val username: String? = null,
    val email: String? = null,
    val name: String? = null,
    val role: String? = null,
    val message: String? = null
)

@Serializable
data class LoginRequest(
    val grant_type: String = "password",
    val username: String,
    val password: String,
    val scope: String = "",
    val client_id: String? = null,
    val client_secret: String? = null
)

@Serializable
data class LoginResponse(
    val access_token: String? = null,
    val token_type: String? = null,
    val id: String? = null,
    val username: String? = null,
    val message: String? = null
)
