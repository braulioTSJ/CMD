package com.example.cmd

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

// Colores del módulo, coordinados con el resto de CMD.
private val DocumentosNavy = Color(0xFF17466B)
private val DocumentosInk = Color(0xFF263746)
private val DocumentosCardSurface = Color(0xC8E4F1F7)
private val DocumentosCardBorder = Color(0x70B8D3E2)
private val DocumentosSoftMint = Color(0xBAE8F5F2)

// Datos de cada opción del módulo Documentos.
private data class DocumentoSubmodulo(val nombre: String, val icono: Int)

// Tarjetas del módulo sin duplicar el acceso a Recetas.
private val DocumentosSubmodulos = listOf(
    DocumentoSubmodulo("Agregar documento", R.drawable.informe),
    DocumentoSubmodulo("Estudios", R.drawable.quimica),
    DocumentoSubmodulo("Imágenes", R.drawable.imagen),
    DocumentoSubmodulo("Altas médicas", R.drawable.informe_medico),
    DocumentoSubmodulo("Reportes", R.drawable.informe)
)

// Pantalla de documentos con el perfil seleccionado y sus submódulos.
@Composable
fun DocumentosScreen(
    selectedUserName: String,
    colorTarjetaPerfil: Color,
    modifier: Modifier = Modifier
) {
    var submoduloSeleccionado by remember { mutableStateOf<DocumentoSubmodulo?>(null) }
    var mostrarFormularioDocumento by remember { mutableStateOf(false) }
    val metodos = DocumentosMetodos()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { EncabezadoDocumentos() }

        item { TarjetaPerfilSeleccionado(selectedUserName, colorTarjetaPerfil) }

        items(DocumentosSubmodulos) { submodulo ->
            TarjetaDocumento(submodulo) {
                if (submodulo.nombre == "Agregar documento") {
                    mostrarFormularioDocumento = true
                } else {
                    submoduloSeleccionado = submodulo
                }
            }
        }
    }

    submoduloSeleccionado?.let { submodulo ->
        val cerrarAviso = { submoduloSeleccionado = null }
        when (submodulo.nombre) {
            "Estudios" -> metodos.mostrarEstudios(cerrarAviso)
            "Imágenes" -> metodos.mostrarImagenes(cerrarAviso)
            "Altas médicas" -> metodos.mostrarAltasMedicas(cerrarAviso)
            "Reportes" -> metodos.mostrarReportes(cerrarAviso)
            else -> Unit
        }
    }

    if (mostrarFormularioDocumento) {
        RegistroFormularioSheet(
            titulo = "Agregar documento",
            campos = FormulariosRegistro.documento,
            permitirFoto = true,
            onCerrar = { mostrarFormularioDocumento = false },
            onGuardar = { datos ->
                RegistroLocalStore.agregar("Documentos", datos)
                mostrarFormularioDocumento = false
            }
        )
    }
}

// Encabezado del módulo Documentos.
@Composable
private fun EncabezadoDocumentos() {
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
                painter = painterResource(id = R.drawable.informe),
                contentDescription = "Icono de documentos",
                modifier = Modifier.size(29.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White)
            )
        }

        Spacer(Modifier.width(12.dp))

        Text(
            "Documentos",
            color = Color.White,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Tarjeta clickeable para una categoría de documentos.
@Composable
private fun TarjetaDocumento(submodulo: DocumentoSubmodulo, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(68.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DocumentosCardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, DocumentosCardBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(DocumentosSoftMint),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = submodulo.icono),
                    contentDescription = submodulo.nombre,
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(DocumentosNavy)
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                submodulo.nombre,
                color = DocumentosInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
