package com.example.cmd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

// Colores del módulo, coordinados con el resto de CMD.
private val ResumenNavy = Color(0xFF17466B)
private val ResumenInk = Color(0xFF263746)
private val ResumenCardSurface = Color(0xC8E4F1F7)
private val ResumenCardBorder = Color(0x70B8D3E2)
private val ResumenSoftMint = Color(0xBAE8F5F2)

// Datos de los submódulos visibles en Resumen médico.
private data class ResumenSubmodulo(val nombre: String, val icono: Int)

// Submódulos del diseño de referencia, sin Emergencias.
private val ResumenSubmodulos = listOf(
    ResumenSubmodulo("Alergias", R.drawable.alergia),
    ResumenSubmodulo("Tipo de sangre", R.drawable.sangre),
    ResumenSubmodulo("Medicamentos", R.drawable.pildora),
    ResumenSubmodulo("Antecedentes", R.drawable.antecedente)
)

// Pantalla del resumen médico con sus submódulos seleccionables.
@Composable
fun ResumenMedicoScreen(
    selectedUserName: String,
    colorTarjetaPerfil: Color,
    resumen: ResumenMedicoData,
    modifier: Modifier = Modifier
) {
    var submoduloSeleccionado by remember { mutableStateOf<ResumenSubmodulo?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EncabezadoResumenMedico() }

        item { TarjetaPerfilSeleccionado(selectedUserName, colorTarjetaPerfil) }

        item { Spacer(Modifier.height(20.dp)) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumenSubmodulo(
                    submodulo = ResumenSubmodulos[0],
                    detalle = resumen.alergias,
                    modifier = Modifier.weight(1f)
                ) {
                    submoduloSeleccionado = ResumenSubmodulos[0]
                }
                TarjetaResumenSubmodulo(
                    submodulo = ResumenSubmodulos[1],
                    detalle = resumen.tipoSangre,
                    modifier = Modifier.weight(1f)
                ) {
                    submoduloSeleccionado = ResumenSubmodulos[1]
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaResumenSubmodulo(
                    submodulo = ResumenSubmodulos[2],
                    detalle = if (resumen.medicamentos.isEmpty()) "Sin datos" else "${resumen.medicamentos.size} registro(s)",
                    modifier = Modifier.weight(1f)
                ) {
                    submoduloSeleccionado = ResumenSubmodulos[2]
                }
                TarjetaResumenSubmodulo(
                    submodulo = ResumenSubmodulos[3],
                    detalle = if (resumen.antecedentes.isEmpty()) "Sin datos" else "${resumen.antecedentes.size} registro(s)",
                    modifier = Modifier.weight(1f)
                ) {
                    submoduloSeleccionado = ResumenSubmodulos[3]
                }
            }
        }
    }

    submoduloSeleccionado?.let { submodulo ->
        val cerrarAviso = { submoduloSeleccionado = null }
        val datos = when (submodulo.nombre) {
            "Alergias" -> listOf(resumen.alergias)
            "Tipo de sangre" -> listOf(resumen.tipoSangre)
            "Medicamentos" -> resumen.medicamentos
            "Antecedentes" -> resumen.antecedentes
            else -> emptyList()
        }.filter { it.isNotBlank() && it != "No registrado" }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = cerrarAviso,
            title = { Text(submodulo.nombre) },
            text = {
                if (datos.isEmpty()) Text("No hay información registrada para este perfil.")
                else Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    datos.forEach { dato -> Text("• $dato", color = ResumenInk) }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = cerrarAviso) { Text("Cerrar") }
            }
        )
    }
}

// Encabezado con el icono de resumen y el nombre del módulo.
@Composable
private fun EncabezadoResumenMedico() {
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
                painter = painterResource(id = R.drawable.resumen),
                contentDescription = "Icono de resumen médico",
                modifier = Modifier.size(30.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            "Resumen médico",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Tarjeta clickeable de un submódulo del resumen médico.
@Composable
private fun TarjetaResumenSubmodulo(
    submodulo: ResumenSubmodulo,
    detalle: String? = null,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.aspectRatio(1f).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ResumenCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, ResumenCardBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(ResumenSoftMint),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = submodulo.icono),
                    contentDescription = submodulo.nombre,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(ResumenNavy)
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                submodulo.nombre,
                color = ResumenInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            if (detalle != null) {
                Text(
                    detalle,
                    color = ResumenNavy,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
