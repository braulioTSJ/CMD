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

// Datos fijos de las funciones; cada una puede mostrar un PNG o una abreviatura.
private data class Feature(val abbreviation: String, val title: String, val iconRes: Int? = null)

// Filas fijas de accesos rápidos, en el mismo orden y distribución de la pantalla.
private val HomeFeatureRows = listOf(
    listOf(
        // HM identifica el historial médico.
        Feature("HM", "Historial médico", R.drawable.historial_medico),

        // VA identifica el registro de vacunas.
        Feature("VA", "Vacunas", R.drawable.jeringuilla)
    ),
    listOf(
        // ME identifica alergias y medicamentos.
        Feature("ME", "Medicamentos", R.drawable.medicina),

        // RX identifica las recetas digitales.
        Feature("RX", "Recetas", R.drawable.informe_medico)
    )
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
    modifier: Modifier = Modifier
) {
    // Lista desplazable con fondo de la pantalla.
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        // Encabezado de bienvenida y datos del usuario.
        item { Header() }

        // Contenido principal de la pantalla.
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                // Selector sencillo de perfiles familiares.
                FamilyCard(selectedMember, onMemberSelected)

                // Separa la tarjeta familiar de los accesos rápidos.
                Spacer(Modifier.height(10.dp))

                // Muestra las funciones principales de CMD.
                FeatureGrid()

                // Separa los accesos rápidos de la próxima cita.
                Spacer(Modifier.height(10.dp))

                // Muestra un resumen de la cita más cercana.
                AppointmentCard()
            }
        }
    }
}

// Encabezado compacto con identidad CMD y saludo personalizado.
@Composable
private fun Header() {
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
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
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
private fun FamilyCard(selected: String, onSelect: (String) -> Unit) {
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

            // Presenta los perfiles y la opción para agregar otro.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                // Perfil demostrativo de María.
                MemberTile("M", "Mamá", "María", selected == "Mamá", Modifier.weight(1f)) {
                    onSelect("Mamá")
                }

                // Perfil demostrativo de Lucas.
                MemberTile("L", "Hijo", "Lucas", selected == "Hijo", Modifier.weight(1f)) {
                    onSelect("Hijo")
                }

                // Opción para agregar un nuevo integrante de la familia.
                Column(
                    modifier = Modifier.weight(1f).height(76.dp).clip(RoundedCornerShape(12.dp))
                        .background(SoftMint)
                        .clickable { onSelect("Nuevo perfil") }
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
    initial: String,
    role: String,
    name: String,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    // Anima suavemente el cambio entre el tono claro y el azul seleccionado translúcido.
    val tileBackgroundColor by animateColorAsState(
        targetValue = if (selected) Color(0xB817466B) else CardSurface,
        animationSpec = tween(durationMillis = 280),
        label = "memberTileBackground"
    )

    // El fondo azul translúcido y el texto claro indican el perfil seleccionado.
    Column(
        modifier = modifier.height(76.dp).clip(RoundedCornerShape(12.dp))
            .background(tileBackgroundColor)
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Inicial sencilla del familiar en lugar de un emoji.
        Text(
            initial,
            color = if (selected) Color.White else Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        // Relación familiar del perfil.
        Text(
            role,
            color = if (selected) Color.White else Ink,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Nombre del familiar.
        Text(
            name,
            color = if (selected) Color.White.copy(alpha = .85f) else Muted,
            fontSize = 9.sp
        )
    }
}

// Cuadrícula de funciones con etiquetas breves en lugar de emojis.
@Composable
private fun FeatureGrid() {
    // Presenta dos tarjetas por fila para que cada opción sea más fácil de tocar.
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeFeatureRows.forEach { row ->
            // Fila con dos accesos rápidos del mismo tamaño.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { feature ->
                    // Tarjeta clara para una función de la aplicación.
                    Card(
                        modifier = Modifier.weight(1f).height(72.dp).clickable { },
                        shape = RoundedCornerShape(13.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        // Alinea el icono y el nombre de la función en cada tarjeta.
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Fondo suave que contiene el PNG o la etiqueta de respaldo.
                            Box(
                                Modifier.size(38.dp).clip(CircleShape).background(SoftMint),
                                contentAlignment = Alignment.Center
                            ) {
                                // PNG médico de la función o iniciales si no hay un icono asociado.
                                if (feature.iconRes != null) {
                                    Image(
                                        painter = painterResource(id = feature.iconRes),
                                        contentDescription = feature.title,
                                        modifier = Modifier.size(25.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                } else {
                                    // Iniciales de respaldo para funciones sin un PNG equivalente.
                                    Text(feature.abbreviation, color = Navy, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Separa el icono del título de la función.
                            Spacer(Modifier.width(10.dp))

                            // Nombre directo de la función.
                            Text(
                                feature.title,
                                color = Ink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

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
