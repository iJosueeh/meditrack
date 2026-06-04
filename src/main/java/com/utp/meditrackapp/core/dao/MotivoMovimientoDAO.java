package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.MotivoMovimiento;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MotivoMovimientoDAO extends JdbcDaoSupport {

    public List<MotivoMovimiento> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM motivos_movimiento ORDER BY id ASC";
        List<MotivoMovimiento> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new MotivoMovimiento(rs.getString("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public Optional<MotivoMovimiento> buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, nombre FROM motivos_movimiento WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new MotivoMovimiento(rs.getString("id"), rs.getString("nombre")));
                }
            }
        }
        return Optional.empty();
    }

    public MotivoMovimiento crear(MotivoMovimiento motivo) throws SQLException {
        try (Connection conn = getConnection()) {
            if (motivo.getId() == null || motivo.getId().isBlank()) {
                motivo.setId(IdGenerator.generateId(conn, "motivos_movimiento", EntidadPrefix.MOTIVO_MOVIMIENTO, 2));
            }
            String sql = "INSERT INTO motivos_movimiento (id, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, motivo.getId());
                ps.setString(2, motivo.getNombre());
                ps.executeUpdate();
            }
        }
        return motivo;
    }

    public void actualizar(MotivoMovimiento motivo) throws SQLException {
        String sql = "UPDATE motivos_movimiento SET nombre = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, motivo.getNombre());
            ps.setString(2, motivo.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException {
        String sql = "DELETE FROM motivos_movimiento WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
