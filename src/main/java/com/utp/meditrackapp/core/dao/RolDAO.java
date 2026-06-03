package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.Rol;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RolDAO extends JdbcDaoSupport {

    public List<Rol> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM roles ORDER BY id ASC";
        List<Rol> lista = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Rol(rs.getString("id"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public Optional<Rol> buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, nombre FROM roles WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Rol(rs.getString("id"), rs.getString("nombre")));
                }
            }
        }
        return Optional.empty();
    }

    public Rol crear(Rol rol) throws SQLException {
        try (Connection conn = getConnection()) {
            if (rol.getId() == null || rol.getId().isBlank()) {
                rol.setId(IdGenerator.generateId(conn, "roles", EntidadPrefix.ROL, 3));
            }
            String sql = "INSERT INTO roles (id, nombre) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rol.getId());
                ps.setString(2, rol.getNombre());
                ps.executeUpdate();
            }
        }
        return rol;
    }

    public void actualizar(Rol rol) throws SQLException {
        String sql = "UPDATE roles SET nombre = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(String id) throws SQLException {
        // Verificar si hay usuarios con este rol
        String checkSql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("No se puede eliminar el rol porque tiene usuarios asignados.");
                }
            }
        }

        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }
}
