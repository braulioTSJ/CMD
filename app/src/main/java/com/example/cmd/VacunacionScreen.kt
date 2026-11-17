package com.example.cmd

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Colores de las tarjetas del módulo Vacunación.
private val VacunacionNavy = Color(0xFF17466B)
private val VacunacionInk = Color(0xFF263746)
private val VacunacionCardSurface = Color(0xC8E4F1F7)
private val VacunacionCardBorder = Color(0x70B8D3E2)
private val VacunacionSoftMint = Color(0xBAE8F5F2)

// Datos de las opciones del módulo Vacunación.
private data class OpcionVacunacion(val nombre: String, val icono: Int)

// Acciones principales del esquema de navegación.
private val OpcionesVacunacion = listOf(
    OpcionVacunacion("Historial", R.drawable.historial_medico),
    OpcionVacunacion("Agregar vacuna", R.drawable.jeringuilla)
)

// Pantalla de Vacunación para el perfil familiar seleccionado.
@Composable
fun VacunacionScreen(
    selectedUserName: String,
    colorTarjetaPerfil: Color,
    modifier: Modifier = Modifier
) {
    var opcionSeleccionada by remember { mutableStateOf<OpcionVacunacion?>(null) }
    var mostrarFormularioVacuna by remember { mutableStateOf(false) }
    val metodosVacunacion = VacunacionMetodos()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EncabezadoVacunacion() }

        item { TarjetaPerfilSeleccionado(selectedUserName, colorTarjetaPerfil) }

        items(OpcionesVacunacion) { opcion ->
            if (opcion.nombre == "Agregar vacuna") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TarjetaVacunacion(opcion, modifier = Modifier.weight(1f)) {
                        mostrarFormularioVacuna = true
                    }

                    // Botón + exclusivo para agregar una vacuna.
                    Button(
                        onClick = { mostrarFormularioVacuna = true },
                        modifier = Modifier.size(50.dp),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, VacunacionCardBorder),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VacunacionCardSurface,
                            contentColor = VacunacionNavy
                        )
                    ) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                TarjetaVacunacion(opcion) {
                    opcionSeleccionada = opcion
                }
            }
        }
    }

    opcionSeleccionada?.let { opcion ->
        val cerrarAviso = { opcionSeleccionada = null }
        when (opcion.nombre) {
            "Historial" -> metodosVacunacion.mostrarHistorial(cerrarAviso)
            "Agregar vacuna" -> metodosVacunacion.agregarVacuna(cerrarAviso)
            else -> Unit
        }
    }

    if (mostrarFormularioVacuna) {
        RegistroFormularioSheet(
            titulo = "Agregar vacuna",
            campos = FormulariosRegistro.vacuna,
            permitirFoto = true,
            onCerrar = { mostrarFormularioVacuna = false },
            onGuardar = { datos ->
                RegistroLocalStore.agregar("Vacunación", datos)
                mostrarFormularioVacuna = false
            }
        )
    }
}

// Encabezado con el icono y el nombre del módulo.
@Composable
private fun EncabezadoVacunacion() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = .18f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.jeringuilla),
                contentDescription = "Icono de vacunación",
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            "Vacunación",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Tarjeta clickeable de una opción de vacunación.
@Composable
private fun TarjetaVacunacion(
    opcion: OpcionVacunacion,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(76.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = VacunacionCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, VacunacionCardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(VacunacionSoftMint),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = opcion.icono),
                    contentDescription = opcion.nombre,
                    modifier = Modifier.size(27.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(VacunacionNavy)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                opcion.nombre,
                color = VacunacionInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
