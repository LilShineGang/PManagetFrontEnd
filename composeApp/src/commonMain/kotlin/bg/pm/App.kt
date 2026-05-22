package bg.pm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import bg.pm.navigation.NavGraph
import bg.pm.ui.common.LocalAppImageLoader
import bg.pm.ui.login.LoginViewModel
import bg.pm.ui.login.RegisterViewModel
import bg.pm.ui.theme.GlyphTheme
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory

@Composable
fun App() {
    val platformContext = LocalPlatformContext.current
    val imageLoader = remember(platformContext) {
        ImageLoader.Builder(platformContext)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
    CompositionLocalProvider(LocalAppImageLoader provides imageLoader) {
        GlyphTheme {
            val navController = rememberNavController()
            val loginViewModel = remember { LoginViewModel() }
            val registerViewModel = remember { RegisterViewModel() }

            NavGraph(
                navController = navController,
                loginViewModel = loginViewModel,
                registerViewModel = registerViewModel,
            )
        }
    }
}

