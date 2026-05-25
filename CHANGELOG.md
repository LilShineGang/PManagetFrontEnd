# Historial de cambios — Frontend (PManagetFrontEnd)

| Hash | Commit | Descripción |
|------|--------|-------------|
| `bf86c0c` | Add Juegos nav section, favorites on home, like and delete buttons on game cards | Añade la pestaña Juegos en la barra de navegación; la página de inicio muestra solo favoritos; botones de corazón y papelera en cada tarjeta de juego. |
| `55c193b` | Add game favorites state, toggle like and delete game to ViewModel and ApiService | Añade los flujos `likedGameIds`/`juegosLiked`, `toggleLike` con actualización optimista y `eliminarJuego` con limpieza local de listas. |
| `15c717b` | Fix Android image upload filename extension and network security config | Deduce la extensión del archivo a partir del tipo MIME cuando falta; añade hosts con tráfico en claro a `network_security_config.xml`. |
| `3a75e7f` | fix: repair App.kt and PantallaPrincipal after broken merge | Restaura `NavigationRail`/`NavigationBar` y el layout `BoxWithConstraints` perdidos al resolver un conflicto de merge. |
| `63e9c13` | fix: resolve merge conflict keeping full profile/banner UI | Conserva la interfaz completa de edición de perfil (avatar + banner) tras un merge con conflictos. |
| `a0f91bb` | feat: add banner field to persist profile background | Añade el campo `banner` a `UserOut`; lo envía al actualizar el perfil y lo muestra como fondo en la pantalla de perfil. |
| `54fcae8` | feat: adapt frontend to backend API | Alinea modelos, formulario de login, gestión de sesión JWT y navegación con la API FastAPI. |
| `4310368` | Login view | Implementa la pantalla de inicio de sesión con validación de formulario. |
| `d520632` | Updated packages for Login, gradle.build and added serialization alias | Añade dependencias de Ktor + kotlinx-serialization y configura Gradle para KMP. |
| `1793fa4` | Login first contact | Primer borrador del composable de pantalla de login. |
| `c63bdfa` | First version uploaded | Estructura inicial del proyecto Compose Multiplatform. |
