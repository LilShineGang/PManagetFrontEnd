package bg.pm.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginAdministrador(
    viewModel: LoginViewModel,
    onLoginExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val contrasena by viewModel.contrasena.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Acceso Administrador",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Introduce tus credenciales para continuar",
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = usuario,
            onValueChange = { viewModel.onUsuarioChange(it) },
            label = { Text("Usuario") },
            placeholder = { Text("Introduce tu usuario") },
            isError = mensajeError != null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = contrasena,
            onValueChange = { viewModel.onContrasenaChange(it) },
            label = { Text("Contraseña") },
            placeholder = { Text("Introduce tu contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = mensajeError != null,
            supportingText = {
                mensajeError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 24.dp),
            singleLine = true
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp)
            ) {
                Text("Cancelar", fontSize = 18.sp)
            }

            Button(
                onClick = {
                    viewModel.validar(onLoginExitoso)
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp),
                enabled = usuario.isNotBlank() && contrasena.isNotBlank()
            ) {
                Text("Entrar", fontSize = 18.sp)
            }
        }
    }
}
