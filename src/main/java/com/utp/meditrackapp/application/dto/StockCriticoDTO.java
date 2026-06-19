package com.utp.meditrackapp.application.dto;

import java.time.LocalDate;

/**
 * DTO para información de stock crítico.
 * Refactorizado desde core.models.dto.StockCriticoItem
 */
public class StockCriticoDTO {
    private final String productoId;
    private final String codigoDigemid;
    private final String productoNombre;
    private final String categoriaNombre;
    private final int stockActual;
    private final int stockMinimo;
    private final LocalDate fechaVencimiento;
    private final int diasParaVencer;

    public StockCriticoDTO(String productoId, String codigoDigemid, String productoNombre,
                           String categoriaNombre, int stockActual, int stockMinimo,
                           LocalDate fechaVencimiento, int diasParaVencer) {
        this.productoId = productoId;
        this.codigoDigemid = codigoDigemid;
        this.productoNombre = productoNombre;
        this.categoriaNombre = categoriaNombre;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.fechaVencimiento = fechaVencimiento;
        this.diasParaVencer = diasParaVencer;
    }

    // === Comportamiento de Negocio ===

    public boolean isStockBajo() {
        return stockActual < stockMinimo;
    }

    public boolean isVencePronto() {
        return diasParaVencer >= 0 && diasParaVencer <= 60;
    }

    public boolean isCritico() {
        return isStockBajo() || isVencePronto();
    }

    public String getNivelRiesgo() {
        if (isStockBajo() && isVencePronto()) return "CRITICO";
        if (isStockBajo()) return "STOCK_BAJO";
        if (diasParaVencer <= 30) return "VENCIMIENTO_INMINENTE";
        if (isVencePronto()) return "POR_VENCER";
        return "NORMAL";
    }

    // === Getters ===

    public String getProductoId() { return productoId; }
    public String getCodigoDigemid() { return codigoDigemid; }
    public String getProductoNombre() { return productoNombre; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public int getDiasParaVencer() { return diasParaVencer; }
}
