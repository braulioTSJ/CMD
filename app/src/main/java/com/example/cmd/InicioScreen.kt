package com.example.cmd

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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

// Nombre visible mientras el perfil del usuario aún no proviene de una cuenta real.
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

// Pantalla de inicio con el perfil familiar y el estado de citas.
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
    var perfilPorEliminar by remember { mutableStateOf<PerfilFamiliarDemo?>(null) }
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
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Encabezado de bienvenida y datos del usuario.
        item { Header(onViewProfile = { datosPerfilAbierto = datosSuperUsuario }) }

        item {
            FamilyCard(
                selectedMember = selectedMember,
                perfilesAgregados = perfilesAgregados,
                onSelect = onMemberSelected,
                onAdd = { mostrarAltaFamiliar = true },
                onDetails = { datosPerfilAbierto = it },
                onEdit = { perfilEditando = it },
                onDelete = { perfilPorEliminar = it },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Agenda", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                AppointmentCard()
            }
        }
    }

    if (mostrarAltaFamiliar || perfilEditando != null) {
        val perfilActual = perfilEditando
        RegistroFormularioSheet(
            titulo = if (perfilActual == null) "Agregar familiar" else "Editar perfil",
            campos = FormulariosRegistro.familiar,
            valoresIniciales = perfilActual?.let { perfil ->
                perfil.datos + mapOf(
                    "Nombre" to perfil.datos["Nombre"].orEmpty().ifBlank { perfil.nombre },
                    "Apellidos" to perfil.datos["Apellidos"].orEmpty()
                )
            }.orEmpty(),
            permitirFoto = true,
            onCerrar = { mostrarAltaFamiliar = false; perfilEditando = null },
            onGuardar = { datos ->
                val nombre = listOf(datos["Nombre"], datos["Apellidos"])
                    .mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) }
                    .joinToString(" ")
                    .ifBlank { datos["Nombre completo"].orEmpty().trim() }
                val datosConNombreCompleto = datos + ("Nombre completo" to nombre)
                val perfil = PerfilFamiliarDemo(
                    id = perfilActual?.id ?: "familiar_${System.currentTimeMillis()}",
                    nombre = nombre,
                    parentesco = datos["Parentesco"].orEmpty(),
                    datos = datosConNombreCompleto
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

    perfilPorEliminar?.let { perfil ->
        AlertDialog(
            onDismissRequest = { perfilPorEliminar = null },
            title = { Text("Eliminar perfil") },
            text = { Text("¿Quieres eliminar el perfil de ${perfil.nombre}? También se quitarán sus registros de historial de esta sesión.") },
            confirmButton = {
                TextButton(onClick = {
                    onProfileDeleted(perfil)
                    perfilPorEliminar = null
                }) { Text("Eliminar", color = Color(0xFFB3261E)) }
            },
            dismissButton = {
                TextButton(onClick = { perfilPorEliminar = null }) { Text("Cancelar") }
            }
        )
    }
}

// Encabezado de bienvenida con acceso a los datos de la cuenta.
@Composable
private fun Header(onViewProfile: () -> Unit) {
    // Nombre de ejemplo; después puede venir del perfil o de la base de datos.
    val nombreUsuario = DemoUserName

    // Memoriza las iniciales para no recalcularlas en cada recomposición.
    val inicialesUsuario = remember(nombreUsuario) { initialsFor(nombreUsuario) }

    // Fondo de marca con un degradado discreto azul y turquesa.
    Box(
        modifier = Modifier.fillMaxWidth().height(184.dp)
    ) {
        // Acomoda la marca y el saludo con espacio suficiente para leerlos.
            Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp)) {
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
            Spacer(Modifier.height(20.dp))

            // Muestra las iniciales y el nombre del usuario.
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar sencillo con iniciales derivadas del nombre.
                Box(
                    Modifier.size(54.dp).clip(CircleShape).background(Color.White),
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
                    Text("Hola, $nombreUsuario", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                    // Datos demostrativos del perfil.
                    Text("Tu salud y la de tu familia, en un solo lugar.", color = Color.White.copy(alpha = .9f), fontSize = 12.sp)

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
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
    onDelete: (PerfilFamiliarDemo) -> Unit,
    modifier: Modifier = Modifier
) {
    // Superficie blanca con sombra mínima para mantener un aspecto limpio.
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        // Organiza el título, la indicación y las opciones familiares.
        Column(Modifier.padding(horizontal = 16.dp, vertical = 15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Título sencillo de la sección familiar.
                Text("Perfiles familiares", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.Bold)

                Spacer(Modifier.weight(1f))
                Surface(shape = RoundedCornerShape(20.dp), color = SoftMint) {
                    Text("${perfilesAgregados.size}", color = Navy, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }

            // Instrucción breve para orientar la selección.
            Text(
                if (perfilesAgregados.isEmpty()) "Agrega un perfil familiar para comenzar."
                else "Selecciona a quién quieres consultar",
                color = Muted,
                fontSize = 12.sp
            )

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
                            modifier = Modifier.width(100.dp),
                            onClick = { onSelect(perfil.id) },
                            onDetails = { onDetails(perfil.datos) },
                            onEdit = { onEdit(perfil) },
                            onDelete = { onDelete(perfil) }
                        )
                    }
                }

                }

                Spacer(Modifier.width(8.dp))

                // Opción fija para agregar un familiar, aun cuando haya muchos perfiles.
                Column(
                    modifier = Modifier.width(80.dp).height(96.dp).clip(RoundedCornerShape(14.dp))
                        .background(SoftMint)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onAdd)
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Signo más para comunicar que se puede agregar un perfil.
                    Text("+", color = Navy, fontSize = 24.sp, fontWeight = FontWeight.Medium)

                    // Texto de la acción para agregar un usuario.
                    Text("Agregar", color = Navy, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 13.sp)
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
    onDetails: () -> Unit,
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
        modifier = modifier.height(96.dp).clip(RoundedCornerShape(14.dp))
            .background(tileBackgroundColor)
            .border(1.dp, if (selected) Color.White.copy(alpha = .75f) else CardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(7.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 3.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterVertically)
        ) {
            Box(
                Modifier.size(30.dp).clip(CircleShape)
                    .background(if (selected) Color.White.copy(alpha = .2f) else SoftMint),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initialsFor(name),
                    color = if (selected) Color.White else Navy,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(role, color = if (selected) Color.White else Ink, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(name, color = if (selected) Color.White.copy(alpha = .9f) else Muted, fontSize = 10.sp, maxLines = 1, textAlign = TextAlign.Center)
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
                DropdownMenuItem(text = { Text("Ver datos") }, onClick = { menuAbierto = false; onDetails() })
                DropdownMenuItem(text = { Text("Editar") }, onClick = { menuAbierto = false; onEdit() })
                DropdownMenuItem(text = { Text("Eliminar") }, onClick = { menuAbierto = false; onDelete() })
            }
        }
    }
}

// Estado vacío de citas hasta conectar el calendario.
@Composable
private fun AppointmentCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Teal),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CITAS", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Sin citas registradas", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text("Aquí aparecerán las próximas citas.", color = Color.White.copy(alpha = .9f), fontSize = 11.sp)
            }
        }
    }
}
