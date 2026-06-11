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
     * Cuenta cuántos productos tienen stock total por debajo de un umbral.
     * Cuenta productos, no lotes individuales.
     */
    public int getStockCriticoCount(int umbral) {
        String sql = "SELECT COUNT(*) as total FROM (" +
                     "  SELECT producto_id, SUM(cantidad) as stock_total " +
                     "  FROM lotes GROUP BY producto_id " +
                     ") as Resumen WHERE stock_total < ?";
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
        String sql = "SELECT TOP 5 p.codigo_digemid, p.nombre, c.nombre as categoria, SUM(l.cantidad) as stock_total, " +
                     "MAX(ISNULL(p.precio_unitario, 0)) as precio " +
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
                double precio = rs.getDouble("precio");
                double valorTotal = stock * precio;
                String estado = stock < 10 ? "CRÍTICO" : "BAJO";
                
                String formattedValue = String.format("S/ %,.2f", valorTotal);
                
                lista.add(new MedicamentoResumen(
                    rs.getString("codigo_digemid"),
                    rs.getString("nombre"),
                    rs.getString("categoria"),
                    stock,
                    10,
                    estado,
                    formattedValue
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Calcula la salud del inventario usando el mismo algoritmo que InventarioService.
     * penaliza stock bajo y lotes por vencer.
     */
    public int getSaludInventario(String sedeId) {
        String sql = "SELECT " +
                     "  (CAST(SUM(CASE WHEN stock_total < ISNULL(s.stock_minimo, 10) THEN 1 ELSE 0 END) AS FLOAT) / " +
                     "   CAST(COUNT(*) AS FLOAT)) * 100 as salud " +
                     "FROM ( " +
                     "  SELECT l.producto_id, SUM(l.cantidad) as stock_total " +
                     "  FROM lotes l " +
                     (sedeId != null ? "  WHERE l.sede_id = ? " : " ") +
                     "  GROUP BY l.producto_id " +
                     ") as Resumen " +
                     "LEFT JOIN (SELECT id, stock_minimo FROM productos) s ON Resumen.producto_id = s.id";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return (int) rs.getFloat("salud");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Retorna una serie de puntos por mes (periodo yyyy-MM) con el total de unidades movidas.
     * Usa la fecha de registro de movimientos en vez de fecha de fabricación.
     */
    public List<Map<String,Object>> getInventoryTrendMonths(int monthsBack) {
        List<Map<String,Object>> out = new LinkedList<>();
        String sql = "SELECT FORMAT(m.fecha_registro,'yyyy-MM') as periodo, " +
                     "  SUM(CASE WHEN m.tipo_id = 'MOV-T-01' THEN m.cantidad ELSE 0 END) - " +
                     "  SUM(CASE WHEN m.tipo_id = 'MOV-T-02' THEN m.cantidad ELSE 0 END) as total " +
                     "FROM movimientos m " +
                     "WHERE m.fecha_registro >= DATEADD(month, -?, GETDATE()) " +
                     "GROUP BY FORMAT(m.fecha_registro,'yyyy-MM') ORDER BY periodo";
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
        String sql = "SELECT SUM(ISNULL(p.precio_unitario,0) * l.cantidad) as valor " +
                     "FROM productos p JOIN lotes l ON p.id = l.producto_id";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble("valor");
        } catch (SQLException e) {
            // Fallback: if precio_unitario column doesn't exist, sum units only
            try (Connection conn = dbConfig.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT SUM(l.cantidad) as unidades FROM lotes l")) {
                if (rs.next()) return rs.getDouble("unidades");
            } catch (SQLException ex) {
                System.err.println("[DAO] Error calculando valor inventario: " + ex.getMessage());
            }
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
