package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.TipoMovimiento;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TipoMovimientoDAO extends JdbcDaoSupport {

    public List<TipoMovimiento> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM tipos_movimiento ORDER BY id ASC";
        List<TipoMovimiento> lista = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new TipoMovimiento(
                    rs.getString("id"),
                    rs.getString("nombre")
                ));
            }
        }
        return lista;
    }

    public Optional<TipoMovimiento> buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, nombre FROM tipos_movimiento WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new TipoMovimiento(rs.getString("id"), rs.getString("nombre")));
                }
            }
        }
        return Optional.empty();
    }

    public TipoMovimiento crear(TipoMovimiento tipo) throws SQLException {
        try (Connection conn = getConnection()) {
            if (tipo.getId() == null || tipo.getId().isBlank()) {
                tipo.setId(IdGenerator.generateId(conn, "tipos_movimiento", EntidadPrefix.TIPO_MOVIMIENTO, 2));
            }
            String sql = "INSERT INTO tipos_movimiento (id, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, tipo.getId());
                ps.setString(2, tipo.getNombre());
                ps.executeUpdate();
            }
        }
        return tipo;
    }

    public void actualizar(TipoMovimiento tipo) throws SQLException {
        String sql = "UPDATE tipos_movimiento SET nombre = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.getNombre());
            ps.setString(2, tipo.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM movimientos WHERE tipo_id = ?";
        String deleteSql = "DELETE FROM tipos_movimiento WHERE id = ?";
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) > 0) {
                            conn.rollback();
                            throw new SQLException("No se puede eliminar el tipo de movimiento porque tiene movimientos asociados.");
                        }
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, id);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
