package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.core.validation.SedeAccessValidator;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del repositorio de Movimientos.
 * Migrado desde core.dao.MovimientoDAO
 */
public class JdbcMovimientoRepository implements MovimientoRepository {
    private final DatabaseConfig dbConfig;

    public JdbcMovimientoRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Movimiento save(Connection conn, Movimiento movimiento) {
        try {
            if (movimiento.getId() == null || movimiento.getId().isBlank()) {
                movimiento.setId(IdGenerator.generateSedeDependentId(conn, "movimientos", EntidadPrefix.MOVIMIENTO, movimiento.getSedeId(), 6));
            }

            String sql = "INSERT INTO movimientos (id, tipo_id, motivo_id, sede_id, usuario_id, lote_id, cantidad, observacion, fecha_registro) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, movimiento.getId());
                ps.setString(2, movimiento.getTipoId());
                ps.setString(3, movimiento.getMotivoId());
                ps.setString(4, movimiento.getSedeId());
                ps.setString(5, movimiento.getUsuarioId());
                ps.setString(6, movimiento.getLoteId());
                ps.setInt(7, movimiento.getCantidad());
                ps.setString(8, movimiento.getObservacion());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar movimiento: " + e.getMessage(), e);
        }
        return movimiento;
    }

    @Override
    public List<Movimiento> findBySede(String sedeId, String tipoId, String buscar) {
        return findByFilters(sedeId, tipoId, buscar, null, null);
    }

    @Override
    public List<Movimiento> findByFilters(String sedeId, String tipoId, String buscar, LocalDate desde, LocalDate hasta) {
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, tm.nombre as tipo_nombre, mm.nombre as motivo_nombre, p.nombre as producto_nombre, l.numero_lote " +
            "FROM movimientos m " +
            "JOIN tipos_movimiento tm ON m.tipo_id = tm.id " +
            "JOIN motivos_movimiento mm ON m.motivo_id = mm.id " +
            "JOIN lotes l ON m.lote_id = l.id " +
            "JOIN productos p ON l.producto_id = p.id " +
            "WHERE m.sede_id = ?"
        );

        if (tipoId != null && !tipoId.isEmpty()) {
            sql.append(" AND m.tipo_id = ?");
        }
        if (buscar != null && !buscar.isEmpty()) {
            sql.append(" AND (p.nombre LIKE ? OR l.numero_lote LIKE ?)");
        }
        if (desde != null) {
            sql.append(" AND CAST(m.fecha_registro AS DATE) >= ?");
        }
        if (hasta != null) {
            sql.append(" AND CAST(m.fecha_registro AS DATE) <= ?");
        }
        sql.append(" ORDER BY m.fecha_registro DESC");

        List<Movimiento> lista = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, sedeId);
            if (tipoId != null && !tipoId.isEmpty()) ps.setString(i++, tipoId);
            if (buscar != null && !buscar.isEmpty()) {
                String pattern = "%" + buscar + "%";
                ps.setString(i++, pattern);
                ps.setString(i++, pattern);
            }
            if (desde != null) ps.setDate(i++, Date.valueOf(desde));
            if (hasta != null) ps.setDate(i++, Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void deleteById(Connection conn, String id) {
        String sql = "DELETE FROM movimientos WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No se encontró el movimiento con ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar movimiento: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateObservacion(Connection conn, String id, String observacion) {
        String sql = "UPDATE movimientos SET observacion = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, observacion);
            ps.setString(2, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No se encontró el movimiento con ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar observación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Movimiento> findAll() {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, tm.nombre as tipo_nombre, mm.nombre as motivo_nombre, p.nombre as producto_nombre, l.numero_lote " +
            "FROM movimientos m " +
            "JOIN tipos_movimiento tm ON m.tipo_id = tm.id " +
            "JOIN motivos_movimiento mm ON m.motivo_id = mm.id " +
            "JOIN lotes l ON m.lote_id = l.id " +
            "JOIN productos p ON l.producto_id = p.id "
        );
        
        if (sedeId != null) {
            sql.append("WHERE m.sede_id = ? ");
        }
        sql.append("ORDER BY m.fecha_registro DESC");

        List<Movimiento> lista = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Movimiento mapMovimiento(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setId(rs.getString("id"));
        m.setTipoId(rs.getString("tipo_id"));
        m.setMotivoId(rs.getString("motivo_id"));
        m.setSedeId(rs.getString("sede_id"));
        m.setUsuarioId(rs.getString("usuario_id"));
        m.setLoteId(rs.getString("lote_id"));
        m.setCantidad(rs.getInt("cantidad"));
        m.setObservacion(rs.getString("observacion"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) m.setFechaRegistro(ts.toLocalDateTime());
        try { m.setTipoNombre(rs.getString("tipo_nombre")); } catch (SQLException ignored) {}
        try { m.setMotivoNombre(rs.getString("motivo_nombre")); } catch (SQLException ignored) {}
        try { m.setProductoNombre(rs.getString("producto_nombre")); } catch (SQLException ignored) {}
        try { m.setNumeroLote(rs.getString("numero_lote")); } catch (SQLException ignored) {}
        return m;
    }
}
