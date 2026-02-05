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
fun RegisterUsuario(
    viewModel: RegisterViewModel,
    onRegistroExitoso: () -> Unit,
    onCancelar: () -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val email by viewModel.email.collectAsState()
    val contrasena by viewModel.contrasena.collectAsState()
    val confirmarContrasena by viewModel.confirmarContrasena.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crear Cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Completa los datos para registrarte",
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
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            label = { Text("Email") },
            placeholder = { Text("Introduce tu email") },
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
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .padding(bottom = 16.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = confirmarContrasena,
            onValueChange = { viewModel.onConfirmarContrasenaChange(it) },
            label = { Text("Confirmar Contraseña") },
            placeholder = { Text("Confirma tu contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            isError = mensajeError != null,
            supportingText = {
                if (mensajeError != null) {
                    Text(
                        text = mensajeError!!,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (mensajeExito != null) {
                    Text(
                        text = mensajeExito!!,
                        color = MaterialTheme.colorScheme.primary
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
                    viewModel.registrar(onRegistroExitoso)
                },
                modifier = Modifier
                    .width(150.dp)
                    .height(56.dp),
                enabled = usuario.isNotBlank() && email.isNotBlank() && contrasena.isNotBlank() && confirmarContrasena.isNotBlank()
            ) {
                Text("Registrar", fontSize = 18.sp)
            }
        }
    }
}
