package com.example.cmd

/** Datos calculados para la ficha de resumen del perfil seleccionado. */
data class ResumenMedicoData(
    val alergias: String,
    val tipoSangre: String,
    val medicamentos: List<String>,
    val antecedentes: List<String>
)

/** Capa de consulta en memoria; se puede sustituir por una API/base local más adelante. */
class ResumenMedicoMetodos(
    private val historial: MetodosHistorial = MetodosHistorial()
) {
    fun obtenerResumen(nombrePerfil: String, datosPerfil: Map<String, String>?): ResumenMedicoData {
        val registros = historial.consultarTodos(nombrePerfil)
        val medicamentos = registros.filterIsInstance<EnfermedadRegistro>()
            .filter { !it.estado.equals("Resuelta", ignoreCase = true) }
            .mapNotNull { registro ->
                registro.tratamientoActual.trim().takeIf(String::isNotBlank)?.let {
                    "${registro.nombreEnfermedad}: $it"
                }
            }
            .distinct()

        val antecedentes = registros.mapNotNull(::describirAntecedente)

        return ResumenMedicoData(
            alergias = datosPerfil?.get("Alergias").valorOMensaje(),
            tipoSangre = datosPerfil?.get("Tipo de sangre").valorOMensaje(),
            medicamentos = medicamentos,
            antecedentes = antecedentes
        )
    }

    private fun describirAntecedente(registro: RegistroHistorialBase): String? {
        val tipo = when (registro) {
            is AccidenteRegistro -> "Accidente"
            is CirugiaRegistro -> "Cirugía"
            is ConsultaRegistro -> "Consulta"
            is EnfermedadRegistro -> "Enfermedad"
            is HospitalizacionRegistro -> "Hospitalización"
            else -> return null
        }
        val detalle = registro.resumen.trim()
        return "$tipo · ${registro.fechaRegistro}" + if (detalle.isBlank()) "" else " · $detalle"
    }

    private fun String?.valorOMensaje(): String = this
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: "No registrado"
}
