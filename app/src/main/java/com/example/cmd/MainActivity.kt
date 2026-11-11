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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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

    // Símbolo del expediente.
    NavigationDestination("Expediente", textIcon = "▤"),

    // Símbolo de mensajes.
    NavigationDestination("Mensajes", textIcon = "✉"),

    // Símbolo de ajustes.
    NavigationDestination("Ajustes", textIcon = "⚙")
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

    // Conserva el perfil familiar elegido al cambiar entre pestañas.
    var perfilSeleccionado by remember { mutableStateOf("Mamá") }

    // Convierte la relación familiar seleccionada en el nombre visible del perfil.
    val nombrePerfilSeleccionado = when (perfilSeleccionado) {
        "Mamá" -> "María"
        "Hijo" -> "Lucas"
        else -> "Nuevo perfil"
    }

    // Aplica el tema Material y deja el degradado como fondo de toda la ventana.
    MaterialTheme {
        // Fondo único para la pantalla, la navegación inferior y las barras del sistema.
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(
                        CmdNavy,
                        Color(0xFF20A199),
                        CmdTeal
                    )
                )
            )
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
                    // Muestra la pantalla que corresponde a la pestaña seleccionada.
                    when (selectedTab) {
                        // Pantalla de inicio creada en el archivo InicioScreen.kt.
                        0 -> InicioScreen(
                            selectedMember = perfilSeleccionado,
                            onMemberSelected = { perfilSeleccionado = it },
                            modifier = Modifier.padding(insets)
                        )

                        // Módulo de historial médico creado en HistorialScreen.kt.
                        1 -> HistorialScreen(
                            selectedUserName = nombrePerfilSeleccionado,
                            modifier = Modifier.padding(insets)
                        )

                        // Pantallas que se completarán más adelante.
                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(insets),
                                contentAlignment = Alignment.Center
                            ) {
                                // Texto provisional para las secciones aún no desarrolladas.
                                Text("Pantalla en construcción", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
