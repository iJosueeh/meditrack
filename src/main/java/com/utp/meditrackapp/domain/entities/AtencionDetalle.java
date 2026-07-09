package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio Atención Detalle con comportamiento de negocio.
 * Refactorizada desde core.models.entity.AtencionDetalle
 */
public class AtencionDetalle {
    private String id;
    private String atencionId;
    private String loteId;
    private int cantidadEntregada;

    // Transient fields for UI
    private String productoId;
    private String productoNombre;
    private String loteNumero;
    private String fechaVencimiento;

    public AtencionDetalle() {
    }

    // === Comportamiento de Dominio ===

    /**
     * Valida que el detalle tenga todos los campos obligatorios.
     *
     * @return null si es válido, mensaje de error si no lo es
     */
    public String validate() {
        if (loteId == null || loteId.trim().isEmpty()) {
            return "El lote es obligatorio.";
        }
        if (cantidadEntregada <= 0) {
            return "La cantidad entregada debe ser mayor a cero.";
        }
        return null;
    }

    /**
     * Obtiene un resumen del detalle para UI.
     */
    public String getResumen() {
        StringBuilder sb = new StringBuilder();
        if (productoNombre != null) sb.append(productoNombre);
        if (loteNumero != null) sb.append(" (Lote: ").append(loteNumero).append(")");
        sb.append(" - Cantidad: ").append(cantidadEntregada);
        return sb.toString();
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAtencionId() { return atencionId; }
    public void setAtencionId(String atencionId) { this.atencionId = atencionId; }

    public String getLoteId() { return loteId; }
    public void setLoteId(String loteId) { this.loteId = loteId; }

    public int getCantidadEntregada() { return cantidadEntregada; }
    public void setCantidadEntregada(int cantidadEntregada) { this.cantidadEntregada = cantidadEntregada; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getLoteNumero() { return loteNumero; }
    public void setLoteNumero(String loteNumero) { this.loteNumero = loteNumero; }

    public String getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
}
