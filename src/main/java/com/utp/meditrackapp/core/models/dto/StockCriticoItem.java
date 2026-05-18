package com.utp.meditrackapp.core.models.dto;

import java.time.LocalDate;

public class StockCriticoItem {
    private final String productoId;
    private final String codigoDigemid;
    private final String nombreProducto;
    private final String categoria;
    private final int stockActual;
    private final int stockMinimo;
    private final LocalDate fechaVencimientoMasProxima;
    private final int diasParaVencer;

    public StockCriticoItem(String productoId, String codigoDigemid, String nombreProducto, String categoria, int stockActual, int stockMinimo, LocalDate fechaVencimientoMasProxima, int diasParaVencer) {
        this.productoId = productoId;
        this.codigoDigemid = codigoDigemid;
        this.nombreProducto = nombreProducto;
        this.categoria = categoria;
        this.stockActual = stockActual;
        this.stockMinimo = stockMinimo;
        this.fechaVencimientoMasProxima = fechaVencimientoMasProxima;
        this.diasParaVencer = diasParaVencer;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getCodigoDigemid() {
        return codigoDigemid;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getStockActual() {
        return stockActual;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public LocalDate getFechaVencimientoMasProxima() {
        return fechaVencimientoMasProxima;
    }

    public int getDiasParaVencer() {
        return diasParaVencer;
    }

    public boolean isStockBajo() {
        return stockActual < stockMinimo;
    }

    public boolean isVencePronto() {
        return diasParaVencer >= 0 && diasParaVencer <= 60;
    }

    public String getEstado() {
        if (isStockBajo() && isVencePronto()) {
            return "CRITICO";
        }
        if (isStockBajo()) {
            return "STOCK BAJO";
        }
        if (isVencePronto()) {
            return diasParaVencer <= 30 ? "POR VENCER" : "VENCIMIENTO PRONTO";
        }
        return "OK";
    }
}