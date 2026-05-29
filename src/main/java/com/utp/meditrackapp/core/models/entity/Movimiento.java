package com.utp.meditrackapp.core.models.entity;

import java.time.LocalDateTime;

public class Movimiento {
    private String id;
    private String tipoId;
    private String motivoId;
    private String sedeId;
    private String usuarioId;
    private String loteId;
    private int cantidad;
    private String observacion;
    private LocalDateTime fechaRegistro;

    // Transient fields for UI
    private String tipoNombre;
    private String motivoNombre;
    private String productoNombre;
    private String numeroLote;

    public Movimiento() {
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTipoId() { return tipoId; }
    public void setTipoId(String tipoId) { this.tipoId = tipoId; }
    public String getMotivoId() { return motivoId; }
    public void setMotivoId(String motivoId) { this.motivoId = motivoId; }
    public String getSedeId() { return sedeId; }
    public void setSedeId(String sedeId) { this.sedeId = sedeId; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getLoteId() { return loteId; }
    public void setLoteId(String loteId) { this.loteId = loteId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getTipoNombre() { return tipoNombre; }
    public void setTipoNombre(String tipoNombre) { this.tipoNombre = tipoNombre; }
    public String getMotivoNombre() { return motivoNombre; }
    public void setMotivoNombre(String motivoNombre) { this.motivoNombre = motivoNombre; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
}