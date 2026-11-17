package com.example.cmd

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

// Métodos para abrir cada submódulo de Documentos.
class DocumentosMetodos {
    // Muestra el apartado de Estudios.
    @Composable
    fun mostrarEstudios(onCerrar: () -> Unit) {
        mostrarAviso("Estudios", onCerrar)
    }

    // Muestra el apartado de Imágenes.
    @Composable
    fun mostrarImagenes(onCerrar: () -> Unit) {
        mostrarAviso("Imágenes", onCerrar)
    }

    // Muestra el apartado de Altas médicas.
    @Composable
    fun mostrarAltasMedicas(onCerrar: () -> Unit) {
        mostrarAviso("Altas médicas", onCerrar)
    }

    // Muestra el apartado de Reportes.
    @Composable
    fun mostrarReportes(onCerrar: () -> Unit) {
        mostrarAviso("Reportes", onCerrar)
    }

    // Presenta el aviso provisional compartido por los submódulos.
    @Composable
    private fun mostrarAviso(nombreSubmodulo: String, onCerrar: () -> Unit) {
        AlertDialog(
            onDismissRequest = onCerrar,
            title = { Text(nombreSubmodulo) },
            text = { Text("Este submódulo aún está en preparación.") },
            confirmButton = {
                TextButton(onClick = onCerrar) {
                    Text("Cerrar")
                }
            }
        )
    }
}
