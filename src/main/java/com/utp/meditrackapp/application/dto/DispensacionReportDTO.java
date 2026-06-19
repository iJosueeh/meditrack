package com.utp.meditrackapp.application.dto;

import java.time.LocalDate;

/**
 * DTO para información de dispensación en reportes.
 * Refactorizado desde core.models.dto.DispensacionReportItem
 */
public class DispensacionReportDTO {
    private final LocalDate fecha;
    private final String paciente;
    private final String numeroReceta;
    private final String producto;
    private final String lote;
    private final int cantidad;

    public DispensacionReportDTO(LocalDate fecha, String paciente, String numeroReceta,
                                 String producto, String lote, int cantidad) {
        this.fecha = fecha;
        this.paciente = paciente;
        this.numeroReceta = numeroReceta;
        this.producto = producto;
        this.lote = lote;
        this.cantidad = cantidad;
    }

    // === Comportamiento de Negocio ===

    /**
     * Obtiene el resumen de dispensación para UI.
     */
    public String getResumen() {
        return producto + " (" + lote + ") x" + cantidad;
    }

    // === Getters ===

    public LocalDate getFecha() { return fecha; }
    public String getPaciente() { return paciente; }
    public String getNumeroReceta() { return numeroReceta; }
    public String getProducto() { return producto; }
    public String getLote() { return lote; }
    public int getCantidad() { return cantidad; }
}
