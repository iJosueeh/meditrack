package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.ports.out.TipoMovimientoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTipoMovimientoRepository implements TipoMovimientoRepository {

    @Override
    public Optional<TipoMovimiento> findById(String id) {
        String sql = "SELECT id, nombre, is_activo FROM tipos_movimiento WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTipoMovimiento(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<TipoMovimiento> findByNombre(String nombre) {
        String sql = "SELECT id, nombre, is_activo FROM tipos_movimiento WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToTipoMovimiento(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TipoMovimiento> findAll() {
        String sql = "SELECT id, nombre, is_activo FROM tipos_movimiento ORDER BY nombre";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<TipoMovimiento> tipos = new ArrayList<>();
            while (rs.next()) {
                tipos.add(mapResultSetToTipoMovimiento(rs));
            }
            return tipos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TipoMovimiento save(TipoMovimiento tipo) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String id = tipo.getId();
            if (id == null || id.isBlank()) {
                id = IdGenerator.generateId(conn, "tipos_movimiento", EntidadPrefix.TIPO_MOVIMIENTO, 6);
                tipo.setId(id);
            }

            String sql = "INSERT INTO tipos_movimiento (id, nombre, is_activo) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, tipo.getNombre());
                ps.setInt(3, tipo.getIsActivo());
                ps.executeUpdate();
            }
            return tipo;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(TipoMovimiento tipo) {
        String sql = "UPDATE tipos_movimiento SET nombre = ?, is_activo = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tipo.getNombre());
            ps.setInt(2, tipo.getIsActivo());
            ps.setString(3, tipo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM tipos_movimiento WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private TipoMovimiento mapResultSetToTipoMovimiento(ResultSet rs) throws SQLException {
        TipoMovimiento t = new TipoMovimiento();
        t.setId(rs.getString("id"));
        t.setNombre(rs.getString("nombre"));
        t.setIsActivo(rs.getInt("is_activo"));
        return t;
    }
}
