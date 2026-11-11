package com.example.cmd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Pantalla de acceso demostrativa; el envío real de credenciales se conectará después.
@Composable
fun LoginScreen(onLogin: () -> Unit, modifier: Modifier = Modifier) {
    // Conserva el texto de correo mientras se escribe.
    var email by remember { mutableStateOf("") }

    // Conserva la contraseña escrita en el campo.
    var password by remember { mutableStateOf("") }

    // Controla si la contraseña se muestra o se oculta.
    var passwordVisible by remember { mutableStateOf(false) }

    // Centra el formulario y permite desplazarse en teléfonos con poca altura.
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Marca breve de CMD para identificar la aplicación.
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = .18f)
        ) {
            Text(
                "CMD",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }

        // Espacio entre la marca y el título de bienvenida.
        Spacer(Modifier.height(16.dp))

        // Título principal de la pantalla de acceso.
        Text(
            "Bienvenido",
            color = Color.White,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Explica en una frase qué debe hacer la persona.
        Text(
            "Inicia sesión para consultar tu cartilla familiar.",
            color = Color.White.copy(alpha = .9f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 22.dp)
        )

        // Tarjeta mate y ligeramente translúcida para mantener coherencia con Inicio.
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xD9E4F1F7)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color(0x70B8D3E2))
        ) {
            // Ordena los campos y acciones del formulario.
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo para escribir el correo electrónico.
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Correo electrónico") },
                    placeholder = { Text("nombre@correo.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF17466B),
                        unfocusedBorderColor = Color(0xFF9BB6C4),
                        focusedLabelColor = Color(0xFF17466B),
                        cursorColor = Color(0xFF17466B)
                    )
                )

                // Campo seguro para escribir la contraseña.
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        // Botón de texto para mostrar u ocultar la contraseña.
                        TextButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "Ocultar" else "Mostrar")
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF17466B),
                        unfocusedBorderColor = Color(0xFF9BB6C4),
                        focusedLabelColor = Color(0xFF17466B),
                        cursorColor = Color(0xFF17466B)
                    )
                )

                // Enlace visual de recuperación; su flujo se añadirá más adelante.
                TextButton(
                    onClick = { },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("¿Olvidaste tu contraseña?", color = Color(0xFF17466B), fontSize = 12.sp)
                }

                // Botón principal; por ahora solo permite previsualizar la pantalla de inicio.
                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF17466B),
                        contentColor = Color.White
                    )
                ) {
                    // Texto claro de la acción principal.
                    Text("Iniciar sesión", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Aviso breve para dejar claro que este formulario es una maqueta.
        Text(
            "Borrador visual · El acceso aún no está conectado",
            color = Color.White.copy(alpha = .85f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
