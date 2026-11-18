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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

// Define un campo que puede ser de texto o una lista de opciones.
data class CampoRegistro(
    val etiqueta: String,
    val opciones: List<String> = emptyList(),
    val requerido: Boolean = true,
    val unaLinea: Boolean = true,
    val esFecha: Boolean = false
)

// Centraliza los campos reutilizables; cada pantalla elige el formulario que necesita.
object FormulariosRegistro {
    val familiar = listOf(
        CampoRegistro("Nombre completo"),
        CampoRegistro("Parentesco", listOf("Madre", "Padre", "Hijo", "Hija", "Pareja", "Otro")),
        CampoRegistro("Fecha de nacimiento", requerido = false, esFecha = true),
        CampoRegistro("Tipo de sangre", listOf("A positivo (A+)", "A negativo (A-)", "B positivo (B+)", "B negativo (B-)", "AB positivo (AB+)", "AB negativo (AB-)", "O positivo (O+)", "O negativo (O-)"), requerido = false),
        CampoRegistro("Alergias", requerido = false, unaLinea = false)
    )
    // Devuelve solo los campos necesarios para registrar cada tipo de evento médico.
    fun historialPorSubmodulo(submodulo: String): List<CampoRegistro> = when (submodulo) {
        "Accidentes" -> listOf(
            CampoRegistro("Fecha del accidente", esFecha = true),
            CampoRegistro("Qué ocurrió", unaLinea = false),
            CampoRegistro("Lesión o diagnóstico", requerido = false, unaLinea = false),
            CampoRegistro("Atención recibida", requerido = false, unaLinea = false)
        )
        "Cirugías" -> listOf(
            CampoRegistro("Fecha de cirugía", esFecha = true),
            CampoRegistro("Procedimiento"),
            CampoRegistro("Diagnóstico o motivo", unaLinea = false),
            CampoRegistro("Médico cirujano", requerido = false),
            CampoRegistro("Hospital", requerido = false),
            CampoRegistro("Complicaciones", requerido = false, unaLinea = false)
        )
        "Consultas" -> listOf(
            CampoRegistro("Fecha de consulta", esFecha = true),
            CampoRegistro("Especialidad", listOf("Medicina General", "Pediatría", "Ortopedia", "Cardiología")),
            CampoRegistro("Motivo de consulta", unaLinea = false),
            CampoRegistro("Diagnóstico", requerido = false, unaLinea = false),
            CampoRegistro("Médico tratante", requerido = false)
        )
        "Enfermedades" -> listOf(
            CampoRegistro("Nombre de la enfermedad"),
            CampoRegistro("Fecha del diagnóstico", esFecha = true),
            CampoRegistro("Estado", listOf("Activa", "Controlada", "Resuelta")),
            CampoRegistro("Tratamiento actual", requerido = false, unaLinea = false),
            CampoRegistro("Notas", requerido = false, unaLinea = false)
        )
        "Hospitalizaciones" -> listOf(
            CampoRegistro("Hospital"),
            CampoRegistro("Fecha de ingreso", esFecha = true),
            CampoRegistro("Fecha de alta", requerido = false, esFecha = true),
            CampoRegistro("Motivo de hospitalización", unaLinea = false),
            CampoRegistro("Diagnóstico", unaLinea = false),
            CampoRegistro("Tratamiento o procedimiento", requerido = false, unaLinea = false),
            CampoRegistro("Médico responsable", requerido = false)
        )
        else -> emptyList()
    }
    val vacuna = listOf(
        CampoRegistro("Vacuna", listOf("Influenza", "COVID-19", "Hepatitis B", "Tétanos", "Otra")),
        CampoRegistro("Fecha de aplicación", esFecha = true),
        CampoRegistro("Dosis", listOf("Primera dosis", "Segunda dosis", "Tercera dosis", "Refuerzo"), requerido = false),
        CampoRegistro("Lote o centro de salud", requerido = false)
    )
    val receta = listOf(
        CampoRegistro("Nombre del medicamento"), CampoRegistro("Dosis"),
        CampoRegistro("Frecuencia", listOf("Una vez al día", "Cada 12 horas", "Cada 8 horas", "Cada 6 horas", "Según indicación médica")),
        CampoRegistro("Fecha de inicio", requerido = false, esFecha = true),
        CampoRegistro("Indicaciones", requerido = false, unaLinea = false)
    )
    val documento = listOf(
        CampoRegistro("Nombre del documento"),
        CampoRegistro("Tipo", listOf("Estudio", "Imagen", "Alta médica", "Reporte", "Otro")),
        CampoRegistro("Fecha", esFecha = true), CampoRegistro("Nota", requerido = false, unaLinea = false)
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
                when {
                    campo.esFecha -> CampoFechaRegistro(
                        etiqueta = campo.etiqueta,
                        valor = valores[campo.etiqueta].orEmpty(),
                        onFechaSeleccionada = { valores[campo.etiqueta] = it },
                        soloLectura = soloLectura,
                        colores = coloresCampos
                    )
                    campo.opciones.isEmpty() -> {
                    OutlinedTextField(
                        value = valores[campo.etiqueta].orEmpty(),
                        onValueChange = { nuevoValor -> valores[campo.etiqueta] = nuevoValor },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(campo.etiqueta) },
                        singleLine = campo.unaLinea,
                        readOnly = soloLectura,
                        colors = coloresCampos
                    )
                    }
                    else -> {
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

// Selector de calendario que guarda la fecha en formato ISO y evita escribirla manualmente.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CampoFechaRegistro(
    etiqueta: String,
    valor: String,
    onFechaSeleccionada: (String) -> Unit,
    soloLectura: Boolean,
    colores: androidx.compose.material3.TextFieldColors
) {
    var calendarioAbierto by remember(etiqueta) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = mostrarFechaLocalizada(valor),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(etiqueta) },
            readOnly = true,
            trailingIcon = { Text("▦", color = Color.White) },
            colors = colores
        )
        Box(
            modifier = Modifier.matchParentSize().clickable(enabled = !soloLectura) {
                calendarioAbierto = true
            }
        )
    }

    if (calendarioAbierto) {
        val estadoCalendario = rememberDatePickerState(
            initialSelectedDateMillis = fechaIsoAMillisUtc(valor)
        )
        DatePickerDialog(
            onDismissRequest = { calendarioAbierto = false },
            confirmButton = {
                TextButton(
                    enabled = estadoCalendario.selectedDateMillis != null,
                    onClick = {
                        estadoCalendario.selectedDateMillis?.let { onFechaSeleccionada(fechaMillisAIsoUtc(it)) }
                        calendarioAbierto = false
                    }
                ) { Text("Seleccionar", color = Color(0xFF17466B)) }
            },
            dismissButton = {
                TextButton(onClick = { calendarioAbierto = false }) {
                    Text("Cancelar", color = Color(0xFF17466B))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF32B6AD),
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color(0xFF17466B),
                dayContentColor = Color(0xFF263746),
                selectedDayContainerColor = Color(0xFF17466B),
                selectedDayContentColor = Color.White,
                todayDateBorderColor = Color(0xFF17466B),
                todayContentColor = Color(0xFF17466B)
            )
        ) {
            DatePicker(state = estadoCalendario, showModeToggle = false)
        }
    }
}

// Muestra las fechas en formato día/mes/año, conservando el valor ISO para el registro.
private fun mostrarFechaLocalizada(fechaIso: String): String {
    val partes = fechaIso.split("-")
    return if (partes.size == 3) "${partes[2]}/${partes[1]}/${partes[0]}" else fechaIso
}

// Convierte la fecha ISO a milisegundos UTC para inicializar el calendario.
private fun fechaIsoAMillisUtc(fechaIso: String): Long? = runCatching {
    val (anio, mes, dia) = fechaIso.split("-").map(String::toInt)
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(anio, mes - 1, dia)
    }.timeInMillis
}.getOrNull()

// Convierte la selección del calendario al formato AAAA-MM-DD.
private fun fechaMillisAIsoUtc(millis: Long): String {
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millis }
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        calendario.get(Calendar.YEAR),
        calendario.get(Calendar.MONTH) + 1,
        calendario.get(Calendar.DAY_OF_MONTH)
    )
}

// Crea un destino privado para la foto y expone su URI de forma segura a la cámara.
internal fun crearUriFoto(context: Context): Uri {
    val directorioFotos = File(context.filesDir, "captured_images").apply { mkdirs() }
    val archivoFoto = File.createTempFile("cmd_", ".jpg", directorioFotos)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivoFoto
    )
}
