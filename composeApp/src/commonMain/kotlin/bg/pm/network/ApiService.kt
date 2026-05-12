package bg.pm.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiService {
    private const val BASE_URL = "http://10.160.81.1:8000"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun registrarUsuario(usuario: SignupRequest): SignupResponse {
        return try {
            client.post("$BASE_URL/users/signup/") {
                contentType(ContentType.Application.Json)
                setBody(usuario)
            }.body()
        } catch (e: Exception) {
            throw Exception("Error en el registro: ${e.message}")
        }
    }

    suspend fun validarLogin(login: LoginRequest): LoginResponse {
        return try {
            client.post("$BASE_URL/users/login/") {
                setBody(FormDataContent(Parameters.build {
                    append("username", login.username)
                    append("password", login.password)
                    append("grant_type", "password")
                }))
            }.body()
        } catch (e: Exception) {
            throw Exception("Error en el login: ${e.message}")
        }
    }

    suspend fun obtenerJuegos(token: String): List<GameOut> {
        return try {
            client.get("$BASE_URL/games/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            throw Exception("Error al obtener juegos: ${e.message}")
        }
    }

    suspend fun refreshToken(refreshToken: String): LoginResponse {
        return try {
            client.post("$BASE_URL/users/refresh/") {
                contentType(ContentType.Application.Json)
                setBody(RefreshRequest(refresh_token = refreshToken))
            }.body()
        } catch (e: Exception) {
            throw Exception("Error al refrescar sesión: ${e.message}")
        }
    }

    suspend fun obtenerForos(token: String): List<ForumOut> {
        return try {
            client.get("$BASE_URL/forums/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            throw Exception("Error al obtener foros: ${e.message}")
        }
    }

    suspend fun obtenerChats(token: String): List<ChatOut> {
        return try {
            client.get("$BASE_URL/chats/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            throw Exception("Error al obtener chats: ${e.message}")
        }
    }
}
