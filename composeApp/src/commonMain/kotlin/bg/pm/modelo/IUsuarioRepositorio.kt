package bg.pm.modelo

interface IUsuarioRepositorio {
    suspend fun add(item:Usuario):Unit
    suspend fun remove(item:Usuario): Boolean
    suspend fun remove(id:String): Boolean
    suspend fun update(item:Usuario): Boolean
    suspend fun getAll():List<Usuario>
    suspend fun findByName(name:String): Usuario?
    suspend fun getById(id:String):Usuario?
}