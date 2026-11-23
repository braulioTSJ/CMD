package com.example.cmd

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Azul oscuro para los iconos seleccionados y elementos destacados.
private val CmdNavy = Color(0xFF17466B)

// Turquesa que completa el degradado de fondo de la aplicación.
private val CmdTeal = Color(0xFF20A199)

// Transparencia de los elementos de navegación que no están seleccionados.
private val UnselectedNavigationAlpha = .8f

// Modelo simple para guardar el nombre y el icono de cada pestaña.
private data class NavigationDestination(
    val label: String,
    val textIcon: String? = null,
    val imageIcon: Int? = null
)

// Lista constante de pestañas para no reconstruirla en cada recomposición.
private val NavigationDestinations = listOf(
    // Símbolo de inicio.
    NavigationDestination("Inicio", textIcon = "⌂"),

    // Símbolo de la sección del historial médico.
    NavigationDestination("Historial", imageIcon = R.drawable.historial_medico),

    // Icono de resumen médico.
    NavigationDestination("Resumen", imageIcon = R.drawable.resumen),

    // Icono y nombre del módulo de documentos.
    NavigationDestination("Documentos", imageIcon = R.drawable.informe),

    // Icono y nombre del módulo de vacunación.
    NavigationDestination("Vacunación", imageIcon = R.drawable.jeringuilla)
)

// Activity de entrada: habilita el dibujo detrás de las barras del sistema.
class MainActivity : ComponentActivity() {
    // Crea la ventana y carga el contenido principal de Compose.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permite dibujar el fondo detrás de la barra superior e inferior del sistema.
        enableEdgeToEdge()

        // Inicia la interfaz principal de CMD.
        setContent {
            CmdHomeScreen()
        }
    }
}

// Contenedor de la aplicación: fondo degradado, contenido y navegación inferior.
@Composable
private fun CmdHomeScreen() {
    // Controla si se muestra el borrador de acceso o la pantalla principal.
    var showHome by remember { mutableStateOf(false) }

    // Guarda cuál pestaña de la barra inferior está seleccionada.
    var selectedTab by remember { mutableIntStateOf(0) }

    // Recuerda la pestaña anterior para volver desde Seguridad/Configuración.
    var pestanaAnterior by remember { mutableIntStateOf(0) }

    // Conserva el perfil familiar elegido al cambiar entre pestañas.
    var perfilSeleccionado by remember { mutableStateOf("Mamá") }
    val perfilesFamiliares = remember {
        mutableStateListOf(
            PerfilFamiliarDemo(
                id = "Mamá",
                nombre = "María",
                parentesco = "Mamá",
                datos = mapOf(
                    "Nombre completo" to "María",
                    "Parentesco" to "Madre",
                    "Tipo de sangre" to "O positivo (O+)",
                    "Alergias" to "No registrado"
                )
            ),
            PerfilFamiliarDemo(
                id = "Hijo",
                nombre = "Lucas",
                parentesco = "Hijo",
                datos = mapOf(
                    "Nombre completo" to "Lucas",
                    "Parentesco" to "Hijo",
                    "Tipo de sangre" to "No registrado",
                    "Alergias" to "No registrado"
                )
            )
        )
    }

    // Convierte la relación familiar seleccionada en el nombre visible del perfil.
    val perfilActivo = perfilesFamiliares.firstOrNull { it.id == perfilSeleccionado }
    val nombrePerfilSeleccionado = perfilActivo?.nombre.orEmpty()

    // Obtiene los datos médicos guardados para el perfil familiar seleccionado.
    val datosMedicosPerfil = perfilActivo?.datos
    val resumenPerfil = ResumenMedicoMetodos().obtenerResumen(nombrePerfilSeleccionado, datosMedicosPerfil)

    // Cambia el degradado de fondo según el módulo que esté abierto.
    val fondoDegradado = when (selectedTab) {
        1 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF164B66),
                Color(0xFF177D89),
                Color(0xFF35A99C)
            )
        )
        2 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF145B45),
                Color(0xFF218D64),
                Color(0xFF40B88A)
            )
        )
        3 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF233C67),
                Color(0xFF3D6698),
                Color(0xFF6894B5)
            )
        )
        4 -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF3D3475),
                Color(0xFF5B55A5),
                Color(0xFF8580C5)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(CmdNavy, Color(0xFF20A199), CmdTeal)
        )
    }

    // Ajusta el color translúcido de la tarjeta del perfil al degradado del módulo.
    val colorTarjetaPerfil = when (selectedTab) {
        1 -> Color(0xBA218D91)
        2 -> Color(0xBA218D64)
        3 -> Color(0xBA3D6698)
        4 -> Color(0xBA625BA8)
        else -> Color(0xBA32B6AD)
    }

    // Aplica el tema Material y deja el degradado como fondo de toda la ventana.
    MaterialTheme {
        // Fondo único para la pantalla, la navegación inferior y las barras del sistema.
        Box(
            modifier = Modifier.fillMaxSize().background(fondoDegradado)
        ) {
            // Muestra primero el acceso; tras pulsar el botón se abre el inicio de demostración.
            if (!showHome) {
                // Formulario de acceso visual creado en Login.kt.
                LoginScreen(onLogin = { showHome = true })
            } else {
                // Scaffold transparente para que se vea el fondo del Box.
                Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,

                // Barra inferior transparente para mostrar el mismo degradado detrás.
                bottomBar = {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        // Reutiliza las pestañas declaradas fuera del contenido visual.
                        NavigationDestinations.forEachIndexed { index, destination ->
                            // Elemento seleccionable de la navegación inferior.
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },

                                // Muestra el símbolo correspondiente a esta sección.
                                icon = {
                                    if (destination.imageIcon != null) {
                                        // PNG del historial médico teñido de blanco para la barra inferior.
                                        Image(
                                            painter = painterResource(id = destination.imageIcon),
                                            contentDescription = destination.label,
                                            modifier = Modifier.size(22.dp),
                                            contentScale = ContentScale.Fit,
                                            colorFilter = ColorFilter.tint(Color.White)
                                        )
                                    } else {
                                        // Símbolo de texto para las demás pestañas.
                                        Text(
                                            destination.textIcon.orEmpty(),
                                            fontSize = 20.sp,
                                            color = Color.White
                                        )
                                    }
                                },

                                // Muestra el nombre de la sección debajo del símbolo.
                                label = {
                                    Text(destination.label, fontSize = 10.sp, color = Color.White)
                                },

                                // Mantiene el texto y el icono blancos sobre el degradado.
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = Color.White,
                                    indicatorColor = Color.White.copy(alpha = .18f),
                                    unselectedIconColor = Color.White.copy(alpha = UnselectedNavigationAlpha),
                                    unselectedTextColor = Color.White.copy(alpha = UnselectedNavigationAlpha)
                                )
                            )
                        }
                    }
                }
                ) { insets ->
                    Box(modifier = Modifier.fillMaxSize().padding(insets)) {
                        // Muestra el módulo correspondiente a la pestaña seleccionada.
                        when (selectedTab) {
                            0 -> InicioScreen(
                                selectedMember = perfilSeleccionado,
                                onMemberSelected = { perfilSeleccionado = it },
                                perfilesAgregados = perfilesFamiliares,
                                onProfileAdded = { perfil -> perfilesFamiliares.add(perfil) },
                                onProfileUpdated = { perfil ->
                                    val index = perfilesFamiliares.indexOfFirst { it.id == perfil.id }
                                    if (index >= 0) perfilesFamiliares[index] = perfil
                                },
                                onProfileDeleted = { perfil ->
                                    perfilesFamiliares.removeAll { it.id == perfil.id }
                                    MetodosHistorial().eliminarPerfil(perfil.nombre)
                                    if (perfilSeleccionado == perfil.id) {
                                        perfilSeleccionado = perfilesFamiliares.firstOrNull()?.id.orEmpty()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            1 -> HistorialScreen(
                                selectedUserName = nombrePerfilSeleccionado,
                                colorTarjetaPerfil = colorTarjetaPerfil,
                                modifier = Modifier.fillMaxSize()
                            )

                            2 -> ResumenMedicoScreen(
                                selectedUserName = nombrePerfilSeleccionado,
                                colorTarjetaPerfil = colorTarjetaPerfil,
                                resumen = resumenPerfil,
                                modifier = Modifier.fillMaxSize()
                            )

                            3 -> DocumentosScreen(
                                selectedUserName = nombrePerfilSeleccionado,
                                colorTarjetaPerfil = colorTarjetaPerfil,
                                modifier = Modifier.fillMaxSize()
                            )

                            4 -> VacunacionScreen(
                                selectedUserName = nombrePerfilSeleccionado,
                                colorTarjetaPerfil = colorTarjetaPerfil,
                                modifier = Modifier.fillMaxSize()
                            )

                            5 -> Column(
                                modifier = Modifier.fillMaxSize().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "Seguridad / Configuración",
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Contraseña · 2FA · Sesiones · Copias de seguridad",
                                    color = Color.White.copy(alpha = .9f),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        // Acceso a Seguridad/Configuración en la esquina superior derecha.
                        IconButton(
                            onClick = {
                                if (selectedTab == 5) {
                                    selectedTab = pestanaAnterior
                                } else {
                                    pestanaAnterior = selectedTab
                                    selectedTab = 5
                                }
                            },
                            modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(44.dp)
                        ) {
                            Text(
                                if (selectedTab == 5) "‹" else "⚙",
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
