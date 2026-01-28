package bg.pm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform