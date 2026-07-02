package com.utp.meditrackapp.domain.entities;

import java.time.LocalDateTime;

import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;

/**
 * Entidad de dominio Movimiento con comportamiento de negocio.
 * Refactorizada desde core.models.entity.Movimiento
 */
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

    // === Comportamiento de Dominio ===

    /**
     * Verifica si es un movimiento de entrada.
     */
    public boolean isEntrada() {
        return TipoMovimientoEnum.ENTRADA.getId().equals(tipoId);
    }

    /**
     * Verifica si es un movimiento de salida.
     */
    public boolean isSalida() {
        return TipoMovimientoEnum.SALIDA.getId().equals(tipoId);
    }

    /**
     * Obtiene la descripción completa del movimiento.
     */
    public String getDescripcionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (tipoNombre != null) sb.append(tipoNombre);
        if (motivoNombre != null) sb.append(" - ").append(motivoNombre);
        if (observacion != null && !observacion.isEmpty()) {
            sb.append(" (").append(observacion).append(")");
        }
        return sb.toString();
    }

    /**
     * Valida que el movimiento tenga todos los campos obligatorios.
     *
     * @return null si es válido, mensaje de error si no lo es
     */
    public String validate() {
        if (tipoId == null || tipoId.trim().isEmpty()) {
            return "El tipo de movimiento es obligatorio.";
        }
        if (motivoId == null || motivoId.trim().isEmpty()) {
            return "El motivo de movimiento es obligatorio.";
        }
        if (sedeId == null || sedeId.trim().isEmpty()) {
            return "La sede es obligatoria.";
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            return "El usuario es obligatorio.";
        }
        if (loteId == null || loteId.trim().isEmpty()) {
            return "El lote es obligatorio.";
        }
        if (cantidad <= 0) {
            return "La cantidad debe ser mayor a cero.";
        }
        return null;
    }

    // === Getters y Setters ===

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
