package bg.pm.ui

import androidx.compose.runtime.compositionLocalOf
import coil3.ImageLoader

val LocalAppImageLoader = compositionLocalOf<ImageLoader> {
    error("No ImageLoader provided. Wrap your composable with CompositionLocalProvider(LocalAppImageLoader provides ...)")
}
