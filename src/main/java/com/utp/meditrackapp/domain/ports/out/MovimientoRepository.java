package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Movimiento;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de salida para la persistencia de Movimientos de Inventario.
 */
public interface MovimientoRepository {

    Movimiento save(Connection conn, Movimiento movimiento);

    void deleteById(Connection conn, String id);

    void updateObservacion(Connection conn, String id, String observacion);

    List<Movimiento> findBySede(String sedeId, String tipoId, String buscar);

    List<Movimiento> findByFilters(String sedeId, String tipoId, String buscar, LocalDate desde, LocalDate hasta);

    List<Movimiento> findAll();
}
