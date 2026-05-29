package com.utp.meditrackapp.features.patients.repository;

import com.utp.meditrackapp.core.models.entity.Paciente;
import java.util.List;


public interface PacienteRepository {
    List<Paciente> findAll();
    List<Paciente> findByQuery(String query);
    Paciente findByDocumento(String numeroDocumento);
    boolean save(Paciente paciente);
    boolean update(Paciente paciente);
    boolean delete(String id);
}
