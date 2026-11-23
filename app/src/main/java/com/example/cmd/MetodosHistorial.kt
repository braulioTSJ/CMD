package com.example.cmd

import androidx.compose.runtime.mutableStateListOf
import java.util.concurrent.atomic.AtomicLong

// Campos comunes para identificar y mostrar un registro en la tarjeta padre.
interface RegistroHistorialBase {
    val idLocal: Long
    val perfilNombre: String
    val perfilId: String
    val fechaRegistro: String
    val resumen: String
}

// Registro propio del submódulo Accidentes.
data class AccidenteRegistro(
    override val idLocal: Long = 0,
    override val perfilNombre: String,
    val fechaAccidente: String,
    val queOcurrio: String,
    val lesion: String,
    val atencionRecibida: String,
    val fotoUri: String? = null,
    override val perfilId: String = perfilNombre
) : RegistroHistorialBase {
    override val fechaRegistro: String get() = fechaAccidente
    override val resumen: String get() = queOcurrio
}

// Registro propio del submódulo Cirugías.
data class CirugiaRegistro(
    override val idLocal: Long = 0,
    override val perfilNombre: String,
    val fechaCirugia: String,
    val procedimiento: String,
    val diagnosticoMotivo: String,
    val medicoCirujano: String,
    val hospital: String,
    val complicaciones: String,
    override val perfilId: String = perfilNombre
) : RegistroHistorialBase {
    override val fechaRegistro: String get() = fechaCirugia
    override val resumen: String get() = procedimiento
}

// Registro propio del submódulo Consultas.
data class ConsultaRegistro(
    override val idLocal: Long = 0,
    override val perfilNombre: String,
    val fechaConsulta: String,
    val especialidad: String,
    val motivoConsulta: String,
    val diagnostico: String,
    val medicoTratante: String,
    override val perfilId: String = perfilNombre
) : RegistroHistorialBase {
    override val fechaRegistro: String get() = fechaConsulta
    override val resumen: String get() = motivoConsulta
}

// Registro propio del submódulo Enfermedades.
data class EnfermedadRegistro(
    override val idLocal: Long = 0,
    override val perfilNombre: String,
    val nombreEnfermedad: String,
    val fechaDiagnostico: String,
    val estado: String,
    val tratamientoActual: String,
    val notas: String,
    override val perfilId: String = perfilNombre
) : RegistroHistorialBase {
    override val fechaRegistro: String get() = fechaDiagnostico
    override val resumen: String get() = nombreEnfermedad
}

// Registro propio del submódulo Hospitalizaciones.
data class HospitalizacionRegistro(
    override val idLocal: Long = 0,
    override val perfilNombre: String,
    val hospital: String,
    val fechaIngreso: String,
    val fechaAlta: String,
    val motivo: String,
    val diagnostico: String,
    val tratamientoProcedimiento: String,
    val medicoResponsable: String,
    override val perfilId: String = perfilNombre
) : RegistroHistorialBase {
    override val fechaRegistro: String get() = fechaIngreso
    override val resumen: String get() = motivo
}

// Repositorio temporal separado por tipos de registro, listo para sustituirse por almacenamiento real.
object RepositorioHistorialLocal {
    private val siguienteId = AtomicLong(1L)
    private val accidentes = mutableStateListOf<AccidenteRegistro>()
    private val cirugias = mutableStateListOf<CirugiaRegistro>()
    private val consultas = mutableStateListOf<ConsultaRegistro>()
    private val enfermedades = mutableStateListOf<EnfermedadRegistro>()
    private val hospitalizaciones = mutableStateListOf<HospitalizacionRegistro>()

    fun consultarAccidentes(perfilId: String) = accidentes.delPerfil(perfilId)
    fun consultarCirugias(perfilId: String) = cirugias.delPerfil(perfilId)
    fun consultarConsultas(perfilId: String) = consultas.delPerfil(perfilId)
    fun consultarEnfermedades(perfilId: String) = enfermedades.delPerfil(perfilId)
    fun consultarHospitalizaciones(perfilId: String) = hospitalizaciones.delPerfil(perfilId)

    fun consultarTodos(perfilId: String): List<RegistroHistorialBase> = buildList {
        addAll(consultarAccidentes(perfilId))
        addAll(consultarCirugias(perfilId))
        addAll(consultarConsultas(perfilId))
        addAll(consultarEnfermedades(perfilId))
        addAll(consultarHospitalizaciones(perfilId))
    }.sortedByDescending { it.fechaRegistro }

    fun eliminarPerfil(perfilId: String) {
        accidentes.removeAll { it.perfilId == perfilId }
        cirugias.removeAll { it.perfilId == perfilId }
        consultas.removeAll { it.perfilId == perfilId }
        enfermedades.removeAll { it.perfilId == perfilId }
        hospitalizaciones.removeAll { it.perfilId == perfilId }
    }

    // Actualiza el nombre mostrado sin cambiar la identidad asociada a los registros.
    fun actualizarNombrePerfil(perfilId: String, nuevoNombre: String) {
        accidentes.indices.forEach { i -> if (accidentes[i].perfilId == perfilId) accidentes[i] = accidentes[i].copy(perfilNombre = nuevoNombre) }
        cirugias.indices.forEach { i -> if (cirugias[i].perfilId == perfilId) cirugias[i] = cirugias[i].copy(perfilNombre = nuevoNombre) }
        consultas.indices.forEach { i -> if (consultas[i].perfilId == perfilId) consultas[i] = consultas[i].copy(perfilNombre = nuevoNombre) }
        enfermedades.indices.forEach { i -> if (enfermedades[i].perfilId == perfilId) enfermedades[i] = enfermedades[i].copy(perfilNombre = nuevoNombre) }
        hospitalizaciones.indices.forEach { i -> if (hospitalizaciones[i].perfilId == perfilId) hospitalizaciones[i] = hospitalizaciones[i].copy(perfilNombre = nuevoNombre) }
    }

    fun agregarAccidente(registro: AccidenteRegistro) { accidentes.add(registro.copy(idLocal = siguienteId.getAndIncrement())) }
    fun agregarCirugia(registro: CirugiaRegistro) { cirugias.add(registro.copy(idLocal = siguienteId.getAndIncrement())) }
    fun agregarConsulta(registro: ConsultaRegistro) { consultas.add(registro.copy(idLocal = siguienteId.getAndIncrement())) }
    fun agregarEnfermedad(registro: EnfermedadRegistro) { enfermedades.add(registro.copy(idLocal = siguienteId.getAndIncrement())) }
    fun agregarHospitalizacion(registro: HospitalizacionRegistro) { hospitalizaciones.add(registro.copy(idLocal = siguienteId.getAndIncrement())) }

    fun actualizarAccidente(registro: AccidenteRegistro) { accidentes.reemplazar(registro) }
    fun actualizarCirugia(registro: CirugiaRegistro) { cirugias.reemplazar(registro) }
    fun actualizarConsulta(registro: ConsultaRegistro) { consultas.reemplazar(registro) }
    fun actualizarEnfermedad(registro: EnfermedadRegistro) { enfermedades.reemplazar(registro) }
    fun actualizarHospitalizacion(registro: HospitalizacionRegistro) { hospitalizaciones.reemplazar(registro) }

    fun eliminarAccidente(id: Long) { accidentes.eliminar(id) }
    fun eliminarCirugia(id: Long) { cirugias.eliminar(id) }
    fun eliminarConsulta(id: Long) { consultas.eliminar(id) }
    fun eliminarEnfermedad(id: Long) { enfermedades.eliminar(id) }
    fun eliminarHospitalizacion(id: Long) { hospitalizaciones.eliminar(id) }

    // Consulta y ordena registros por perfil, con la fecha más reciente primero.
    private fun <T : RegistroHistorialBase> List<T>.delPerfil(perfilId: String) =
        filter { it.perfilId == perfilId }.sortedByDescending { it.fechaRegistro }

    // Reemplaza un registro por su identificador local.
    private fun <T : RegistroHistorialBase> MutableList<T>.reemplazar(registro: T) {
        val indice = indexOfFirst { it.idLocal == registro.idLocal }
        if (indice >= 0) this[indice] = registro
    }

    // Quita de la lista temporal el registro confirmado por el usuario.
    private fun <T : RegistroHistorialBase> MutableList<T>.eliminar(id: Long) {
        removeAll { it.idLocal == id }
    }
}

// Métodos nombrados por submódulo para que la pantalla y el backend identifiquen cada operación.
class MetodosHistorial {
    // Obtiene los registros del tipo elegido conservando sus modelos tipados.
    fun consultarPorSubmodulo(perfil: String, submodulo: String): List<RegistroHistorialBase> = when (submodulo) {
        "Eventos", "Accidentes" -> consultarAccidentes(perfil)
        "Cirugías" -> consultarCirugias(perfil)
        "Consultas" -> consultarConsultas(perfil)
        "Enfermedades" -> consultarEnfermedades(perfil)
        "Hospitalizaciones" -> consultarHospitalizaciones(perfil)
        else -> emptyList()
    }

    // Envía la eliminación al método específico del tipo de evento.
    fun eliminar(registro: RegistroHistorialBase) {
        when (registro) {
            is AccidenteRegistro -> eliminarAccidente(registro.idLocal)
            is CirugiaRegistro -> eliminarCirugia(registro.idLocal)
            is ConsultaRegistro -> eliminarConsulta(registro.idLocal)
            is EnfermedadRegistro -> eliminarEnfermedad(registro.idLocal)
            is HospitalizacionRegistro -> eliminarHospitalizacion(registro.idLocal)
        }
    }

    fun consultarAccidentes(perfil: String) = RepositorioHistorialLocal.consultarAccidentes(perfil)
    fun consultarCirugias(perfil: String) = RepositorioHistorialLocal.consultarCirugias(perfil)
    fun consultarConsultas(perfil: String) = RepositorioHistorialLocal.consultarConsultas(perfil)
    fun consultarEnfermedades(perfil: String) = RepositorioHistorialLocal.consultarEnfermedades(perfil)
    fun consultarHospitalizaciones(perfil: String) = RepositorioHistorialLocal.consultarHospitalizaciones(perfil)
    fun consultarTodos(perfil: String) = RepositorioHistorialLocal.consultarTodos(perfil)
    fun eliminarPerfil(perfilId: String) = RepositorioHistorialLocal.eliminarPerfil(perfilId)
    fun actualizarNombrePerfil(perfilId: String, nuevoNombre: String) = RepositorioHistorialLocal.actualizarNombrePerfil(perfilId, nuevoNombre)

    fun agregarAccidente(registro: AccidenteRegistro) = RepositorioHistorialLocal.agregarAccidente(registro)
    fun agregarCirugia(registro: CirugiaRegistro) = RepositorioHistorialLocal.agregarCirugia(registro)
    fun agregarConsulta(registro: ConsultaRegistro) = RepositorioHistorialLocal.agregarConsulta(registro)
    fun agregarEnfermedad(registro: EnfermedadRegistro) = RepositorioHistorialLocal.agregarEnfermedad(registro)
    fun agregarHospitalizacion(registro: HospitalizacionRegistro) = RepositorioHistorialLocal.agregarHospitalizacion(registro)

    fun actualizarAccidente(registro: AccidenteRegistro) = RepositorioHistorialLocal.actualizarAccidente(registro)
    fun actualizarCirugia(registro: CirugiaRegistro) = RepositorioHistorialLocal.actualizarCirugia(registro)
    fun actualizarConsulta(registro: ConsultaRegistro) = RepositorioHistorialLocal.actualizarConsulta(registro)
    fun actualizarEnfermedad(registro: EnfermedadRegistro) = RepositorioHistorialLocal.actualizarEnfermedad(registro)
    fun actualizarHospitalizacion(registro: HospitalizacionRegistro) = RepositorioHistorialLocal.actualizarHospitalizacion(registro)

    fun eliminarAccidente(id: Long) = RepositorioHistorialLocal.eliminarAccidente(id)
    fun eliminarCirugia(id: Long) = RepositorioHistorialLocal.eliminarCirugia(id)
    fun eliminarConsulta(id: Long) = RepositorioHistorialLocal.eliminarConsulta(id)
    fun eliminarEnfermedad(id: Long) = RepositorioHistorialLocal.eliminarEnfermedad(id)
    fun eliminarHospitalizacion(id: Long) = RepositorioHistorialLocal.eliminarHospitalizacion(id)
}
