package com.utp.meditrackapp.application.dto;

import java.util.List;

/**
 * Datos para el reporte consolidado del Dashboard.
 */
public record ReporteConsolidadoDTO(
    String fechaGeneracion,
    String generadoPor,
    String sede,
    String valorInventario,
    String crecimientoMesAnterior,
    String alertasStock,
    String volumenMovimientos,
    String eficienciaOperativa,
    String graficoTendencia,
    String graficoCategorias,
    List<MedicamentoResumenDTO> productosBajoStock
) {}
