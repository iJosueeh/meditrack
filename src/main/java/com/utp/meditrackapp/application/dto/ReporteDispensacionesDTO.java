package com.utp.meditrackapp.application.dto;

import java.util.List;
import java.util.Map;

/**
 * Datos para el reporte de dispensaciones.
 */
public record ReporteDispensacionesDTO(
    String fechaGeneracion,
    String generadoPor,
    String sede,
    String rangoFechas,
    int totalAtenciones,
    int totalMedicamentos,
    List<Map<String, Object>> items
) {}
