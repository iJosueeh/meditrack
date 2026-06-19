package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Atencion;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de Atenciones.
 */
public interface AtencionRepository {

    Atencion save(Connection conn, Atencion atencion);

    AtencionDetalle saveDetalle(Connection conn, AtencionDetalle detalle);

    boolean existeReceta(String sedeId, String numeroReceta);

    List<Atencion> findAll();

    Optional<Atencion> findById(String id);

    List<Atencion> findByPaciente(String pacienteId);

    List<Atencion> findByReceta(String sedeId, String numeroReceta);

    List<Atencion> findBySede(String sedeId);
}
