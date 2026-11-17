package com.example.cmd

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

// Métodos para abrir cada submódulo de Resumen médico.
class ResumenMedicoMetodos {
    // Muestra el apartado de Alergias.
    @Composable
    fun mostrarAlergias(onCerrar: () -> Unit) {
        mostrarAviso("Alergias", onCerrar)
    }

    // Muestra el apartado de Tipo de sangre.
    @Composable
    fun mostrarTipoDeSangre(onCerrar: () -> Unit) {
        mostrarAviso("Tipo de sangre", onCerrar)
    }

    // Muestra el apartado de Medicamentos.
    @Composable
    fun mostrarMedicamentos(onCerrar: () -> Unit) {
        mostrarAviso("Medicamentos", onCerrar)
    }

    // Muestra el apartado de Antecedentes.
    @Composable
    fun mostrarAntecedentes(onCerrar: () -> Unit) {
        mostrarAviso("Antecedentes", onCerrar)
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
