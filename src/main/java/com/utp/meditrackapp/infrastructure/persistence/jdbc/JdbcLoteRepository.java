package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Lotes.
 * Migrado desde core.dao.LoteDAO
 */
public class JdbcLoteRepository implements LoteRepository {
    private final DatabaseConfig dbConfig;

    public JdbcLoteRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    private static final String SELECT_BASE =
        "SELECT l.id, l.producto_id, l.sede_id, l.numero_lote, l.fecha_vencimiento, l.fecha_fabricacion, l.cantidad, " +
        "p.nombre AS producto_nombre, p.codigo_digemid " +
        "FROM lotes l LEFT JOIN productos p ON l.producto_id = p.id ";

    @Override
    public Optional<Lote> findById(String id) {
        String sql = SELECT_BASE + "WHERE l.id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapLote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Lote> findBySede(String sedeId) {
        return findBySedeAndProducto(sedeId, null);
    }

    @Override
    public List<Lote> findBySedeAndProducto(String sedeId, String productoId) {
        StringBuilder sql = new StringBuilder(SELECT_BASE + "WHERE l.sede_id = ?");
        if (productoId != null) {
            sql.append(" AND producto_id = ?");
        }
        sql.append(" ORDER BY fecha_vencimiento ASC, numero_lote ASC");

        List<Lote> lotes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, sedeId);
            if (productoId != null) ps.setString(i++, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lotes.add(mapLote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lotes;
    }

    @Override
    public List<Lote> findFefo(String sedeId, String productoId) {
        String sql = SELECT_BASE +
                     "WHERE l.sede_id = ? AND l.producto_id = ? AND l.cantidad > 0 " +
                     "ORDER BY l.fecha_vencimiento ASC, l.fecha_fabricacion ASC, l.numero_lote ASC";
        List<Lote> lotes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setString(2, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lotes.add(mapLote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lotes;
    }

    @Override
    public Map<String, Integer> findStockTotalBySede(String sedeId) {
        String sql = "SELECT producto_id, COALESCE(SUM(cantidad), 0) AS stock_total FROM lotes WHERE sede_id = ? GROUP BY producto_id";
        Map<String, Integer> stockMap = new HashMap<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockMap.put(rs.getString("producto_id"), rs.getInt("stock_total"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stockMap;
    }

    @Override
    public int findStockTotal(String sedeId, String productoId) {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) AS stock_total FROM lotes WHERE sede_id = ? AND producto_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setString(2, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock_total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int findStockByLote(Connection conn, String loteId) {
        String sql = "SELECT cantidad FROM lotes WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, loteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cantidad");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar stock del lote: " + e.getMessage(), e);
        }
        return 0;
    }

    @Override
    public Lote save(Connection conn, Lote lote) {
        try {
            if (lote.getId() == null || lote.getId().isBlank()) {
                lote.setId(IdGenerator.generateSedeDependentId(conn, "lotes", EntidadPrefix.LOTE, lote.getSedeId(), 5));
            }

            String sql = "INSERT INTO lotes (id, producto_id, sede_id, numero_lote, fecha_vencimiento, fecha_fabricacion, cantidad) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, lote.getId());
                ps.setString(2, lote.getProductoId());
                ps.setString(3, lote.getSedeId());
                ps.setString(4, lote.getNumeroLote());
                ps.setDate(5, java.sql.Date.valueOf(lote.getFechaVencimiento()));
                ps.setDate(6, lote.getFechaFabricacion() == null ? null : java.sql.Date.valueOf(lote.getFechaFabricacion()));
                ps.setInt(7, lote.getCantidad());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar lote: " + e.getMessage(), e);
        }
        return lote;
    }

    @Override
    public void aumentarStock(Connection conn, String loteId, int cantidad) {
        String sql = "UPDATE lotes SET cantidad = cantidad + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, loteId);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Lote no encontrado: " + loteId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al aumentar stock: " + e.getMessage(), e);
        }
    }

    @Override
    public void reducirStock(Connection conn, String loteId, int cantidad) {
        String sql = "UPDATE lotes SET cantidad = cantidad - ? WHERE id = ? AND cantidad >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cantidad);
            ps.setString(2, loteId);
            ps.setInt(3, cantidad);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Stock insuficiente o lote no encontrado: " + loteId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al reducir stock: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByNumeroLoteProductoSede(String numeroLote, String productoId, String sedeId) {
        String sql = "SELECT COUNT(*) FROM lotes WHERE numero_lote = ? AND producto_id = ? AND sede_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroLote);
            ps.setString(2, productoId);
            ps.setString(3, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Lote save(Lote lote) {
        try (Connection conn = dbConfig.getConnection()) {
            return save(conn, lote);
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar lote: " + e.getMessage(), e);
        }
    }

    private Lote mapLote(ResultSet rs) throws SQLException {
        Lote lote = new Lote();
        lote.setId(rs.getString("id"));
        lote.setProductoId(rs.getString("producto_id"));
        lote.setSedeId(rs.getString("sede_id"));
        lote.setNumeroLote(rs.getString("numero_lote"));
        java.sql.Date fechaVencimiento = rs.getDate("fecha_vencimiento");
        if (fechaVencimiento != null) {
            lote.setFechaVencimiento(fechaVencimiento.toLocalDate());
        }
        java.sql.Date fechaFabricacion = rs.getDate("fecha_fabricacion");
        if (fechaFabricacion != null) {
            lote.setFechaFabricacion(fechaFabricacion.toLocalDate());
        }
        lote.setCantidad(rs.getInt("cantidad"));
        try {
            String nombre = rs.getString("producto_nombre");
            lote.setProductoNombre(nombre != null ? nombre : "(Producto desconocido)");
        } catch (SQLException ignored) {}
        try {
            String codigo = rs.getString("codigo_digemid");
            lote.setCodigoDigemid(codigo != null ? codigo : "N/A");
        } catch (SQLException ignored) {}
        return lote;
    }
}
