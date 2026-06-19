package com.utp.meditrackapp.application.dto;

import java.time.LocalDate;

/**
 * DTO para información de movimientos de inventario.
 */
public class MovimientoDTO {
    private final String id;
    private final LocalDate fecha;
    private final String tipo;
    private final String producto;
    private final String numeroLote;
    private final int cantidad;
    private final String motivo;
    private final String observacion;

    public MovimientoDTO(String id, LocalDate fecha, String tipo, String producto,
                         String numeroLote, int cantidad, String motivo, String observacion) {
        this.id = id;
        this.fecha = fecha;
        this.tipo = tipo;
        this.producto = producto;
        this.numeroLote = numeroLote;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.observacion = observacion;
    }

    // === Comportamiento de Negocio ===

    public boolean isEntrada() {
        return tipo != null && tipo.equalsIgnoreCase("ENTRADA");
    }

    public boolean isSalida() {
        return tipo != null && tipo.equalsIgnoreCase("SALIDA");
    }

    public String getResumen() {
        return tipo + " - " + producto + " (" + numeroLote + ") x" + cantidad;
    }

    // === Getters ===

    public String getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getProducto() { return producto; }
    public String getNumeroLote() { return numeroLote; }
    public int getCantidad() { return cantidad; }
    public String getMotivo() { return motivo; }
    public String getObservacion() { return observacion; }
}
