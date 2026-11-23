package com.example.cmd

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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

// Azul oscuro usado para títulos y detalles importantes.
private val Navy = Color(0xFF17466B)

// Turquesa usado como color de acento de la marca CMD.
private val Teal = Color(0xFF32B6AD)

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

data class PerfilFamiliarDemo(
    val id: String,
    val nombre: String,
    val parentesco: String,
    val datos: Map<String, String>
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

// Pantalla de inicio con perfil familiar, accesos y próxima cita.
@Composable
fun InicioScreen(
    selectedMember: String,
    onMemberSelected: (String) -> Unit,
    perfilesAgregados: List<PerfilFamiliarDemo> = emptyList(),
    onProfileAdded: (PerfilFamiliarDemo) -> Unit = {},
    onProfileUpdated: (PerfilFamiliarDemo) -> Unit = {},
    onProfileDeleted: (PerfilFamiliarDemo) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var mostrarAltaFamiliar by remember { mutableStateOf(false) }
    var perfilEditando by remember { mutableStateOf<PerfilFamiliarDemo?>(null) }
    var datosPerfilAbierto by remember { mutableStateOf<Map<String, String>?>(null) }
    val datosSuperUsuario = mapOf(
        "Nombre completo" to DemoUserName,
        "Rol" to "Superusuario",
        "Correo electrónico" to "No configurado",
        "Teléfono" to "No configurado",
        "Tipo de sangre" to "O+",
        "Alergias" to "No registrado"
    )
    // Lista desplazable con fondo de la pantalla.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        // Encabezado de bienvenida y datos del usuario.
        item { Header(onViewProfile = { datosPerfilAbierto = datosSuperUsuario }) }

        // Contenido principal de la pantalla.
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                // Selector sencillo de perfiles familiares.
                FamilyCard(
                    selectedMember = selectedMember,
                    perfilesAgregados = perfilesAgregados,
                    onSelect = onMemberSelected,
                    onAdd = { mostrarAltaFamiliar = true },
                    onDetails = { datosPerfilAbierto = it },
                    onEdit = { perfilEditando = it },
                    onDelete = onProfileDeleted
                )

                // Separa la tarjeta familiar de la próxima cita.
                Spacer(Modifier.height(10.dp))

                // Muestra un resumen de la cita más cercana.
                AppointmentCard()
            }
        }
    }

    if (mostrarAltaFamiliar || perfilEditando != null) {
        val perfilActual = perfilEditando
        RegistroFormularioSheet(
            titulo = if (perfilActual == null) "Agregar familiar" else "Editar perfil",
            campos = FormulariosRegistro.familiar,
            valoresIniciales = perfilActual?.datos.orEmpty(),
            permitirFoto = true,
            onCerrar = { mostrarAltaFamiliar = false; perfilEditando = null },
            onGuardar = { datos ->
                val nombre = datos["Nombre completo"].orEmpty().trim()
                val perfil = PerfilFamiliarDemo(
                    id = perfilActual?.id ?: "familiar_${System.currentTimeMillis()}",
                    nombre = nombre,
                    parentesco = datos["Parentesco"].orEmpty(),
                    datos = datos
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

    datosPerfilAbierto?.let { datos ->
        RegistroFormularioSheet(
            titulo = if (datos["Rol"] == "Superusuario") "Perfil del superusuario" else "Datos del perfil",
            campos = if (datos["Rol"] == "Superusuario") {
                listOf(
                    CampoRegistro("Nombre completo"),
                    CampoRegistro("Rol"),
                    CampoRegistro("Correo electrónico"),
                    CampoRegistro("Teléfono"),
                    CampoRegistro("Tipo de sangre"),
                    CampoRegistro("Alergias")
                )
            } else FormulariosRegistro.familiar,
            valoresIniciales = datos,
            soloLectura = true,
            onCerrar = { datosPerfilAbierto = null }
        )
    }
}

// Encabezado compacto con identidad CMD y acceso al perfil del superusuario.
@Composable
private fun Header(onViewProfile: () -> Unit) {
    // Nombre de ejemplo; después puede venir del perfil o de la base de datos.
    val nombreUsuario = DemoUserName

    // Memoriza las iniciales para no recalcularlas en cada recomposición.
    val inicialesUsuario = remember(nombreUsuario) { initialsFor(nombreUsuario) }

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
                    Text("X años · Tipo de sangre O+", color = Color.White.copy(alpha = .9f), fontSize = 11.sp)

                    // Deja un espacio corto antes de la acción del perfil.
                    Spacer(Modifier.height(5.dp))

                    // Acción visible para editar el perfil.
                    Surface(
                        modifier = Modifier.clickable(onClick = onViewProfile),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Text(
                            "Ver perfil",
                            color = Navy,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Tarjeta para elegir un familiar o iniciar la acción de agregar un perfil.
@Composable
private fun FamilyCard(
    selectedMember: String,
    perfilesAgregados: List<PerfilFamiliarDemo>,
    onSelect: (String) -> Unit,
    onAdd: () -> Unit,
    onDetails: (Map<String, String>) -> Unit,
    onEdit: (PerfilFamiliarDemo) -> Unit,
    onDelete: (PerfilFamiliarDemo) -> Unit
) {
    // Superficie blanca con sombra mínima para mantener un aspecto limpio.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        // Organiza el título, la indicación y las opciones familiares.
        Column(Modifier.padding(horizontal = 13.dp, vertical = 11.dp)) {
            // Título de Familiares y acceso visual a emergencias en la esquina derecha.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título sencillo de la sección familiar.
                Text("Familiares", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)

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

            // Instrucción breve para orientar la selección.
            Text("Selecciona un perfil", color = Muted, fontSize = 11.sp)

            // Separa la indicación de las tarjetas de selección.
            Spacer(Modifier.height(9.dp))

            // Mantiene visible Agregar perfil y permite desplazar los perfiles si se acumulan.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                perfilesAgregados.forEach { perfil ->
                    key(perfil.id) {
                        MemberTile(
                            role = perfil.parentesco,
                            name = perfil.nombre,
                            selected = selectedMember == perfil.id,
                            modifier = Modifier.width(88.dp),
                            onClick = { onSelect(perfil.id); onDetails(perfil.datos) },
                            onEdit = { onEdit(perfil) },
                            onDelete = { onDelete(perfil) }
                        )
                    }
                }

                }

                Spacer(Modifier.width(8.dp))

                // Opción fija para agregar un familiar, aun cuando haya muchos perfiles.
                Column(
                    modifier = Modifier.width(76.dp).height(88.dp).clip(RoundedCornerShape(12.dp))
                        .background(SoftMint)
                        .clickable(onClick = onAdd)
                        .padding(7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Signo más para comunicar que se puede agregar un perfil.
                    Text("+", color = Navy, fontSize = 21.sp, fontWeight = FontWeight.Medium)

                    // Texto de la acción para agregar un usuario.
                    Text("Agregar perfil", color = Navy, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
                }
            }
        }
    }
}

// Tarjeta individual de un perfil dentro del selector familiar.
@Composable
private fun MemberTile(
    role: String,
    name: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
        modifier = modifier.height(88.dp).clip(RoundedCornerShape(12.dp))
            .background(tileBackgroundColor)
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(role, color = if (selected) Color.White else Ink, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text(name, color = if (selected) Color.White.copy(alpha = .9f) else Muted, fontSize = 10.sp, maxLines = 2, textAlign = TextAlign.Center)
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
                DropdownMenuItem(text = { Text("Editar") }, onClick = { menuAbierto = false; onEdit() })
                DropdownMenuItem(text = { Text("Eliminar") }, onClick = { menuAbierto = false; onDelete() })
            }
        }
    }
}

// Tarjeta sencilla que resalta la siguiente cita médica.
@Composable
private fun AppointmentCard() {
    // Superficie turquesa de alto contraste para encontrar la cita rápido.
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Teal),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // Organiza el detalle de la cita en una sola fila compacta.
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Texto que identifica la sección de citas.
            Text("CITA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)

            // Separa la etiqueta del detalle de la cita.
            Spacer(Modifier.width(12.dp))

            // Información de ejemplo de la cita próxima.
            Column {
                // Título directo de la tarjeta.
                Text("Próxima cita", color = Color.White.copy(alpha = .9f), fontSize = 10.sp)

                // Especialidad médica y familiar con cita.
                Text("Pediatra · Lucas", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                // Fecha y hora de muestra.
                Text("Mañana · 10:00", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}
