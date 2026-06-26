package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.ports.out.MotivoMovimientoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMotivoMovimientoRepository implements MotivoMovimientoRepository {

    @Override
    public Optional<MotivoMovimiento> findById(String id) {
        String sql = "SELECT id, nombre, is_activo FROM motivos_movimiento WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMotivoMovimiento(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<MotivoMovimiento> findByNombre(String nombre) {
        String sql = "SELECT id, nombre, is_activo FROM motivos_movimiento WHERE LOWER(nombre) = LOWER(?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMotivoMovimiento(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MotivoMovimiento> findAll() {
        String sql = "SELECT id, nombre, is_activo FROM motivos_movimiento ORDER BY nombre";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MotivoMovimiento> motivos = new ArrayList<>();
            while (rs.next()) {
                motivos.add(mapResultSetToMotivoMovimiento(rs));
            }
            return motivos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public MotivoMovimiento save(MotivoMovimiento motivo) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String id = motivo.getId();
            if (id == null || id.isBlank()) {
                id = IdGenerator.generateId(conn, "motivos_movimiento", EntidadPrefix.MOTIVO_MOVIMIENTO, 6);
                motivo.setId(id);
            }

            String sql = "INSERT INTO motivos_movimiento (id, nombre, is_activo) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, motivo.getNombre());
                ps.setInt(3, motivo.getIsActivo());
                ps.executeUpdate();
            }
            return motivo;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(MotivoMovimiento motivo) {
        String sql = "UPDATE motivos_movimiento SET nombre = ?, is_activo = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, motivo.getNombre());
            ps.setInt(2, motivo.getIsActivo());
            ps.setString(3, motivo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM motivos_movimiento WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private MotivoMovimiento mapResultSetToMotivoMovimiento(ResultSet rs) throws SQLException {
        MotivoMovimiento m = new MotivoMovimiento();
        m.setId(rs.getString("id"));
        m.setNombre(rs.getString("nombre"));
        m.setIsActivo(rs.getInt("is_activo"));
        return m;
    }
}
