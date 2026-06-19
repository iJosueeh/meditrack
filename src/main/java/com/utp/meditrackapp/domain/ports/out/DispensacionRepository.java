package com.utp.meditrackapp.domain.ports.out;

import java.time.LocalDate;
import java.util.List;

/**
 * Puerto de salida para datos de dispensaciones.
 * Centraliza las consultas que estaban en AtencionDAO.listarDispensacionesReporte().
 */
public interface DispensacionRepository {

    /**
     * Obtiene las dispensaciones para reporte en un rango de fechas.
     *
     * @param sedeId ID de la sede
     * @param desde  Fecha de inicio (puede ser null)
     * @param hasta  Fecha de fin (puede ser null)
     * @return Lista de mapas con los datos de dispensación
     */
    List<java.util.Map<String, Object>> findByRangoFechas(String sedeId, LocalDate desde, LocalDate hasta);
}
