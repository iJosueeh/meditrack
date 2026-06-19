package com.utp.meditrackapp.application.dto;

/**
 * Resumen de un producto con bajo stock.
 */
public record MedicamentoResumenDTO(
    String codigo,
    String nombre,
    String categoria,
    int stockActual,
    int stockMinimo,
    String estado,
    String valorFormateado
) {}
