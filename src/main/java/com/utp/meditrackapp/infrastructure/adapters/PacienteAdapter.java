package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.domain.services.paciente.GestionarPacienteUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcAtencionRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcPacienteRepository;

import java.util.List;

/**
 * Adaptador que puentea el PacienteService antiguo con el nuevo GestionarPacienteUseCase.
 * Permite que los controladores existentes sigan funcionando mientras se migra gradualmente.
 */
public class PacienteAdapter {
    private final GestionarPacienteUseCase useCase;
    private final JdbcPacienteRepository pacienteRepository;

    public PacienteAdapter() {
        this.pacienteRepository = new JdbcPacienteRepository();
        this.useCase = new GestionarPacienteUseCase(
            this.pacienteRepository,
            new JdbcAtencionRepository()
        );
    }

    /**
     * Lista todos los pacientes activos (retorna domain entity).
     */
    public List<Paciente> listarPacientes() {
        return useCase.listarPacientes();
    }

    /**
     * Busca pacientes por query (retorna domain entity).
     */
    public List<Paciente> buscarPacientes(String query) {
        return useCase.buscarPacientes(query);
    }

    /**
     * Guarda o actualiza un paciente.
     */
    public String guardarPaciente(Paciente domainPaciente) {
        return useCase.guardarPaciente(domainPaciente);
    }

    /**
     * Elimina un paciente (con lógica de borrado inteligente).
     */
    public String eliminarPaciente(String id) {
        return useCase.eliminarPaciente(id);
    }

    public String reactivarPaciente(String id) {
        return useCase.reactivarPaciente(id);
    }

    /**
     * Obtiene el contador de pacientes totales por sede.
     */
    public int getContadorTotal() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return 0;
        return useCase.getContadorTotal(user.getSedeId());
    }

    /**
     * Obtiene el conteo de atenciones de hoy.
     */
    public int getAtendidosHoy() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return 0;
        return pacienteRepository.countTodayAttentions(user.getSedeId());
    }

    /**
     * Obtiene el conteo de nuevos pacientes del mes.
     */
    public int getNuevosDelMes() {
        var user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return 0;
        return pacienteRepository.countNewPatientsMonth(user.getSedeId());
    }

}
