package com.utp.meditrackapp.features.dashboard.Dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
}
