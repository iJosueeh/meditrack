package com.utp.meditrackapp.core.models.entity;

import java.time.LocalDate;

public class Lote {
    private String id;
    private String productoId;
    private String sedeId;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private LocalDate fechaFabricacion;
    private int cantidad;

    public Lote() {}

    public Lote(String id, String productoId, String sedeId, String numeroLote, LocalDate fechaVencimiento, LocalDate fechaFabricacion, int cantidad) {
        this.id = id;
        this.productoId = productoId;
        this.sedeId = sedeId;
        this.numeroLote = numeroLote;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaFabricacion = fechaFabricacion;
        this.cantidad = cantidad;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getSedeId() { return sedeId; }
    public void setSedeId(String sedeId) { this.sedeId = sedeId; }

    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public LocalDate getFechaFabricacion() { return fechaFabricacion; }
    public void setFechaFabricacion(LocalDate fechaFabricacion) { this.fechaFabricacion = fechaFabricacion; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public boolean verificarFEFO() {
        // Lógica para verificar First Expired, First Out
        if (fechaVencimiento == null) return false;
        return fechaVencimiento.isBefore(LocalDate.now().plusMonths(6));
    }
}
