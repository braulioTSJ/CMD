package com.example.cmd

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri

private val VerdeFormulario = Color(0xFF247F93)
private val AzulFormulario = Color(0xFF17466B)
private val BlancoFormulario = Color.White
private val ColorDesplegableHistorial = Color(0xFF155E75)
@Composable
private fun coloresCamposHistorial() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = BlancoFormulario,
    unfocusedTextColor = BlancoFormulario,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor = BlancoFormulario,
    unfocusedBorderColor = BlancoFormulario.copy(alpha = .75f),
    focusedLabelColor = BlancoFormulario,
    unfocusedLabelColor = BlancoFormulario.copy(alpha = .9f),
    cursorColor = BlancoFormulario,
    focusedTrailingIconColor = BlancoFormulario,
    unfocusedTrailingIconColor = BlancoFormulario
)

// Formulario específico para accidentes; es el único que permite adjuntar fotografía.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioAccidente(
    perfil: String,
    fechaNacimientoPerfil: String?,
    registro: AccidenteRegistro?,
    onCerrar: () -> Unit,
    onGuardar: (AccidenteRegistro) -> Unit
) {
    val coloresCampos = coloresCamposHistorial()
    val fechaMaxima = fechaHoyIso()
    var fecha by remember(registro) { mutableStateOf(registro?.fechaAccidente.orEmpty()) }
    var ocurrido by remember(registro) { mutableStateOf(registro?.queOcurrio.orEmpty()) }
    var lesion by remember(registro) { mutableStateOf(registro?.lesion.orEmpty()) }
    var atencion by remember(registro) { mutableStateOf(registro?.atencionRecibida.orEmpty()) }
    var foto by remember(registro) { mutableStateOf(registro?.fotoUri) }
    val context = LocalContext.current
    var uriPendiente by remember { mutableStateOf<Uri?>(null) }
    var errorCamara by remember { mutableStateOf(false) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { guardada ->
        if (guardada) foto = uriPendiente?.toString()
        errorCamara = false
    }

    FormularioBaseHistorial("Evento", registro != null, onCerrar,
        fecha.isNotBlank() && esFechaDentroDeLimites(fecha, fechaNacimientoPerfil, fechaMaxima) && ocurrido.isNotBlank(), campos = {
        CampoFechaRegistro("Fecha del evento", fecha, { fecha = it }, false, coloresCampos, fechaNacimientoPerfil, fechaMaxima)
        CampoTextoHistorial("Qué ocurrió", ocurrido, { ocurrido = it }, multilinea = true, maxCaracteres = 500)
        CampoTextoHistorial("Lesión o diagnóstico", lesion, { lesion = it }, multilinea = true, requerido = false, maxCaracteres = 500)
        CampoTextoHistorial("Atención recibida", atencion, { atencion = it }, multilinea = true, requerido = false, maxCaracteres = 500)
        OutlinedButton(
            onClick = {
                runCatching { crearUriFoto(context) }.onSuccess { uri ->
                    uriPendiente = uri
                    runCatching { camara.launch(uri) }.onFailure { errorCamara = true }
                }.onFailure { errorCamara = true }
            },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, BlancoFormulario),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = BlancoFormulario)
        ) { Text(if (foto == null) "Tomar fotografía" else "Cambiar fotografía") }
        if (foto != null) Text("Fotografía adjunta", color = BlancoFormulario)
        if (errorCamara) Text("No se pudo abrir la cámara", color = BlancoFormulario)
    }, onGuardar = {
        onGuardar(AccidenteRegistro(registro?.idLocal ?: 0, perfil, fecha, ocurrido.trim(), lesion.trim(), atencion.trim(), foto))
    })
}

// Formulario específico para cirugías y procedimientos.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioCirugia(perfil: String, fechaNacimientoPerfil: String?, registro: CirugiaRegistro?, onCerrar: () -> Unit, onGuardar: (CirugiaRegistro) -> Unit) {
    val coloresCampos = coloresCamposHistorial()
    val fechaMaxima = fechaHoyIso()
    var fecha by remember(registro) { mutableStateOf(registro?.fechaCirugia.orEmpty()) }
    var procedimiento by remember(registro) { mutableStateOf(registro?.procedimiento.orEmpty()) }
    var motivo by remember(registro) { mutableStateOf(registro?.diagnosticoMotivo.orEmpty()) }
    var medico by remember(registro) { mutableStateOf(registro?.medicoCirujano.orEmpty()) }
    var hospital by remember(registro) { mutableStateOf(registro?.hospital.orEmpty()) }
    var complicaciones by remember(registro) { mutableStateOf(registro?.complicaciones.orEmpty()) }
    FormularioBaseHistorial("Cirugía", registro != null, onCerrar, fecha.isNotBlank() && esFechaDentroDeLimites(fecha, fechaNacimientoPerfil, fechaMaxima) && procedimiento.isNotBlank() && motivo.isNotBlank(), campos = {
        CampoFechaRegistro("Fecha de cirugía", fecha, { fecha = it }, false, coloresCampos, fechaNacimientoPerfil, fechaMaxima)
        CampoTextoHistorial("Procedimiento", procedimiento, { procedimiento = it }, maxCaracteres = 500)
        CampoTextoHistorial("Diagnóstico o motivo", motivo, { motivo = it }, multilinea = true, maxCaracteres = 500)
        CampoTextoHistorial("Médico cirujano", medico, { medico = it }, requerido = false, maxCaracteres = 150, soloLetras = true)
        CampoTextoHistorial("Hospital", hospital, { hospital = it }, requerido = false, maxCaracteres = 150)
        CampoTextoHistorial("Complicaciones", complicaciones, { complicaciones = it }, multilinea = true, requerido = false, maxCaracteres = 500)
    }, onGuardar = { onGuardar(CirugiaRegistro(registro?.idLocal ?: 0, perfil, fecha, procedimiento.trim(), motivo.trim(), medico.trim(), hospital.trim(), complicaciones.trim())) })
}

// Formulario específico para consultas médicas.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioConsulta(perfil: String, fechaNacimientoPerfil: String?, registro: ConsultaRegistro?, onCerrar: () -> Unit, onGuardar: (ConsultaRegistro) -> Unit) {
    val coloresCampos = coloresCamposHistorial()
    val fechaMaxima = fechaHoyIso()
    var fecha by remember(registro) { mutableStateOf(registro?.fechaConsulta.orEmpty()) }
    var especialidad by remember(registro) { mutableStateOf(registro?.especialidad.orEmpty()) }
    var motivo by remember(registro) { mutableStateOf(registro?.motivoConsulta.orEmpty()) }
    var diagnostico by remember(registro) { mutableStateOf(registro?.diagnostico.orEmpty()) }
    var medico by remember(registro) { mutableStateOf(registro?.medicoTratante.orEmpty()) }
    FormularioBaseHistorial("Consulta", registro != null, onCerrar, fecha.isNotBlank() && esFechaDentroDeLimites(fecha, fechaNacimientoPerfil, fechaMaxima) && especialidad.isNotBlank() && motivo.isNotBlank(), campos = {
        CampoFechaRegistro("Fecha de consulta", fecha, { fecha = it }, false, coloresCampos, fechaNacimientoPerfil, fechaMaxima)
        CampoOpcionHistorial("Especialidad", especialidad, listOf("Medicina General", "Pediatría", "Ortopedia", "Cardiología")) { especialidad = it }
        CampoTextoHistorial("Motivo de consulta", motivo, { motivo = it }, multilinea = true, maxCaracteres = 500)
        CampoTextoHistorial("Diagnóstico", diagnostico, { diagnostico = it }, multilinea = true, requerido = false, maxCaracteres = 500)
        CampoTextoHistorial("Médico tratante", medico, { medico = it }, requerido = false, maxCaracteres = 150, soloLetras = true)
    }, onGuardar = { onGuardar(ConsultaRegistro(registro?.idLocal ?: 0, perfil, fecha, especialidad.trim(), motivo.trim(), diagnostico.trim(), medico.trim())) })
}

// Formulario específico para enfermedades y su estado actual.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioEnfermedad(perfil: String, fechaNacimientoPerfil: String?, registro: EnfermedadRegistro?, onCerrar: () -> Unit, onGuardar: (EnfermedadRegistro) -> Unit) {
    val coloresCampos = coloresCamposHistorial()
    val fechaMaxima = fechaHoyIso()
    var nombre by remember(registro) { mutableStateOf(registro?.nombreEnfermedad.orEmpty()) }
    var fecha by remember(registro) { mutableStateOf(registro?.fechaDiagnostico.orEmpty()) }
    var estado by remember(registro) { mutableStateOf(registro?.estado.orEmpty()) }
    var tratamiento by remember(registro) { mutableStateOf(registro?.tratamientoActual.orEmpty()) }
    var notas by remember(registro) { mutableStateOf(registro?.notas.orEmpty()) }
    FormularioBaseHistorial("Enfermedad", registro != null, onCerrar, nombre.isNotBlank() && fecha.isNotBlank() && esFechaDentroDeLimites(fecha, fechaNacimientoPerfil, fechaMaxima) && estado.isNotBlank(), campos = {
        CampoTextoHistorial("Nombre de la enfermedad", nombre, { nombre = it }, maxCaracteres = 150)
        CampoFechaRegistro("Fecha del diagnóstico", fecha, { fecha = it }, false, coloresCampos, fechaNacimientoPerfil, fechaMaxima)
        CampoOpcionHistorial("Estado", estado, listOf("Activa", "Controlada", "Resuelta")) { estado = it }
        CampoTextoHistorial("Tratamiento actual", tratamiento, { tratamiento = it }, multilinea = true, requerido = false, maxCaracteres = 500)
        CampoTextoHistorial("Notas", notas, { notas = it }, multilinea = true, requerido = false, maxCaracteres = 500)
    }, onGuardar = { onGuardar(EnfermedadRegistro(registro?.idLocal ?: 0, perfil, nombre.trim(), fecha, estado, tratamiento.trim(), notas.trim())) })
}

// Formulario específico para hospitalizaciones.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioHospitalizacion(perfil: String, fechaNacimientoPerfil: String?, registro: HospitalizacionRegistro?, onCerrar: () -> Unit, onGuardar: (HospitalizacionRegistro) -> Unit) {
    val coloresCampos = coloresCamposHistorial()
    val fechaMaxima = fechaHoyIso()
    var hospital by remember(registro) { mutableStateOf(registro?.hospital.orEmpty()) }
    var ingreso by remember(registro) { mutableStateOf(registro?.fechaIngreso.orEmpty()) }
    var alta by remember(registro) { mutableStateOf(registro?.fechaAlta.orEmpty()) }
    var motivo by remember(registro) { mutableStateOf(registro?.motivo.orEmpty()) }
    var diagnostico by remember(registro) { mutableStateOf(registro?.diagnostico.orEmpty()) }
    var tratamiento by remember(registro) { mutableStateOf(registro?.tratamientoProcedimiento.orEmpty()) }
    var medico by remember(registro) { mutableStateOf(registro?.medicoResponsable.orEmpty()) }
    val fechasValidas = ingreso.isNotBlank() && esFechaDentroDeLimites(ingreso, fechaNacimientoPerfil, fechaMaxima) &&
        (alta.isBlank() || esFechaDentroDeLimites(alta, ingreso.ifBlank { fechaNacimientoPerfil }, fechaMaxima))
    FormularioBaseHistorial("Hospitalización", registro != null, onCerrar, hospital.isNotBlank() && fechasValidas && motivo.isNotBlank() && diagnostico.isNotBlank(), campos = {
        CampoTextoHistorial("Hospital", hospital, { hospital = it }, maxCaracteres = 150)
        CampoFechaRegistro("Fecha de ingreso", ingreso, { ingreso = it }, false, coloresCampos, fechaNacimientoPerfil, fechaMaxima)
        CampoFechaRegistro("Fecha de alta", alta, { alta = it }, false, coloresCampos, ingreso.ifBlank { fechaNacimientoPerfil }, fechaMaxima)
        CampoTextoHistorial("Motivo de hospitalización", motivo, { motivo = it }, multilinea = true, maxCaracteres = 500)
        CampoTextoHistorial("Diagnóstico", diagnostico, { diagnostico = it }, multilinea = true, maxCaracteres = 500)
        CampoTextoHistorial("Tratamiento o procedimiento", tratamiento, { tratamiento = it }, multilinea = true, requerido = false, maxCaracteres = 500)
        CampoTextoHistorial("Médico responsable", medico, { medico = it }, requerido = false, maxCaracteres = 150, soloLetras = true)
    }, onGuardar = { onGuardar(HospitalizacionRegistro(registro?.idLocal ?: 0, perfil, hospital.trim(), ingreso, alta, motivo.trim(), diagnostico.trim(), tratamiento.trim(), medico.trim())) })
}

// Estructura común del desplegable: título, campos, cancelar y guardar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioBaseHistorial(
    titulo: String,
    editando: Boolean,
    onCerrar: () -> Unit,
    valido: Boolean,
    campos: @Composable () -> Unit,
    onGuardar: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onCerrar,
        containerColor = VerdeFormulario,
        contentColor = BlancoFormulario,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(if (editando) "Editar $titulo" else "Agregar $titulo", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Completa los campos requeridos. Las fechas se limitan al periodo válido del perfil.", fontSize = 14.sp, color = BlancoFormulario.copy(alpha = .92f))
            campos()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onCerrar, modifier = Modifier.weight(1f).heightIn(min = 48.dp), colors = ButtonDefaults.textButtonColors(contentColor = BlancoFormulario)) { Text("Cancelar", fontSize = 16.sp) }
                Button(onClick = onGuardar, modifier = Modifier.weight(1f).heightIn(min = 48.dp), enabled = valido, colors = ButtonDefaults.buttonColors(containerColor = AzulFormulario, contentColor = BlancoFormulario)) { Text("Guardar", fontSize = 16.sp) }
            }
        }
    }
}

// Campo de texto reutilizable para evitar repetir estilo; el contenido de cada formulario sigue siendo explícito.
@Composable
private fun CampoTextoHistorial(
    etiqueta: String,
    valor: String,
    onValor: (String) -> Unit,
    multilinea: Boolean = false,
    requerido: Boolean = true,
    maxCaracteres: Int = 500,
    soloLetras: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = {
            val textoPermitido = if (soloLetras) filtrarNombrePersona(it) else it
            onValor(textoPermitido.take(maxCaracteres))
        },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(if (requerido) etiqueta else "$etiqueta (opcional)") },
        singleLine = !multilinea,
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
        supportingText = { Text("${valor.length}/$maxCaracteres", color = BlancoFormulario.copy(alpha = .82f)) },
        colors = coloresCamposHistorial()
    )
}

// Filtra dígitos y símbolos en campos destinados exclusivamente a nombres de personas.
private fun filtrarNombrePersona(valor: String): String =
    valor.filter { it.isLetter() || it.isWhitespace() || it in "-'’." }

// Campo de selección para las opciones médicas ya definidas.
@Composable
private fun CampoOpcionHistorial(etiqueta: String, valor: String, opciones: List<String>, onSeleccion: (String) -> Unit) {
    var abierto by remember(etiqueta) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = valor,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                label = { Text(etiqueta) },
                readOnly = true,
                trailingIcon = { Text("⌄", color = BlancoFormulario, fontSize = 20.sp) },
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
                colors = coloresCamposHistorial()
            )
            Box(Modifier.matchParentSize().clickable { abierto = !abierto })
        }
        if (abierto) Column(
            modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp).clip(RoundedCornerShape(12.dp))
                .background(ColorDesplegableHistorial).verticalScroll(rememberScrollState())
        ) {
            opciones.forEach { opcion ->
                Text(
                    text = opcion,
                    color = BlancoFormulario,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth().clickable { onSeleccion(opcion); abierto = false }
                        .padding(horizontal = 16.dp, vertical = 13.dp)
                )
            }
        }
    }
}
