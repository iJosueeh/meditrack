package com.utp.meditrackapp.domain.ports.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Puerto de salida para datos del Dashboard y reportes.
 * Centraliza las consultas que estaban en DashboardDao.
 */
public interface DashboardRepository {

    /**
     * Cuenta productos con stock total por debajo de un umbral.
     */
    int getStockCriticoCount(int umbral);

    /**
     * Cuenta lotes que vencen en los próximos N días.
     */
    int getLotesPorVencerCount(int dias);

    /**
     * Calcula la salud del inventario (0-100%).
     */
    int getSaludInventario(String sedeId);

    /**
     * Obtiene el valor total del inventario.
     */
    double getValorInventario();

    /**
     * Obtiene el volumen de movimientos en los últimos N días.
     */
    int getVolumenMovimientos(int dias);

    /**
     * Obtiene tendencia de inventario por mes (últimos N meses).
     * Retorna lista de mapas con "period" (yyyy-MM) y "value" (unidades).
     */
    List<Map<String, Object>> getTendenciaInventario(int meses);

    /**
     * Obtiene distribución de stock por categoría.
     * Retorna lista de mapas con "category" y "total".
     */
    List<Map<String, Object>> getDistribucionPorCategoria();

    /**
     * Obtiene el TOP N de productos con menor stock.
     * Retorna lista de mapas con: codigo, nombre, categoria, stock_total, precio, valor_total.
     */
    List<Map<String, Object>> getProductosBajoStock(int topN);
}
