package com.utp.meditrackapp.application.dto;

import java.util.List;
import java.util.Map;

/**
 * Datos para el reporte de movimientos.
 */
public record ReporteMovimientosDTO(
    String fechaGeneracion,
    String generadoPor,
    String sede,
    String rangoFechas,
    int totalMovimientos,
    int cantidadTotal,
    List<Map<String, Object>> items
) {}
