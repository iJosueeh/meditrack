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

    public Movimiento() {
    }

    public Movimiento(String id, String tipoId, String motivoId, String sedeId, String usuarioId, String loteId, int cantidad, String observacion, LocalDateTime fechaRegistro) {
        this.id = id;
        this.tipoId = tipoId;
        this.motivoId = motivoId;
        this.sedeId = sedeId;
        this.usuarioId = usuarioId;
        this.loteId = loteId;
        this.cantidad = cantidad;
        this.observacion = observacion;
        this.fechaRegistro = fechaRegistro;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTipoId() {
        return tipoId;
    }

    public void setTipoId(String tipoId) {
        this.tipoId = tipoId;
    }

    public String getMotivoId() {
        return motivoId;
    }

    public void setMotivoId(String motivoId) {
        this.motivoId = motivoId;
    }

    public String getSedeId() {
        return sedeId;
    }

    public void setSedeId(String sedeId) {
        this.sedeId = sedeId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getLoteId() {
        return loteId;
    }

    public void setLoteId(String loteId) {
        this.loteId = loteId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}