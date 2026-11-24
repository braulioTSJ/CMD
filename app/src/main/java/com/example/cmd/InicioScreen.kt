package com.example.cmd

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// Azul oscuro usado para títulos y detalles importantes.
private val Navy = Color(0xFF17466B)

// Color principal para textos sobre fondos claros.
private val Ink = Color(0xFF263746)

// Color secundario para descripciones y datos menos importantes.
private val Muted = Color(0xFF73818C)

// Azul muy claro y translúcido que deja percibir el degradado detrás de las tarjetas.
private val CardSurface = Color(0xC8E4F1F7)

// Borde azul grisáceo translúcido para separar las tarjetas con suavidad.
private val CardBorder = Color(0x70B8D3E2)

// Menta translúcido para perfiles seleccionados y etiquetas de accesos rápidos.
private val SoftMint = Color(0xBAE8F5F2)

// Nombre demostrativo usado en el encabezado.
private const val DemoUserName = "Braulio Castillo"

// Adapta perfiles guardados antes de separar nombre y apellidos.
private fun datosFamiliaresEditables(datos: Map<String, String>): Map<String, String> {
    val nombreCompleto = datos["Nombre completo"].orEmpty().trim().split(Regex("\\s+"))
    val nombre = datos["Nombre"].orEmpty().ifBlank { nombreCompleto.firstOrNull().orEmpty() }
    val apellidos = datos["Apellidos"].orEmpty().ifBlank { nombreCompleto.drop(1).joinToString(" ") }
    val sangreOriginal = datos["Tipo de sangre"].orEmpty()
    val sangreNormalizada = Regex(".*\\(([^()]*)\\).*").find(sangreOriginal)?.groupValues?.get(1) ?: sangreOriginal
    return datos.toMutableMap().apply {
        put("Nombre", nombre)
        put("Apellidos", apellidos)
        put("Sexo", datos["Sexo"].orEmpty().ifBlank { datos["Genero"].orEmpty() })
        put("Tipo de sangre", sangreNormalizada.takeUnless { it == "No registrado" }.orEmpty())
    }
}
data class PerfilFamiliarDemo(
    val id: String,
    val nombre: String,
    val parentesco: String,
    val datos: Map<String, String>
)

// Datos y acciones de cada tarjeta dentro de la cuadrícula de perfiles.
private data class PerfilTileItem(
    val id: String,
    val nombre: String,
    val seleccionado: Boolean,
    val alSeleccionar: () -> Unit,
    val alVer: () -> Unit,
    val alEditar: () -> Unit,
    val alEliminar: () -> Unit,
    val permiteEliminar: Boolean
)

// Calcula las iniciales del nombre una sola vez por valor de usuario.
private fun initialsFor(name: String): String = name
    .trim()
    .split(Regex("\\s+"))
    .filter { it.isNotBlank() }
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
    .joinToString("")
    .ifBlank { "?" }

// Calcula la edad cumplida usando la fecha de nacimiento guardada en formato ISO.
private fun calcularEdad(fechaIso: String): Int? = runCatching {
    val partes = fechaIso.split("-").map(String::toInt)
    require(partes.size == 3)
    val nacimiento = Calendar.getInstance().apply {
        isLenient = false
        clear()
        set(partes[0], partes[1] - 1, partes[2])
    }
    val hoy = Calendar.getInstance()
    require(!nacimiento.after(hoy))
    var edad = hoy.get(Calendar.YEAR) - nacimiento.get(Calendar.YEAR)
    val antesDelCumpleanos = hoy.get(Calendar.MONTH) < nacimiento.get(Calendar.MONTH) ||
        (hoy.get(Calendar.MONTH) == nacimiento.get(Calendar.MONTH) && hoy.get(Calendar.DAY_OF_MONTH) < nacimiento.get(Calendar.DAY_OF_MONTH))
    if (antesDelCumpleanos) edad--
    edad.takeIf { it >= 0 }
}.getOrNull()

// Pantalla de inicio con perfil familiar, accesos y próxima cita.
@Composable
fun InicioScreen(
    selectedMember: String,
    onMemberSelected: (String) -> Unit,
    datosSuperUsuario: Map<String, String> = mapOf("Nombre" to "Braulio", "Apellidos" to "Castillo", "Nombre completo" to DemoUserName, "Rol" to "Superusuario"),
    onSuperUsuarioActualizado: (Map<String, String>) -> Unit = {},
    perfilesAgregados: List<PerfilFamiliarDemo> = emptyList(),
    onProfileAdded: (PerfilFamiliarDemo) -> Unit = {},
    onProfileUpdated: (PerfilFamiliarDemo) -> Unit = {},
    onProfileDeleted: (PerfilFamiliarDemo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mostrarAltaFamiliar by remember { mutableStateOf(false) }
    var perfilEditando by remember { mutableStateOf<PerfilFamiliarDemo?>(null) }
    var editarSuperUsuario by remember { mutableStateOf(false) }
    var datosPerfilAbierto by remember { mutableStateOf<Map<String, String>?>(null) }
    // Lista desplazable con fondo de la pantalla.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        // Encabezado de bienvenida y datos del usuario.
        item {
            Header(
                nombreUsuario = datosSuperUsuario["Nombre completo"].orEmpty().ifBlank { DemoUserName },
                fechaNacimiento = datosSuperUsuario["Fecha de nacimiento"].orEmpty(),
                tipoSangre = datosSuperUsuario["Tipo de sangre"].orEmpty()
            )
        }

        // Contenido principal de la pantalla.
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                // Selector sencillo de perfiles familiares.
                FamilyCard(
                    selectedMember = selectedMember,
                    superUserName = datosSuperUsuario["Nombre completo"].orEmpty(),
                    perfilesAgregados = perfilesAgregados,
                    onSelect = onMemberSelected,
                    onAdd = { mostrarAltaFamiliar = true },
                    onDetails = { datosPerfilAbierto = it },
                    onSuperUserDetails = { datosPerfilAbierto = datosSuperUsuario },
                    onEditSuperUser = { editarSuperUsuario = true },
                    onEdit = { perfilEditando = it },
                    onDelete = onProfileDeleted
                )

            }
        }
    }

    if (mostrarAltaFamiliar || perfilEditando != null) {
        val perfilActual = perfilEditando
        RegistroFormularioSheet(
            titulo = if (perfilActual == null) "Agregar familiar" else "Editar perfil",
            campos = FormulariosRegistro.familiar,
            valoresIniciales = perfilActual?.datos?.let(::datosFamiliaresEditables).orEmpty(),
            limitarCaracteres = true,
            desplegablesHaciaAbajo = true,
            fechaMinimaIso = "1900-01-01",
            fechaMaximaIso = fechaHoyIso(),
            colorFondo = Color(0xFF247F93),
            onCerrar = { mostrarAltaFamiliar = false; perfilEditando = null },
            onGuardar = { datos ->
                val datosCompletos = datos.toMutableMap()
                val nombre = listOf(datos["Nombre"], datos["Apellidos"]).filterNotNull()
                    .map { it.trim().replace(Regex("\\s+"), " ") }.filter(String::isNotBlank).joinToString(" ")
                datosCompletos["Nombre"] = datos["Nombre"].orEmpty().trim().replace(Regex("\\s+"), " ")
                datosCompletos["Apellidos"] = datos["Apellidos"].orEmpty().trim().replace(Regex("\\s+"), " ")
                datosCompletos["Nombre completo"] = nombre
                val perfil = PerfilFamiliarDemo(
                    id = perfilActual?.id ?: "familiar_${java.util.UUID.randomUUID()}",
                    nombre = nombre,
                    parentesco = datos["Parentesco"].orEmpty(),
                    datos = datosCompletos
                )
                if (perfilActual == null) {
                    onProfileAdded(perfil)
                    onMemberSelected(perfil.id)
                } else {
                    onProfileUpdated(perfil)
                }
                mostrarAltaFamiliar = false
                perfilEditando = null
            }
        )
    }

    if (editarSuperUsuario) {
        val camposSuperUsuario = listOf(
            CampoRegistro("Nombre", maxCaracteres = 50),
            CampoRegistro("Apellidos", maxCaracteres = 50),
            CampoRegistro("Correo electrónico", requerido = false, maxCaracteres = 150),
            CampoRegistro("Teléfono", requerido = false, maxCaracteres = 20),
            CampoRegistro("Fecha de nacimiento", esFecha = true),
            CampoRegistro("Sexo", listOf("Masculino", "Femenino", "Otro"), requerido = false),
            CampoRegistro("Tipo de sangre", listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"), requerido = false, maxCaracteres = 10),
            CampoRegistro("Alergias", requerido = false, unaLinea = false, maxCaracteres = 200)
        )
        RegistroFormularioSheet(
            titulo = "Editar mi perfil",
            campos = camposSuperUsuario,
            valoresIniciales = datosFamiliaresEditables(datosSuperUsuario),
            limitarCaracteres = true,
            desplegablesHaciaAbajo = true,
            fechaMinimaIso = "1900-01-01",
            fechaMaximaIso = fechaHoyIso(),
            colorFondo = Color(0xFF247F93),
            onCerrar = { editarSuperUsuario = false },
            onGuardar = { datos ->
                val nombre = listOf(datos["Nombre"], datos["Apellidos"]).filterNotNull()
                    .map { it.trim().replace(Regex("\\s+"), " ") }.filter(String::isNotBlank).joinToString(" ")
                onSuperUsuarioActualizado(datos + mapOf("Nombre completo" to nombre, "Rol" to "Superusuario"))
                editarSuperUsuario = false
            }
        )
    }

    datosPerfilAbierto?.let { datos ->
        RegistroFormularioSheet(
            titulo = if (datos["Rol"] == "Superusuario") "Perfil del superusuario" else "Datos del perfil",
            campos = if (datos["Rol"] == "Superusuario") {
                listOf(
                    CampoRegistro("Nombre"),
                    CampoRegistro("Apellidos"),
                    CampoRegistro("Rol"),
                    CampoRegistro("Correo electrónico"),
                    CampoRegistro("Teléfono"),
                    CampoRegistro("Fecha de nacimiento", esFecha = true),
                    CampoRegistro("Sexo"),
                    CampoRegistro("Tipo de sangre"),
                    CampoRegistro("Alergias")
                )
            } else FormulariosRegistro.familiar,
            valoresIniciales = if (datos["Rol"] == "Superusuario") datos else datosFamiliaresEditables(datos),
            soloLectura = true,
            onEditarLectura = if (datos["Rol"] == "Superusuario") {
                { datosPerfilAbierto = null; editarSuperUsuario = true }
            } else null,
            onCerrar = { datosPerfilAbierto = null }
        )
    }
}

// Encabezado compacto con identidad CMD y datos del superusuario.
@Composable
private fun Header(nombreUsuario: String, fechaNacimiento: String, tipoSangre: String) {
    // Memoriza las iniciales para no recalcularlas en cada recomposición.
    val inicialesUsuario = remember(nombreUsuario) { initialsFor(nombreUsuario) }
    val edad = remember(fechaNacimiento) { calcularEdad(fechaNacimiento) }
    val resumenUsuario = listOf(
        edad?.let { "$it años" } ?: "Edad no registrada",
        "Tipo de sangre ${tipoSangre.ifBlank { "no registrada" }}"
    ).joinToString(" · ")

    // Fondo de marca con un degradado discreto azul y turquesa.
    Box(
        modifier = Modifier.fillMaxWidth().height(174.dp)
    ) {
        // Acomoda la marca y el saludo con espacio suficiente para leerlos.
        Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 14.dp)) {
            // Fila superior que identifica la aplicación.
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Etiqueta de texto CMD en lugar de un emoji o icono decorativo.
                Surface(shape = RoundedCornerShape(9.dp), color = Color.White.copy(alpha = .18f)) {
                    Text(
                        "CMD",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }

                // Nombre completo de la aplicación.
                Text(
                    "Cartilla Médica Digital",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            // Deja espacio entre el nombre de la app y el saludo.
            Spacer(Modifier.height(17.dp))

            // Muestra las iniciales y el nombre del usuario.
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar sencillo con iniciales derivadas del nombre.
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    // Iniciales visibles del usuario.
                    Text(
                        inicialesUsuario,
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Separa el avatar de los datos de la persona.
                Spacer(Modifier.width(11.dp))

                // Saludo y datos breves del usuario.
                Column {
                    // Saludo fácil de entender para la persona usuaria.
                    Text("Hola, $nombreUsuario", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)

                    // Datos demostrativos del perfil.
                    Text(resumenUsuario, color = Color.White.copy(alpha = .9f), fontSize = 12.sp)
                }
            }
        }
    }
}

// Tarjeta para elegir un familiar o iniciar la acción de agregar un perfil.
@Composable
private fun FamilyCard(
    selectedMember: String,
    superUserName: String,
    perfilesAgregados: List<PerfilFamiliarDemo>,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onDetails: (Map<String, String>) -> Unit,
    onSuperUserDetails: () -> Unit,
    onEditSuperUser: () -> Unit,
    onEdit: (PerfilFamiliarDemo) -> Unit,
    onDelete: (PerfilFamiliarDemo) -> Unit
) {
    var perfilPendienteDeEliminar by remember { mutableStateOf<PerfilFamiliarDemo?>(null) }

    // Superficie blanca con sombra mínima para mantener un aspecto limpio.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        // Organiza el título, la indicación y las opciones familiares.
        Column(Modifier.padding(horizontal = 16.dp, vertical = 18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            // Título de Familiares y acceso visual a emergencias en la esquina derecha.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título sencillo de la sección familiar.
                Text("Familiares", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)

                // Empuja el icono de emergencias hacia la esquina superior derecha.
                Spacer(Modifier.weight(1f))

                // Icono de equipo médico asociado a contactos de emergencia.
                Image(
                    painter = painterResource(id = R.drawable.equipo_medico),
                    contentDescription = "Contactos de emergencia",
                    modifier = Modifier.size(27.dp),
                    contentScale = ContentScale.Fit
                )
            }

            // Orienta sobre cómo cambiar entre la cuenta propia y los familiares.
            Text("Toca una tarjeta para cambiar el perfil", color = Muted, fontSize = 12.sp)

            // Separa la indicación de las tarjetas de selección.
            Spacer(Modifier.height(12.dp))

            // Prepara Mi perfil y los familiares para acomodarlos en filas de dos.
            val perfilesEnTarjetas = buildList {
                add(
                    PerfilTileItem(
                        id = "__superusuario__",
                        nombre = superUserName,
                        seleccionado = selectedMember.isBlank(),
                        alSeleccionar = { onSelect("") },
                        alVer = onSuperUserDetails,
                        alEditar = onEditSuperUser,
                        alEliminar = {},
                        permiteEliminar = false
                    )
                )
                perfilesAgregados.forEach { perfil ->
                    add(
                        PerfilTileItem(
                            id = perfil.id,
                            nombre = perfil.nombre,
                            seleccionado = selectedMember == perfil.id,
                            alSeleccionar = { onSelect(if (selectedMember == perfil.id) "" else perfil.id) },
                            alVer = { onDetails(perfil.datos) },
                            alEditar = { onEdit(perfil) },
                            alEliminar = { perfilPendienteDeEliminar = perfil },
                            permiteEliminar = true
                        )
                    )
                }
            }

            // Cada perfil nuevo ocupa la siguiente celda y hace crecer la tarjeta hacia abajo.
            perfilesEnTarjetas.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fila.forEach { perfil ->
                        key(perfil.id) {
                            MemberTile(
                                name = perfil.nombre,
                                selected = perfil.seleccionado,
                                modifier = Modifier.weight(1f),
                                onClick = perfil.alSeleccionar,
                                onView = perfil.alVer,
                                onEdit = perfil.alEditar,
                                onDelete = perfil.alEliminar,
                                mostrarEliminar = perfil.permiteEliminar
                            )
                        }
                    }
                    if (fila.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Botón compacto separado de los perfiles, alineado a la esquina inferior derecha.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Surface(
                    modifier = Modifier.clickable(onClick = onAdd),
                    shape = RoundedCornerShape(22.dp),
                    color = SoftMint
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("+", color = Navy, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                        Text("Agregar perfil", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // Confirma la eliminación antes de borrar el perfil y su historial asociado.
    perfilPendienteDeEliminar?.let { perfil ->
        AlertDialog(
            onDismissRequest = { perfilPendienteDeEliminar = null },
            title = { Text("Eliminar perfil") },
            text = { Text("¿Quieres eliminar el perfil de ${perfil.nombre} y sus registros médicos?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(perfil)
                        perfilPendienteDeEliminar = null
                    }
                ) { Text("Eliminar", color = Color(0xFFB3261E)) }
            },
            dismissButton = {
                TextButton(onClick = { perfilPendienteDeEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// Tarjeta individual de un perfil dentro del selector familiar.
@Composable
private fun MemberTile(
    name: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    mostrarEliminar: Boolean
) {
    // Anima suavemente el cambio entre el tono claro y el azul seleccionado translúcido.
    val tileBackgroundColor by animateColorAsState(
        targetValue = if (selected) Color(0xB817466B) else CardSurface,
        animationSpec = tween(durationMillis = 280),
        label = "memberTileBackground"
    )

    // El fondo azul translúcido y el texto claro indican el perfil seleccionado.
    var menuAbierto by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.height(122.dp).clip(RoundedCornerShape(12.dp))
            .background(tileBackgroundColor)
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(name, color = if (selected) Color.White else Ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, textAlign = TextAlign.Center, lineHeight = 19.sp)
        }

        Box(
            modifier = Modifier.align(Alignment.TopEnd).size(30.dp).clickable { menuAbierto = true },
            contentAlignment = Alignment.Center
        ) {
            Text(
                "⋯",
                color = if (selected) Color.White else Navy,
                fontSize = 19.sp,
                lineHeight = 19.sp
            )
            DropdownMenu(
                expanded = menuAbierto,
                onDismissRequest = { menuAbierto = false },
                containerColor = Color(0xFFF2F7FA)
            ) {
                DropdownMenuItem(text = { Text("Ver perfil") }, onClick = { menuAbierto = false; onView() })
                DropdownMenuItem(text = { Text("Editar") }, onClick = { menuAbierto = false; onEdit() })
                if (mostrarEliminar) {
                    DropdownMenuItem(text = { Text("Eliminar") }, onClick = { menuAbierto = false; onDelete() })
                }
            }
        }
    }
}
