package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.util.DateTimeProvider;
import com.utp.meditrackapp.core.validation.SedeAccessValidator;
import com.utp.meditrackapp.domain.ports.out.DashboardRepository;

import java.sql.*;
import java.util.*;

/**
 * Implementación JDBC del repositorio de Dashboard.
 * Migrado desde DashboardDao.
 */
public class JdbcDashboardRepository implements DashboardRepository {
    private final DatabaseConfig dbConfig;

    public JdbcDashboardRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public int getStockCriticoCount(int umbral) {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) as total FROM (" +
            "  SELECT l.producto_id, SUM(l.cantidad) as stock_total " +
            "  FROM lotes l "
        );
        
        if (sedeId != null) {
            sql.append("WHERE l.sede_id = ? ");
        }
        
        sql.append("  GROUP BY l.producto_id " +
                   ") as Resumen WHERE stock_total < ?");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            if (sedeId != null) {
                ps.setString(i++, sedeId);
            }
            ps.setInt(i++, umbral);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int getLotesPorVencerCount(int dias) {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) as total FROM lotes WHERE fecha_vencimiento <= DATEADD(day, ?, ?) AND fecha_vencimiento >= ?"
        );
        
        if (sedeId != null) {
            sql.append(" AND sede_id = ?");
        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            java.sql.Timestamp now = Timestamp.valueOf(DateTimeProvider.now());
            int i = 1;
            ps.setInt(i++, dias);
            ps.setTimestamp(i++, now);
            ps.setTimestamp(i++, now);
            if (sedeId != null) {
                ps.setString(i++, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
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

    @Override
    public double getValorInventario() {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        StringBuilder sql = new StringBuilder(
            "SELECT SUM(ISNULL(p.precio_unitario,0) * l.cantidad) as valor " +
            "FROM productos p JOIN lotes l ON p.id = l.producto_id"
        );
        
        if (sedeId != null) {
            sql.append(" WHERE l.sede_id = ?");
        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("valor");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int getVolumenMovimientos(int dias) {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) as total FROM movimientos WHERE fecha_registro >= DATEADD(day, -?, ?)"
        );
        
        if (sedeId != null) {
            sql.append(" AND sede_id = ?");
        }

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setInt(i++, dias);
            ps.setTimestamp(i++, Timestamp.valueOf(DateTimeProvider.now()));
            if (sedeId != null) {
                ps.setString(i++, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> getTendenciaInventario(int meses) {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        List<Map<String, Object>> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT FORMAT(m.fecha_registro,'yyyy-MM') as periodo, " +
            "  SUM(CASE WHEN m.tipo_id = 'MOV-T-01' THEN m.cantidad ELSE 0 END) - " +
            "  SUM(CASE WHEN m.tipo_id = 'MOV-T-02' THEN m.cantidad ELSE 0 END) as total " +
            "FROM movimientos m " +
            "WHERE m.fecha_registro >= DATEADD(month, -?, ?) "
        );
        
        if (sedeId != null) {
            sql.append("AND m.sede_id = ? ");
        }
        
        sql.append("GROUP BY FORMAT(m.fecha_registro,'yyyy-MM') ORDER BY periodo");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setInt(i++, meses);
            ps.setTimestamp(i++, Timestamp.valueOf(DateTimeProvider.now()));
            if (sedeId != null) {
                ps.setString(i++, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
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

    @Override
    public List<Map<String, Object>> getDistribucionPorCategoria() {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        List<Map<String, Object>> out = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.nombre as category, SUM(l.cantidad) as total " +
            "FROM productos p JOIN categorias c ON p.categoria_id = c.id " +
            "JOIN lotes l ON p.id = l.producto_id"
        );
        
        if (sedeId != null) {
            sql.append(" WHERE l.sede_id = ?");
        }
        
        sql.append(" GROUP BY c.nombre");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("category", rs.getString("category"));
                    m.put("total", rs.getInt("total"));
                    out.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    @Override
    public List<Map<String, Object>> getProductosBajoStock(int topN) {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        List<Map<String, Object>> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP " + topN + " p.codigo_digemid as codigo, p.nombre, c.nombre as categoria, " +
            "SUM(l.cantidad) as stock_total, " +
            "MAX(ISNULL(p.precio_unitario, 0)) as precio " +
            "FROM productos p " +
            "JOIN categorias c ON p.categoria_id = c.id " +
            "JOIN lotes l ON p.id = l.producto_id"
        );
        
        if (sedeId != null) {
            sql.append(" WHERE l.sede_id = ?");
        }
        
        sql.append(" GROUP BY p.codigo_digemid, p.nombre, c.nombre " +
                   "ORDER BY stock_total ASC");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("codigo", rs.getString("codigo"));
                    m.put("nombre", rs.getString("nombre"));
                    m.put("categoria", rs.getString("categoria"));
                    m.put("stock_total", rs.getInt("stock_total"));
                    m.put("precio", rs.getDouble("precio"));
                    lista.add(m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
