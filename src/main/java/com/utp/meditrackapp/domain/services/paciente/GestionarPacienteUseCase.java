package com.utp.meditrackapp.domain.services.paciente;

import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.domain.ports.out.AtencionRepository;
import com.utp.meditrackapp.domain.ports.out.PacienteRepository;

import java.util.List;

/**
 * Caso de uso: Gestionar pacientes (CRUD + validaciones).
 * Centraliza la lógica que estaba en PacienteService.
 */
public class GestionarPacienteUseCase {
    private final PacienteRepository pacienteRepository;
    private final AtencionRepository atencionRepository;

    public GestionarPacienteUseCase(PacienteRepository pacienteRepository,
                                    AtencionRepository atencionRepository) {
        this.pacienteRepository = pacienteRepository;
        this.atencionRepository = atencionRepository;
    }

    /**
     * Lista todos los pacientes activos.
     */
    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> listarPacientesPorSede(String sedeId) {
        return pacienteRepository.findBySede(sedeId);
    }

    public List<Paciente> buscarPacientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listarPacientes();
        }
        return pacienteRepository.findByQuery(query.trim());
    }

    /**
     * Guarda o actualiza un paciente aplicando validaciones de dominio.
     *
     * @param paciente El paciente a guardar
     * @return "OK" si fue exitoso, o un mensaje de error
     */
    public String guardarPaciente(Paciente paciente) {
        // Validar usando el comportamiento de dominio
        String validation = paciente.validate();
        if (validation != null) {
            return validation;
        }

        // Verificar documento duplicado
        Paciente existente = pacienteRepository.findByDocumento(paciente.getNumeroDocumento());
        if (existente != null && (paciente.getId() == null || !existente.getId().equals(paciente.getId()))) {
            return "Error: Ya existe un paciente registrado con el número de documento " + paciente.getNumeroDocumento();
        }

        try {
            if (paciente.getId() == null || paciente.getId().trim().isEmpty()) {
                pacienteRepository.save(paciente);
            } else {
                pacienteRepository.update(paciente);
            }
            return "OK";
        } catch (Exception e) {
            return "Error técnico al guardar en la base de datos: " + e.getMessage();
        }
    }

    /**
     * Elimina un paciente (borrado lógico si tiene historial, físico si no).
     *
     * @param id ID del paciente
     * @return "OK" si fue exitoso, o un mensaje de error
     */
    public String eliminarPaciente(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "ID de paciente no válido.";
        }

        try {
            var historial = atencionRepository.findByPaciente(id);
            if (historial != null && !historial.isEmpty()) {
                return "NO_HISTORY";
            }
        } catch (Exception e) {
            return "Error al verificar historial del paciente: " + e.getMessage();
        }

        boolean success = pacienteRepository.hardDelete(id);
        return success ? "OK" : "Error técnico al eliminar el paciente.";
    }

    public String desactivarPaciente(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "ID de paciente no válido.";
        }
        try {
            boolean success = pacienteRepository.softDelete(id);
            return success ? "OK" : "Error técnico al desactivar el paciente.";
        } catch (Exception e) {
            return "Error al desactivar paciente: " + e.getMessage();
        }
    }

    public String reactivarPaciente(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "ID de paciente no válido.";
        }
        try {
            boolean success = pacienteRepository.reactivar(id);
            return success ? "OK" : "Error técnico al reactivar el paciente.";
        } catch (Exception e) {
            return "Error al reactivar paciente: " + e.getMessage();
        }
    }

    /**
     * Obtiene un paciente por ID.
     */
    public java.util.Optional<Paciente> obtenerPorId(String id) {
        return pacienteRepository.findById(id);
    }

    /**
     * Obtiene el contador de pacientes totales por sede.
     */
    public int getContadorTotal(String sedeId) {
        return pacienteRepository.countBySede(sedeId);
    }
}
