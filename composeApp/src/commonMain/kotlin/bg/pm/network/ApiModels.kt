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

@Serializable
data class GameIn(
    val name: String,
    val gender: String,
    val difficulty: String,
    val rating: Double? = null,
    val image: String? = null,
    val category: String
)

@Serializable
data class UserOut(
    val id: Int,
    val name: String,
    val username: String,
    val email: String,
    val image: String? = null,
    val role: String
)

@Serializable
data class ForumOut(
    val id_forum: Int,
    val name: String,
    val id_game: Int? = null,
    val id_user: Int? = null
)

@Serializable
data class ChatOut(
    val id_chat: Int,
    val id_mi: Int,
    val content: String,
    val timestamp: String? = null
)

@Serializable
data class WikiOut(
    val id_wiki: Int,
    val name: String,
    val category: String? = null,
    val description: String? = null,
    val id_forum: Int? = null
)

@Serializable
data class BuildOut(
    val id_build: Int,
    val name: String,
    val planner: String,
    val category: String,
    val description: String,
    val id_forum: Int? = null
)

@Serializable
data class AchievementOut(
    val id_achievement: Int,
    val difficulty: String,
    val description: String,
    val id_game: Int? = null
)
