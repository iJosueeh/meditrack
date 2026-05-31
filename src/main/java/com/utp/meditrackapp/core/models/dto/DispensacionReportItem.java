package com.utp.meditrackapp.core.models.dto;

import java.time.LocalDateTime;

public class DispensacionReportItem {
    private LocalDateTime fecha;
    private String paciente;
    private String numeroReceta;
    private String producto;
    private String lote;
    private int cantidad;

    public DispensacionReportItem() {}

    public DispensacionReportItem(LocalDateTime fecha, String paciente, String numeroReceta, String producto, String lote, int cantidad) {
        this.fecha = fecha;
        this.paciente = paciente;
        this.numeroReceta = numeroReceta;
        this.producto = producto;
        this.lote = lote;
        this.cantidad = cantidad;
    }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getPaciente() { return paciente; }
    public void setPaciente(String paciente) { this.paciente = paciente; }

    public String getNumeroReceta() { return numeroReceta; }
    public void setNumeroReceta(String numeroReceta) { this.numeroReceta = numeroReceta; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
}
