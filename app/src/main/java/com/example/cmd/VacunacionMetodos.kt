package com.example.cmd

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

// Métodos para abrir cada opción del módulo Vacunación.
class VacunacionMetodos {
    // Muestra el historial de vacunas.
    @Composable
    fun mostrarHistorial(onCerrar: () -> Unit) {
        mostrarAviso("Historial de vacunas", onCerrar)
    }

    // Abre la acción para agregar una vacuna.
    @Composable
    fun agregarVacuna(onCerrar: () -> Unit) {
        mostrarAviso("Agregar vacuna", onCerrar)
    }

    // Presenta el aviso provisional compartido por las opciones.
    @Composable
    private fun mostrarAviso(titulo: String, onCerrar: () -> Unit) {
        AlertDialog(
            onDismissRequest = onCerrar,
            title = { Text(titulo) },
            text = { Text("Esta función aún está en preparación.") },
            confirmButton = {
                TextButton(onClick = onCerrar) {
                    Text("Cerrar")
                }
            }
        )
    }
}
