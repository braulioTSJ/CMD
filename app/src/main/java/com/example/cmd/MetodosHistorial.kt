package com.example.cmd

import androidx.compose.runtime.mutableStateListOf
import java.util.concurrent.atomic.AtomicLong

// Campos comunes para identificar y mostrar un registro en la tarjeta padre.
interface RegistroHistorialBase {
    val idLocal: Long
    val perfilNombre: String
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
    val fotoUri: String? = null
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
    val complicaciones: String
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
    val medicoTratante: String
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
    val notas: String
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
    val medicoResponsable: String
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

    fun consultarAccidentes(perfil: String) = accidentes.delPerfil(perfil)
    fun consultarCirugias(perfil: String) = cirugias.delPerfil(perfil)
    fun consultarConsultas(perfil: String) = consultas.delPerfil(perfil)
    fun consultarEnfermedades(perfil: String) = enfermedades.delPerfil(perfil)
    fun consultarHospitalizaciones(perfil: String) = hospitalizaciones.delPerfil(perfil)

    fun consultarTodos(perfil: String): List<RegistroHistorialBase> = buildList {
        addAll(consultarAccidentes(perfil))
        addAll(consultarCirugias(perfil))
        addAll(consultarConsultas(perfil))
        addAll(consultarEnfermedades(perfil))
        addAll(consultarHospitalizaciones(perfil))
    }.sortedByDescending { it.fechaRegistro }

    fun eliminarPerfil(perfil: String) {
        accidentes.removeAll { it.perfilNombre == perfil }
        cirugias.removeAll { it.perfilNombre == perfil }
        consultas.removeAll { it.perfilNombre == perfil }
        enfermedades.removeAll { it.perfilNombre == perfil }
        hospitalizaciones.removeAll { it.perfilNombre == perfil }
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
    private fun <T : RegistroHistorialBase> List<T>.delPerfil(perfil: String) =
        filter { it.perfilNombre == perfil }.sortedByDescending { it.fechaRegistro }

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
    fun eliminarPerfil(perfil: String) = RepositorioHistorialLocal.eliminarPerfil(perfil)

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
