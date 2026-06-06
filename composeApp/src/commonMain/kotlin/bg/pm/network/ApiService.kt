package bg.pm.network

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
        expectSuccess = true
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

    suspend fun obtenerFavoritos(token: String): List<GameOut> {
        return client.get("$BASE_URL/games/favorites/") {
            bearerAuth(token)
        }.body()
    }

    suspend fun darLike(gameId: Int, token: String) {
        client.post("$BASE_URL/games/$gameId/favorite/") {
            bearerAuth(token)
        }
    }

    suspend fun quitarLike(gameId: Int, token: String) {
        client.delete("$BASE_URL/games/$gameId/favorite/") {
            bearerAuth(token)
        }
    }

    suspend fun eliminarJuego(gameId: Int, token: String) {
        client.delete("$BASE_URL/games/$gameId/") {
            bearerAuth(token)
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

    suspend fun crearForo(forum: ForumIn, token: String): ForumOut {
        return client.post("$BASE_URL/forums/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(forum)
        }.body()
    }

    suspend fun eliminarForo(forumId: Int, token: String) {
        client.delete("$BASE_URL/forums/$forumId/") { bearerAuth(token) }
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

    suspend fun obtenerMisLogros(token: String): List<AchievementOut> {
        return try {
            client.get("$BASE_URL/achievements/me/") {
                bearerAuth(token)
            }.body()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun marcarLogroObtenido(achievementId: Int, token: String) {
        client.post("$BASE_URL/achievements/me/$achievementId/") {
            bearerAuth(token)
        }
    }

    suspend fun desmarcarLogroObtenido(achievementId: Int, token: String) {
        client.delete("$BASE_URL/achievements/me/$achievementId/") {
            bearerAuth(token)
        }
    }

    suspend fun obtenerMisBuilds(token: String): List<BuildOut> {
        return try {
            client.get("$BASE_URL/builds/me/") {
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

    suspend fun eliminarCuenta(username: String, token: String) {
        client.delete("$BASE_URL/users/$username/") {
            bearerAuth(token)
        }
    }

    suspend fun actualizarPerfil(update: UserUpdate, token: String): UserOut {
        return client.put("$BASE_URL/users/me/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(update)
        }.body()
    }

    suspend fun subirImagenPerfil(image: PickedImageUpload, token: String): UserOut {
        return client.post("$BASE_URL/users/me/image/") {
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

    suspend fun subirBannerPerfil(image: PickedImageUpload, token: String): UserOut {
        return client.post("$BASE_URL/users/me/banner/") {
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

    // ── Discussions ──────────────────────────────────────────────────────

    suspend fun obtenerDiscusionesPorForo(forumId: Int, token: String): List<DiscussionOut> {
        return try {
            client.get("$BASE_URL/discussions/forum/$forumId/") { bearerAuth(token) }.body()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun crearDiscusion(discussion: DiscussionIn, token: String): DiscussionOut {
        return client.post("$BASE_URL/discussions/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(discussion)
        }.body()
    }

    suspend fun subirImagenDiscusion(
        discussionId: Int,
        image: PickedImageUpload,
        token: String,
    ): DiscussionOut {
        return client.post("$BASE_URL/discussions/$discussionId/image/") {
            bearerAuth(token)
            setBody(MultiPartFormDataContent(formData {
                append("file", image.bytes, Headers.build {
                    append(HttpHeaders.ContentType, image.contentType)
                    append(HttpHeaders.ContentDisposition, "filename=\"${image.fileName}\"")
                })
            }))
        }.body()
    }

    suspend fun eliminarDiscusion(discussionId: Int, token: String) {
        client.delete("$BASE_URL/discussions/$discussionId/") { bearerAuth(token) }
    }

    // ── Votes ────────────────────────────────────────────────────────────

    suspend fun votar(discussionId: Int, vote: Int, token: String): VoteResponse {
        return client.post("$BASE_URL/discussions/$discussionId/vote/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(PostVoteIn(vote = vote))
        }.body()
    }

    suspend fun obtenerMiVoto(discussionId: Int, token: String): VoteResponse {
        return try {
            client.get("$BASE_URL/discussions/$discussionId/vote/") { bearerAuth(token) }.body()
        } catch (_: Exception) { VoteResponse(my_vote = 0, likes = 0, dislikes = 0) }
    }

    // ── Replies ──────────────────────────────────────────────────────────

    suspend fun obtenerRespuestas(discussionId: Int, token: String): List<PostReplyOut> {
        return try {
            client.get("$BASE_URL/discussions/$discussionId/replies/") { bearerAuth(token) }.body()
        } catch (_: Exception) { emptyList() }
    }

    suspend fun crearRespuesta(
        discussionId: Int,
        reply: PostReplyIn,
        token: String,
    ): PostReplyOut {
        return client.post("$BASE_URL/discussions/$discussionId/replies/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(reply)
        }.body()
    }

    suspend fun subirImagenRespuesta(
        discussionId: Int,
        replyId: Int,
        image: PickedImageUpload,
        token: String,
    ): PostReplyOut {
        return client.post("$BASE_URL/discussions/$discussionId/replies/$replyId/image/") {
            bearerAuth(token)
            setBody(MultiPartFormDataContent(formData {
                append("file", image.bytes, Headers.build {
                    append(HttpHeaders.ContentType, image.contentType)
                    append(HttpHeaders.ContentDisposition, "filename=\"${image.fileName}\"")
                })
            }))
        }.body()
    }

    suspend fun eliminarRespuesta(discussionId: Int, replyId: Int, token: String) {
        client.delete("$BASE_URL/discussions/$discussionId/replies/$replyId/") {
            bearerAuth(token)
        }
    }

    // ── Comment votes ────────────────────────────────────────────────────────

    suspend fun votarComentario(discussionId: Int, replyId: Int, vote: Int, token: String): VoteResponse {
        return client.post("$BASE_URL/discussions/$discussionId/replies/$replyId/vote/") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(PostVoteIn(vote = vote))
        }.body()
    }

    suspend fun obtenerMiVotoComentario(discussionId: Int, replyId: Int, token: String): VoteResponse {
        return try {
            client.get("$BASE_URL/discussions/$discussionId/replies/$replyId/vote/") {
                bearerAuth(token)
            }.body()
        } catch (_: Exception) { VoteResponse(my_vote = 0, likes = 0, dislikes = 0) }
    }
}
