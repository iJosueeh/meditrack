package com.utp.meditrackapp.domain.entities;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Entidad de dominio Lote con comportamiento de negocio.
 * Refactorizada desde core.models.entity.Lote
 */
public class Lote {
    private String id;
    private String productoId;
    private String sedeId;
    private String numeroLote;
    private LocalDate fechaVencimiento;
    private LocalDate fechaFabricacion;
    private int cantidad;

    // Transient fields for UI (mantenidos para compatibilidad)
    private String productoNombre;
    private String codigoDigemid;

    public Lote() {
    }

    // === Comportamiento de Dominio ===

    /**
     * Indica si el lote está vencido (fecha de vencimiento pasada).
     */
    public boolean isVencido() {
        return fechaVencimiento != null && fechaVencimiento.isBefore(LocalDate.now());
    }

    /**
     * Calcula los días restantes para vencer.
     * Retorna valor negativo si ya venció.
     */
    public long diasParaVencer() {
        if (fechaVencimiento == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimiento);
    }

    /**
     * Indica si el lote vence pronto (dentro de N días).
     */
    public boolean venceDentroDe(int dias) {
        return diasParaVencer() >= 0 && diasParaVencer() <= dias;
    }

    /**
     * Verifica si el lote tiene stock disponible para dispensar.
     */
    public boolean tieneStockDisponible(int cantidadSolicitada) {
        return cantidad >= cantidadSolicitada && cantidadSolicitada > 0;
    }

    /**
     * Verifica si el lote puede dispensar una cantidad específica.
     * Combina verificación de stock y vencimiento.
     */
    public boolean puedeDispensar(int cantidadSolicitada) {
        return tieneStockDisponible(cantidadSolicitada) && !isVencido();
    }

    /**
     * Descuenta stock del lote (operación de dominio).
     * Valida que haya stock suficiente antes de descontar.
     *
     * @throws IllegalArgumentException si la cantidad es inválida o insuficiente
     */
    public void descontarStock(int cantidadADescontar) {
        if (cantidadADescontar <= 0) {
            throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a cero.");
        }
        if (cantidad < cantidadADescontar) {
            throw new IllegalArgumentException("Stock insuficiente. Disponible: " + cantidad + ", Solicitado: " + cantidadADescontar);
        }
        this.cantidad -= cantidadADescontar;
    }

    /**
     * Agrega stock al lote (operación de dominio).
     *
     * @throws IllegalArgumentException si la cantidad es inválida
     */
    public void agregarStock(int cantidadAAgregar) {
        if (cantidadAAgregar <= 0) {
            throw new IllegalArgumentException("La cantidad a agregar debe ser mayor a cero.");
        }
        this.cantidad += cantidadAAgregar;
    }

    // === Getters y Setters ===

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

    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }

    public String getCodigoDigemid() { return codigoDigemid; }
    public void setCodigoDigemid(String codigoDigemid) { this.codigoDigemid = codigoDigemid; }
}
