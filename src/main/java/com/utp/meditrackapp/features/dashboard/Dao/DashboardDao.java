package com.utp.meditrackapp.features.dashboard.Dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;

public class DashboardDao {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    /**
     * Cuenta cuántos productos tienen stock por debajo de un umbral (ej. 10 unidades).
     */
    public int getStockCriticoCount(int umbral) {
        String sql = "SELECT COUNT(*) as total FROM lotes WHERE cantidad < ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cuenta cuántos lotes están por vencer en los próximos 30 días.
     */
    public int getLotesPorVencerCount(int dias) {
        String sql = "SELECT COUNT(*) as total FROM lotes WHERE fecha_vencimiento <= DATEADD(day, ?, GETDATE()) AND fecha_vencimiento >= GETDATE()";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Obtiene el TOP 5 de productos con menos stock.
     */
    public List<MedicamentoResumen> getTopBajoStock() {
        List<MedicamentoResumen> lista = new ArrayList<>();
        String sql = "SELECT TOP 5 p.codigo_digemid, p.nombre, c.nombre as categoria, SUM(l.cantidad) as stock_total " +
                     "FROM productos p " +
                     "JOIN categorias c ON p.categoria_id = c.id " +
                     "JOIN lotes l ON p.id = l.producto_id " +
                     "GROUP BY p.codigo_digemid, p.nombre, c.nombre " +
                     "ORDER BY stock_total ASC";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int stock = rs.getInt("stock_total");
                String estado = stock < 10 ? "CRÍTICO" : "BAJO";
                lista.add(new MedicamentoResumen(
                    rs.getString("codigo_digemid"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    stock,
                    10,
                    estado
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Calcula la salud del inventario como un porcentaje.
     * (Productos con stock > 10 / Total de productos) * 100
     */
    public int getSaludInventario() {
        String sql = "SELECT " +
                     " (CAST(SUM(CASE WHEN stock_total > 10 THEN 1 ELSE 0 END) AS FLOAT) / " +
                     "  CAST(COUNT(*) AS FLOAT)) * 100 as salud " +
                     "FROM ( " +
                     "  SELECT producto_id, SUM(cantidad) as stock_total " +
                     "  FROM lotes " +
                     "  GROUP BY producto_id " +
                     ") as Resumen";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return (int) rs.getFloat("salud");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 100;
    }

    /**
     * Retorna una serie de puntos por mes (periodo yyyy-MM) con total de unidades en lotes.
     */
    public List<Map<String,Object>> getInventoryTrendMonths(int monthsBack) {
        List<Map<String,Object>> out = new LinkedList<>();
        String sql = "SELECT FORMAT(fecha_fabricacion,'yyyy-MM') as periodo, SUM(cantidad) as total " +
                     "FROM lotes WHERE fecha_fabricacion >= DATEADD(month, -?, GETDATE()) " +
                     "GROUP BY FORMAT(fecha_fabricacion,'yyyy-MM') ORDER BY periodo";
        try (Connection conn = dbConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, monthsBack);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> m = new HashMap<>();
                    m.put("period", rs.getString("periodo"));
                    m.put("value", rs.getInt("total"));
                    out.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Retorna distribución por categoría: nombre y total unidades.
     */
    public List<Map<String,Object>> getCategoryDistribution() {
        List<Map<String,Object>> out = new LinkedList<>();
        String sql = "SELECT c.nombre as category, SUM(l.cantidad) as total " +
                     "FROM productos p JOIN categorias c ON p.categoria_id = c.id " +
                     "JOIN lotes l ON p.id = l.producto_id " +
                     "GROUP BY c.nombre";
        try (Connection conn = dbConfig.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String,Object> m = new HashMap<>();
                m.put("category", rs.getString("category"));
                m.put("total", rs.getInt("total"));
                out.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * Calcula el valor total del inventario (suma de precio_unitario * cantidad).
     */
    public double getInventoryValue() {
        String sqlCheck = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='productos' AND COLUMN_NAME='precio_unitario'";
        String sqlAdd = "ALTER TABLE productos ADD precio_unitario decimal(18,2) DEFAULT 0";
        String sqlSum = "SELECT SUM(ISNULL(p.precio_unitario,0) * l.cantidad) as valor FROM productos p JOIN lotes l ON p.id = l.producto_id";
        try (Connection conn = dbConfig.getConnection(); Statement stmt = conn.createStatement()) {
            try (ResultSet rsCheck = stmt.executeQuery(sqlCheck)) {
                if (!rsCheck.next()) {
                    try {
                        stmt.execute(sqlAdd);
                    } catch (SQLException e) {
                        System.err.println("[DAO] No se pudo crear columna precio_unitario: " + e.getMessage());
                    }
                }
            }
            try (ResultSet rs = stmt.executeQuery(sqlSum)) {
                if (rs.next()) return rs.getDouble("valor");
            } catch (SQLException e) {
                System.err.println("[DAO] No se pudo calcular valor por precio_unitario, usando fallback (unidades): " + e.getMessage());
                // Fallback: sumar solo las unidades
                try (ResultSet rs2 = stmt.executeQuery("SELECT SUM(l.cantidad) as unidades FROM lotes l")) {
                    if (rs2.next()) return rs2.getDouble("unidades");
                } catch (SQLException ex) {
                    System.err.println("[DAO] Fallback también falló: " + ex.getMessage());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Obtiene el volumen de movimientos (entradas + salidas) en los últimos N días.
     * Asume que existe una tabla `movimientos` con columna `fecha`.
     */
    public int getMovementsVolume(int dias) {
        String sql = "SELECT COUNT(*) as total FROM movimientos WHERE fecha_registro >= DATEADD(day, -?, GETDATE())";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, dias);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
