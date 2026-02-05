package bg.pm.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiService {
    private const val BASE_URL = "http://localhost:8000"
    
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
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(login)
            }.body()
        } catch (e: Exception) {
            throw Exception("Error en el login: ${e.message}")
        }
    }
}
