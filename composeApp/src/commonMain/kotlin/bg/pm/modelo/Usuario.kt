package bg.pm.modelo
import kotlinx.serialization.Serializable

@Serializable
class Usuario {
    var id: String = ""
    var name: String = ""
    var username: String = ""
    var email: String = ""
    var password: String = ""
    var image: String = ""
    var role: String = ""


}