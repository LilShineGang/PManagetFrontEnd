package bg.pm.network

import bg.pm.getApiBaseUrl
import bg.pm.PickedImageUpload
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import bg.pm.getBaseUrl

object ApiService {
    private val BASE_URL = getBaseUrl()

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
            throw Exception("Error en el registro contra $BASE_URL: ${e.message ?: "sin detalle"}")
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

    suspend fun obtenerForosPorJuego(gameId: Int, token: String): List<ForumOut> {
        return try {
            client.get("$BASE_URL/forums/game/$gameId/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            emptyList()
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

    suspend fun obtenerWikiPorJuego(gameId: Int, token: String): List<WikiOut> {
        return try {
            client.get("$BASE_URL/wiki/game/$gameId/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerBuildsPorJuego(gameId: Int, token: String): List<BuildOut> {
        return try {
            client.get("$BASE_URL/builds/game/$gameId/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerLogrosPorJuego(gameId: Int, token: String): List<AchievementOut> {
        return try {
            client.get("$BASE_URL/achievements/game/$gameId/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun obtenerPerfil(token: String): UserOut? {
        return try {
            client.get("$BASE_URL/users/me/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun crearJuego(game: GameIn, token: String): GameOut {
        return client.post("$BASE_URL/games/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(game)
        }.body()
    }

    suspend fun crearWiki(wiki: WikiIn, token: String): WikiOut {
        return client.post("$BASE_URL/wiki/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(wiki)
        }.body()
    }

    suspend fun crearBuild(build: BuildIn, token: String): BuildOut {
        return client.post("$BASE_URL/builds/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(build)
        }.body()
    }

    suspend fun subirImagenJuego(gameId: Int, image: PickedImageUpload, token: String): GameOut {
        return client.post("$BASE_URL/games/$gameId/image/") {
            bearerAuth(token)
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append(
                            key = "file",
                            value = image.bytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, image.contentType)
                                append(HttpHeaders.ContentDisposition, "filename=\"${image.fileName}\"")
                            }
                        )
                    }
                )
            )
        }.body()
    }

    suspend fun crearLogro(achievement: AchievementIn, token: String): AchievementOut {
        return client.post("$BASE_URL/achievements/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(achievement)
        }.body()
    }
}
