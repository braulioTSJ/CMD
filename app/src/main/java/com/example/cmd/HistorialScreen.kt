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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val HistorialNavy = Color(0xFF17466B)

private val HistorialInk = Color(0xFF263746)

private val HistorialCardSurface = Color(0xC8E4F1F7)

private val HistorialCardBorder = Color(0x70B8D3E2)

private val HistorialSoftMint = Color(0xBAE8F5F2)

private data class SubmoduloHistorial(val nombre: String, val icono: Int)

private val SubmodulosHistorial = listOf(
    SubmoduloHistorial("Accidentes", R.drawable.accidente),
    SubmoduloHistorial("Cirugías", R.drawable.bisturi),
    SubmoduloHistorial("Consultas", R.drawable.estetoscopio),
    SubmoduloHistorial("Enfermedades", R.drawable.enfermedades),
    SubmoduloHistorial("Hospitalizaciones", R.drawable.hospitalizacion)
)

// Pantalla principal del módulo Historial.
@Composable
fun HistorialScreen(selectedUserName: String, modifier: Modifier = Modifier) {
    var submoduloSeleccionado by remember { mutableStateOf<SubmoduloHistorial?>(null) }
    val metodosHistorial = MetodosHistorial()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { EncabezadoHistorial() }

        item { TarjetaPerfilSeleccionado(selectedUserName) }

        items(SubmodulosHistorial) { submodulo ->
            TarjetaSubmodulo(submodulo) {
                submoduloSeleccionado = submodulo
            }
        }
    }

    submoduloSeleccionado?.let { submodulo ->
        val cerrarAviso = { submoduloSeleccionado = null }

        when (submodulo.nombre) {
            "Accidentes" -> metodosHistorial.mostrarAccidentes(cerrarAviso)
            "Cirugías" -> metodosHistorial.mostrarCirugias(cerrarAviso)
            "Consultas" -> metodosHistorial.mostrarConsultas(cerrarAviso)
            "Enfermedades" -> metodosHistorial.mostrarEnfermedades(cerrarAviso)
            "Hospitalizaciones" -> metodosHistorial.mostrarHospitalizaciones(cerrarAviso)
            else -> Unit
        }
    }
}

// Encabezado con el título y una descripción breve.
@Composable
private fun EncabezadoHistorial() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Historial médico",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            "Consultas y antecedentes de salud",
            color = Color.White.copy(alpha = .9f),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

// Tarjeta translúcida que identifica el perfil familiar seleccionado.
@Composable
private fun TarjetaPerfilSeleccionado(nombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xBA32B6AD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = .25f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                nombre,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// Tarjeta clickeable de un submódulo del historial.
@Composable
private fun TarjetaSubmodulo(submodulo: SubmoduloHistorial, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier.weight(1f).height(68.dp).clickable(onClick = onClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = HistorialCardSurface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, HistorialCardBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(HistorialSoftMint),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = submodulo.icono),
                        contentDescription = submodulo.nombre,
                        modifier = Modifier.size(26.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(HistorialNavy)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    submodulo.nombre,
                    color = HistorialInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Botón para simular la acción de agregar un registro a este submódulo.
        Button(
            onClick = { },
            modifier = Modifier.size(50.dp),
            shape = CircleShape,
            border = BorderStroke(1.dp, HistorialCardBorder),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HistorialCardSurface,
                contentColor = HistorialNavy
            )
        ) {
            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
    }
}

