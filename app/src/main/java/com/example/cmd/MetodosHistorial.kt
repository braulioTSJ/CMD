package com.example.cmd

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


class MetodosHistorial {
    // Muestra el apartado de Accidentes.
    @Composable
    fun mostrarAccidentes(onCerrar: () -> Unit) {
        mostrarAviso("Accidentes", onCerrar)
    }

    // Muestra el apartado de Cirugías.
    @Composable
    fun mostrarCirugias(onCerrar: () -> Unit) {
        mostrarAviso("Cirugías", onCerrar)
    }

    // Muestra el apartado de Consultas.
    @Composable
    fun mostrarConsultas(onCerrar: () -> Unit) {
        mostrarAviso("Consultas", onCerrar)
    }

    // Muestra el apartado de Enfermedades.
    @Composable
    fun mostrarEnfermedades(onCerrar: () -> Unit) {
        mostrarAviso("Enfermedades", onCerrar)
    }

    // Muestra el apartado de Hospitalizaciones.
    @Composable
    fun mostrarHospitalizaciones(onCerrar: () -> Unit) {
        mostrarAviso("Hospitalizaciones", onCerrar)
    }

    // Presenta el aviso provisional compartido por los submódulos.
    @Composable
    private fun mostrarAviso(nombreSubmodulo: String, onCerrar: () -> Unit) {
        AlertDialog(
            onDismissRequest = onCerrar,
            title = { Text(nombreSubmodulo) },
            text = { Text("Este módulo aún está en preparación.") },
            confirmButton = {
                TextButton(onClick = onCerrar) {
                    Text("Cerrar")
                }
            }
        )
    }
}
