package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Paciente;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de Pacientes.
 */
public interface PacienteRepository {

    List<Paciente> findAll();

    List<Paciente> findBySede(String sedeId);

    List<Paciente> findByQuery(String query);

    Optional<Paciente> findById(String id);

    Paciente findByDocumento(String numeroDocumento);

    Paciente save(Paciente paciente);

    Paciente update(Paciente paciente);

    boolean softDelete(String id);

    boolean reactivar(String id);

    boolean hardDelete(String id);

    int countBySede(String sedeId);
}
