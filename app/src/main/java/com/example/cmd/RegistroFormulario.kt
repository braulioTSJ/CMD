package com.example.cmd

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import java.io.File

// Define un campo que puede ser de texto o una lista de opciones.
data class CampoRegistro(
    val etiqueta: String,
    val opciones: List<String> = emptyList(),
    val requerido: Boolean = true,
    val unaLinea: Boolean = true
)

// Centraliza los campos reutilizables; cada pantalla elige el formulario que necesita.
object FormulariosRegistro {
    val familiar = listOf(
        CampoRegistro("Nombre completo"),
        CampoRegistro("Parentesco", listOf("Madre", "Padre", "Hijo", "Hija", "Pareja", "Otro")),
        CampoRegistro("Fecha de nacimiento", requerido = false),
        CampoRegistro("Tipo de sangre", listOf("A positivo (A+)", "A negativo (A-)", "B positivo (B+)", "B negativo (B-)", "AB positivo (AB+)", "AB negativo (AB-)", "O positivo (O+)", "O negativo (O-)"), requerido = false),
        CampoRegistro("Alergias", requerido = false, unaLinea = false)
    )
    val historial = listOf(
        CampoRegistro("Fecha"), CampoRegistro("Descripción", requerido = false, unaLinea = false),
        CampoRegistro("Diagnóstico", requerido = false), CampoRegistro("Médico o institución", requerido = false)
    )
    val vacuna = listOf(
        CampoRegistro("Vacuna", listOf("Influenza", "COVID-19", "Hepatitis B", "Tétanos", "Otra")),
        CampoRegistro("Fecha de aplicación"),
        CampoRegistro("Dosis", listOf("Primera dosis", "Segunda dosis", "Tercera dosis", "Refuerzo"), requerido = false),
        CampoRegistro("Lote o centro de salud", requerido = false)
    )
    val receta = listOf(
        CampoRegistro("Nombre del medicamento"), CampoRegistro("Dosis"),
        CampoRegistro("Frecuencia", listOf("Una vez al día", "Cada 12 horas", "Cada 8 horas", "Cada 6 horas", "Según indicación médica")),
        CampoRegistro("Fecha de inicio", requerido = false),
        CampoRegistro("Indicaciones", requerido = false, unaLinea = false)
    )
    val documento = listOf(
        CampoRegistro("Nombre del documento"),
        CampoRegistro("Tipo", listOf("Estudio", "Imagen", "Alta médica", "Reporte", "Otro")),
        CampoRegistro("Fecha"), CampoRegistro("Nota", requerido = false, unaLinea = false)
    )
}

// Conserva los registros del prototipo entre pestañas mientras la app siga abierta.
object RegistroLocalStore {
    private val registrosPorModulo = mutableStateMapOf<String, List<Map<String, String>>>()

    fun agregar(modulo: String, datos: Map<String, String>) {
        registrosPorModulo[modulo] = registrosPorModulo[modulo].orEmpty() + datos
    }

    fun obtener(modulo: String): List<Map<String, String>> = registrosPorModulo[modulo].orEmpty()
}

// Desplegable reutilizable para registrar o consultar datos de distintos módulos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroFormularioSheet(
    titulo: String,
    campos: List<CampoRegistro>,
    onCerrar: () -> Unit,
    onGuardar: (Map<String, String>) -> Unit = {},
    valoresIniciales: Map<String, String> = emptyMap(),
    soloLectura: Boolean = false,
    permitirFoto: Boolean = false
) {
    // Usa campos blancos sobre un fondo turquesa, como la tarjeta de próxima cita.
    val coloresCampos = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedBorderColor = Color.White,
        unfocusedBorderColor = Color.White.copy(alpha = .7f),
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color.White.copy(alpha = .85f),
        cursorColor = Color.White,
        focusedTrailingIconColor = Color.White,
        unfocusedTrailingIconColor = Color.White
    )

    val valores = remember(titulo, campos, valoresIniciales) {
        mutableStateMapOf<String, String>().apply {
            campos.forEach { campo ->
                put(campo.etiqueta, valoresIniciales[campo.etiqueta].orEmpty())
            }
        }
    }
    val context = LocalContext.current
    var uriPendiente by remember(titulo) { mutableStateOf<Uri?>(null) }
    var fotoUri by remember(titulo, valoresIniciales) {
        mutableStateOf(valoresIniciales["Fotografía"]?.let(Uri::parse))
    }
    var errorCamara by remember(titulo) { mutableStateOf(false) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        if (guardada) fotoUri = uriPendiente
        errorCamara = false
    }

    val formularioValido = campos
        .filter { it.requerido }
        .all { valores[it.etiqueta].orEmpty().isNotBlank() }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = Color(0xFF32B6AD),
        contentColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título del formulario o ficha de consulta.
            Text(titulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            campos.forEach { campo ->
                if (campo.opciones.isEmpty()) {
                    OutlinedTextField(
                        value = valores[campo.etiqueta].orEmpty(),
                        onValueChange = { nuevoValor -> valores[campo.etiqueta] = nuevoValor },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(campo.etiqueta) },
                        singleLine = campo.unaLinea,
                        readOnly = soloLectura,
                        colors = coloresCampos
                    )
                } else {
                    var desplegableAbierto by remember(campo.etiqueta) { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = valores[campo.etiqueta].orEmpty(),
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(campo.etiqueta) },
                            readOnly = true,
                            trailingIcon = { Text("⌄", color = Color.White) },
                            colors = coloresCampos
                        )

                        // Captura el toque sobre toda la caja, porque el campo es solo de selección.
                        Box(
                            modifier = Modifier.matchParentSize().clickable(enabled = !soloLectura) {
                                desplegableAbierto = true
                            }
                        )

                        DropdownMenu(
                            expanded = desplegableAbierto,
                            onDismissRequest = { desplegableAbierto = false },
                            containerColor = Color(0xFF32B6AD)
                        ) {
                            campo.opciones.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(opcion, color = Color.White) },
                                    onClick = {
                                        valores[campo.etiqueta] = opcion
                                        desplegableAbierto = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (permitirFoto && !soloLectura) {
                OutlinedButton(
                    onClick = {
                        runCatching { crearUriFoto(context) }
                            .onSuccess { uri ->
                                uriPendiente = uri
                                runCatching { camara.launch(uri) }
                                    .onFailure { errorCamara = true }
                            }
                            .onFailure { errorCamara = true }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = .8f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text(if (fotoUri == null) "Tomar foto" else "Cambiar foto")
                }
                if (fotoUri != null) Text("Foto adjunta al registro", color = Color.White)
                if (errorCamara) Text("No se pudo abrir la cámara", color = Color.White)
            }

            Spacer(Modifier.height(4.dp))

            if (soloLectura) {
                Button(
                    onClick = onCerrar,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF17466B),
                        contentColor = Color.White
                    )
                ) {
                    Text("Cerrar")
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onCerrar,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val datosConFoto = valores.toMap().toMutableMap()
                            fotoUri?.let { datosConFoto["Fotografía"] = it.toString() }
                            onGuardar(datosConFoto)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = formularioValido,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF17466B),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF6A969D),
                            disabledContentColor = Color.White.copy(alpha = .75f)
                        )
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

// Crea un destino privado para la foto y expone su URI de forma segura a la cámara.
private fun crearUriFoto(context: Context): Uri {
    val directorioFotos = File(context.filesDir, "captured_images").apply { mkdirs() }
    val archivoFoto = File.createTempFile("cmd_", ".jpg", directorioFotos)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivoFoto
    )
}
