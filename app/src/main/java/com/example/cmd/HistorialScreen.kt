package com.example.cmd

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val HistorialNavy = Color(0xFF17466B)
private val HistorialInk = Color(0xFF263746)
private val HistorialCardSurface = Color(0xC8E4F1F7)
private val HistorialCardBorder = Color(0x70B8D3E2)
private val HistorialSoftMint = Color(0xBAE8F5F2)
private val HistorialTealTranslucido = Color(0xD832B6AD)

private data class SubmoduloHistorial(val nombre: String, val icono: Int)

private val SubmodulosHistorial = listOf(
    SubmoduloHistorial("Eventos", R.drawable.accidente),
    SubmoduloHistorial("Cirugías", R.drawable.bisturi),
    SubmoduloHistorial("Consultas", R.drawable.estetoscopio),
    SubmoduloHistorial("Enfermedades", R.drawable.enfermedades),
    SubmoduloHistorial("Hospitalizaciones", R.drawable.hospitalizacion)
)

// Pantalla del historial médico con formularios y operaciones independientes por submódulo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    selectedUserName: String,
    colorTarjetaPerfil: Color,
    selectedProfileId: String = selectedUserName,
    selectedProfileBirthDate: String? = null,
    modifier: Modifier = Modifier
) {
    var submoduloAbierto by remember { mutableStateOf<String?>(null) }
    var formularioAbierto by remember { mutableStateOf<String?>(null) }
    var registroEditando by remember { mutableStateOf<RegistroHistorialBase?>(null) }
    var registroConsultado by remember { mutableStateOf<RegistroHistorialBase?>(null) }
    var registroEliminando by remember { mutableStateOf<RegistroHistorialBase?>(null) }
    var fotoVisibleEnConsulta by remember { mutableStateOf(false) }
    val metodos = remember { MetodosHistorial() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { EncabezadoHistorial() }
        item { TarjetaPerfilSeleccionado(selectedUserName, colorTarjetaPerfil) }
        items(SubmodulosHistorial) { submodulo ->
            TarjetaSubmodulo(
                submodulo = submodulo,
                onAdd = {
                    registroEditando = null
                    formularioAbierto = submodulo.nombre
                },
                onClick = { submoduloAbierto = submodulo.nombre }
            )
        }
    }

    submoduloAbierto?.let { modulo ->
        HojaRegistrosHistorial(
            nombreSubmodulo = modulo,
            registros = metodos.consultarPorSubmodulo(selectedProfileId, modulo),
            onCerrar = { submoduloAbierto = null },
            onConsultar = { registroConsultado = it; fotoVisibleEnConsulta = false },
            onEditar = { registroEditando = it; formularioAbierto = modulo; submoduloAbierto = null },
            onEliminar = { registroEliminando = it }
        )
    }

    formularioAbierto?.let { modulo ->
        when (modulo) {
            "Eventos" -> FormularioAccidente(selectedUserName, registroEditando as? AccidenteRegistro, { cerrarFormulario { formularioAbierto = null; registroEditando = null } }, fechaNacimientoPerfil = selectedProfileBirthDate, perfilId = selectedProfileId) { registro ->
                if (registroEditando == null) metodos.agregarAccidente(registro) else metodos.actualizarAccidente(registro)
                formularioAbierto = null; registroEditando = null
            }
            "Cirugías" -> FormularioCirugia(selectedUserName, registroEditando as? CirugiaRegistro, { formularioAbierto = null; registroEditando = null }, fechaNacimientoPerfil = selectedProfileBirthDate, perfilId = selectedProfileId) { registro ->
                if (registroEditando == null) metodos.agregarCirugia(registro) else metodos.actualizarCirugia(registro)
                formularioAbierto = null; registroEditando = null
            }
            "Consultas" -> FormularioConsulta(selectedUserName, registroEditando as? ConsultaRegistro, { formularioAbierto = null; registroEditando = null }, fechaNacimientoPerfil = selectedProfileBirthDate, perfilId = selectedProfileId) { registro ->
                if (registroEditando == null) metodos.agregarConsulta(registro) else metodos.actualizarConsulta(registro)
                formularioAbierto = null; registroEditando = null
            }
            "Enfermedades" -> FormularioEnfermedad(selectedUserName, registroEditando as? EnfermedadRegistro, { formularioAbierto = null; registroEditando = null }, fechaNacimientoPerfil = selectedProfileBirthDate, perfilId = selectedProfileId) { registro ->
                if (registroEditando == null) metodos.agregarEnfermedad(registro) else metodos.actualizarEnfermedad(registro)
                formularioAbierto = null; registroEditando = null
            }
            "Hospitalizaciones" -> FormularioHospitalizacion(selectedUserName, registroEditando as? HospitalizacionRegistro, { formularioAbierto = null; registroEditando = null }, fechaNacimientoPerfil = selectedProfileBirthDate, perfilId = selectedProfileId) { registro ->
                if (registroEditando == null) metodos.agregarHospitalizacion(registro) else metodos.actualizarHospitalizacion(registro)
                formularioAbierto = null; registroEditando = null
            }
        }
    }

    registroConsultado?.let { registro ->
        DialogoDetalleHistorial(
            registro = registro,
            mostrarFoto = fotoVisibleEnConsulta,
            onAlternarFoto = { fotoVisibleEnConsulta = !fotoVisibleEnConsulta },
            onCerrar = { registroConsultado = null; fotoVisibleEnConsulta = false }
        )
    }

    registroEliminando?.let { registro ->
        AlertDialog(
            onDismissRequest = { registroEliminando = null },
            title = { Text("Eliminar registro") },
            text = { Text("¿Quieres eliminar este registro de ${nombreSubmodulo(registro).lowercase()}?") },
            confirmButton = {
                TextButton(onClick = {
                    metodos.eliminar(registro)
                    registroEliminando = null
                }) { Text("Eliminar", color = Color(0xFFB3261E)) }
            },
            dismissButton = { TextButton(onClick = { registroEliminando = null }) { Text("Cancelar") } }
        )
    }
}

// Cierra el formulario activo y limpia el registro que se estaba editando.
private fun cerrarFormulario(cerrar: () -> Unit) = cerrar()

// Devuelve el nombre legible del submódulo del registro para el mensaje de confirmación.
private fun nombreSubmodulo(registro: RegistroHistorialBase): String = when (registro) {
    is AccidenteRegistro -> "Eventos"
    is CirugiaRegistro -> "Cirugías"
    is ConsultaRegistro -> "Consultas"
    is EnfermedadRegistro -> "Enfermedades"
    is HospitalizacionRegistro -> "Hospitalizaciones"
    else -> "Historial"
}

// Título de la pantalla y una breve explicación del contenido.
@Composable
private fun EncabezadoHistorial() {
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Historial médico", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Consultas y antecedentes de salud", color = Color.White.copy(alpha = .9f), fontSize = 15.sp, textAlign = TextAlign.Center)
    }
}

// Tarjeta para abrir el listado del submódulo o crear un registro nuevo.
@Composable
private fun TarjetaSubmodulo(submodulo: SubmoduloHistorial, onAdd: () -> Unit, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Card(
            modifier = Modifier.weight(1f).height(74.dp).clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HistorialCardSurface),
            elevation = CardDefaults.cardElevation(0.dp),
            border = BorderStroke(1.dp, HistorialCardBorder)
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(HistorialSoftMint), contentAlignment = Alignment.Center) {
                    Image(painterResource(submodulo.icono), submodulo.nombre, Modifier.size(26.dp), contentScale = ContentScale.Fit, colorFilter = ColorFilter.tint(HistorialNavy))
                }
                Spacer(Modifier.width(12.dp))
                Text(submodulo.nombre, color = HistorialInk, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
        }
        Button(
            onClick = onAdd,
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, HistorialCardBorder),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HistorialCardSurface, contentColor = HistorialNavy)
        ) { Text("+", fontSize = 24.sp, fontWeight = FontWeight.Medium) }
    }
}

// Hoja común que lista los registros del submódulo seleccionado.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaRegistrosHistorial(
    nombreSubmodulo: String,
    registros: List<RegistroHistorialBase>,
    onCerrar: () -> Unit,
    onConsultar: (RegistroHistorialBase) -> Unit,
    onEditar: (RegistroHistorialBase) -> Unit,
    onEliminar: (RegistroHistorialBase) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onCerrar, containerColor = HistorialTealTranslucido, contentColor = Color.White, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(nombreSubmodulo, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (registros.isEmpty()) Text("Aún no hay registros en este submódulo.", modifier = Modifier.padding(vertical = 16.dp))
            else registros.forEach { registro -> TarjetaRegistroHistorial(registro, { onConsultar(registro) }, { onEditar(registro) }, { onEliminar(registro) }) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

// Método padre: muestra el resumen del registro y los botones comunes Consultar, Editar y Eliminar.
@Composable
private fun TarjetaRegistroHistorial(registro: RegistroHistorialBase, onConsultar: () -> Unit, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = HistorialCardSurface), border = BorderStroke(1.dp, HistorialCardBorder)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(registro.fechaRegistro, color = HistorialNavy, fontWeight = FontWeight.Bold)
            Text(registro.resumen, color = HistorialInk, maxLines = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onConsultar) { Text("Consultar") }
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(onClick = onEliminar) { Text("Eliminar", color = Color(0xFFB3261E)) }
            }
        }
    }
}

// Muestra los datos específicos del tipo de registro seleccionado.
@Composable
private fun DialogoDetalleHistorial(registro: RegistroHistorialBase, mostrarFoto: Boolean, onAlternarFoto: () -> Unit, onCerrar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Detalle de ${nombreSubmodulo(registro).lowercase()}") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                when (registro) {
                    is AccidenteRegistro -> {
                        DatoDetalle("Fecha", registro.fechaAccidente); DatoDetalle("Qué ocurrió", registro.queOcurrio)
                        DatoDetalle("Lesión o diagnóstico", registro.lesion); DatoDetalle("Atención recibida", registro.atencionRecibida)
                        registro.fotoUri?.let { uri ->
                            TextButton(onClick = onAlternarFoto) { Text(if (mostrarFoto) "Ocultar fotografía" else "Mostrar fotografía") }
                            if (mostrarFoto) FotografiaAdjunta(uri)
                        }
                    }
                    is CirugiaRegistro -> {
                        DatoDetalle("Fecha", registro.fechaCirugia); DatoDetalle("Procedimiento", registro.procedimiento)
                        DatoDetalle("Diagnóstico o motivo", registro.diagnosticoMotivo); DatoDetalle("Médico cirujano", registro.medicoCirujano)
                        DatoDetalle("Hospital", registro.hospital); DatoDetalle("Complicaciones", registro.complicaciones)
                    }
                    is ConsultaRegistro -> {
                        DatoDetalle("Fecha", registro.fechaConsulta); DatoDetalle("Especialidad", registro.especialidad)
                        DatoDetalle("Motivo", registro.motivoConsulta); DatoDetalle("Diagnóstico", registro.diagnostico); DatoDetalle("Médico", registro.medicoTratante)
                    }
                    is EnfermedadRegistro -> {
                        DatoDetalle("Enfermedad", registro.nombreEnfermedad); DatoDetalle("Fecha de diagnóstico", registro.fechaDiagnostico)
                        DatoDetalle("Estado", registro.estado); DatoDetalle("Tratamiento actual", registro.tratamientoActual); DatoDetalle("Notas", registro.notas)
                    }
                    is HospitalizacionRegistro -> {
                        DatoDetalle("Hospital", registro.hospital); DatoDetalle("Ingreso", registro.fechaIngreso); DatoDetalle("Alta", registro.fechaAlta)
                        DatoDetalle("Motivo", registro.motivo); DatoDetalle("Diagnóstico", registro.diagnostico)
                        DatoDetalle("Tratamiento o procedimiento", registro.tratamientoProcedimiento); DatoDetalle("Médico responsable", registro.medicoResponsable)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onCerrar) { Text("Cerrar") } }
    )
}

// Oculta valores opcionales que todavía no se hayan capturado.
@Composable
private fun DatoDetalle(etiqueta: String, valor: String) {
    if (valor.isNotBlank()) Text("$etiqueta: $valor")
}

// Carga la fotografía adjunta fuera del hilo de interfaz y la muestra dentro de la consulta.
@Composable
private fun FotografiaAdjunta(uriTexto: String) {
    val context = LocalContext.current.applicationContext
    var cargando by remember(uriTexto) { mutableStateOf(true) }
    var imagen by remember(uriTexto) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uriTexto) {
        cargando = true
        imagen = withContext(Dispatchers.IO) { runCatching { cargarImagenReducida(context, Uri.parse(uriTexto)) }.getOrNull() }
        cargando = false
    }
    when (val foto = imagen) {
        null -> Text(if (cargando) "Cargando fotografía…" else "No se pudo abrir la fotografía adjunta.")
        else -> Image(foto, "Fotografía adjunta al accidente", Modifier.fillMaxWidth().heightIn(max = 320.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit)
    }
}

// Reduce la resolución de la foto para usar menos memoria al mostrarla.
private fun cargarImagenReducida(context: Context, uri: Uri): ImageBitmap? {
    val opciones = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
    if (opciones.outWidth <= 0 || opciones.outHeight <= 0) return null
    var muestra = 1
    while (opciones.outWidth / muestra > 1200 || opciones.outHeight / muestra > 1200) muestra *= 2
    opciones.inJustDecodeBounds = false
    opciones.inSampleSize = muestra
    return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones)?.asImageBitmap() }
}
