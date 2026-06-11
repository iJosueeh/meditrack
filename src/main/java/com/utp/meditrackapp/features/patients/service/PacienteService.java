package com.utp.meditrackapp.features.patients.service;

import com.utp.meditrackapp.core.models.entity.Paciente;
import com.utp.meditrackapp.features.patients.repository.PacienteRepository;
import com.utp.meditrackapp.features.patients.repository.PacienteRepositoryImpl;
import java.util.List;


public class PacienteService {
    private final PacienteRepository pacienteRepository;

    public PacienteService() {
        this.pacienteRepository = new PacienteRepositoryImpl();
    }

    public List<Paciente> listarPacientes() {
        return pacienteRepository.findAll();
    }

    public List<Paciente> buscarPacientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listarPacientes();
        }
        return pacienteRepository.findByQuery(query.trim());
    }

    /**
     * Guarda o actualiza un paciente aplicando validaciones.
     */
    public String guardarPaciente(Paciente paciente) {
        if (paciente.getNombres() == null || paciente.getNombres().trim().isEmpty()) {
            return "Los nombres son obligatorios.";
        }
        if (paciente.getApellidos() == null || paciente.getApellidos().trim().isEmpty()) {
            return "Los apellidos son obligatorios.";
        }
        
        String validationMsg = validarDocumento(paciente.getTipoDocumento(), paciente.getNumeroDocumento());
        if (validationMsg != null) return validationMsg;

        Paciente existente = pacienteRepository.findByDocumento(paciente.getNumeroDocumento());
        if (existente != null && (paciente.getId() == null || !existente.getId().equals(paciente.getId()))) {
            return "Error: Ya existe un paciente registrado con el número de documento " + paciente.getNumeroDocumento();
        }

        boolean success;
        if (paciente.getId() == null || paciente.getId().trim().isEmpty()) {
            success = pacienteRepository.save(paciente);
        } else {
            success = pacienteRepository.update(paciente);
        }

        return success ? "OK" : "Error técnico al guardar en la base de datos.";
    }

    public String eliminarPaciente(String id) {
        if (id == null || id.trim().isEmpty()) return "ID de paciente no válido.";

        // Verificar si tiene atenciones registradas
        try {
            var historial = new com.utp.meditrackapp.features.attentions.dao.AtencionDAO().listarPorPaciente(id);
            if (historial != null && !historial.isEmpty()) {
                return "El paciente tiene " + historial.size() + " atención(es) registrada(s). No se puede desactivar.";
            }
        } catch (Exception e) {
            return "Error al verificar historial del paciente: " + e.getMessage();
        }

        return pacienteRepository.delete(id) ? "OK" : "Error técnico al desactivar el paciente.";
    }

    public int getContadorTotal() {
        var user = com.utp.meditrackapp.core.config.SessionManager.getInstance().getCurrentUser();
        if (user == null) return 0;
        String sedeId = user.getSedeId();
        if (pacienteRepository instanceof PacienteRepositoryImpl) {
            return new com.utp.meditrackapp.features.patients.Dao.PacienteDao().countTotal(sedeId);
        }
        return 0;
    }

    public int getAtendidosHoy() {
        var user = com.utp.meditrackapp.core.config.SessionManager.getInstance().getCurrentUser();
        if (user == null) return 0;
        return new com.utp.meditrackapp.features.patients.Dao.PacienteDao().countTodayAttentions(user.getSedeId());
    }

    public int getNuevosDelMes() {
        var user = com.utp.meditrackapp.core.config.SessionManager.getInstance().getCurrentUser();
        if (user == null) return 0;
        return new com.utp.meditrackapp.features.patients.Dao.PacienteDao().countNewPatientsMonth(user.getSedeId());
    }

    private String validarDocumento(String tipo, String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return "El número de documento es obligatorio.";
        }
        
        String limpio = numero.trim();
        
        if ("DNI".equalsIgnoreCase(tipo)) {
            if (!limpio.matches("\\d{8}")) {
                return "El DNI debe contener exactamente 8 dígitos numéricos.";
            }
        } else if ("CE".equalsIgnoreCase(tipo)) {
            if (!limpio.matches("[a-zA-Z0-9]{8,12}")) {
                return "El Carnet de Extranjería (CE) debe tener entre 8 y 12 caracteres alfanuméricos.";
            }
        } else {
            return "Tipo de documento no válido. Use DNI o CE.";
        }
        
        return null;
    }
}
