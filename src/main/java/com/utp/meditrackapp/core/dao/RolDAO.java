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
        List<Rol> lista = new ArrayList<>();
        Connection conn = getConnection();
        try {
            String sql = "SELECT id, nombre, is_activo FROM roles ORDER BY id ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs, true));
                }
                return lista;
            }
        } catch (SQLException e) {
            // Column is_activo doesn't exist, fallback
        } finally {
            closeConn(conn);
        }

        conn = getConnection();
        try {
            String sql = "SELECT id, nombre FROM roles ORDER BY id ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs, false));
                }
            }
        } finally {
            closeConn(conn);
        }
        return lista;
    }

    public Optional<Rol> buscarPorId(String id) throws SQLException {
        Connection conn = getConnection();
        try {
            String sql = "SELECT id, nombre, is_activo FROM roles WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs, true));
                }
            }
        } catch (SQLException e) {
            // Column is_activo doesn't exist, fallback
        } finally {
            closeConn(conn);
        }

        conn = getConnection();
        try {
            String sql = "SELECT id, nombre FROM roles WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapRow(rs, false));
                }
            }
        } finally {
            closeConn(conn);
        }
        return Optional.empty();
    }

    public Rol crear(Rol rol) throws SQLException {
        Connection conn = getConnection();
        try {
            if (rol.getId() == null || rol.getId().isBlank()) {
                rol.setId(IdGenerator.generateId(conn, "roles", EntidadPrefix.ROL, 3));
            }
            String sql = "INSERT INTO roles (id, nombre, is_activo) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rol.getId());
                ps.setString(2, rol.getNombre());
                ps.setInt(3, rol.getIsActivo() == 0 ? 0 : 1);
                ps.executeUpdate();
            }
            return rol;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("is_activo")) {
                // Column doesn't exist, insert without it
                String sql = "INSERT INTO roles (id, nombre) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, rol.getId());
                    ps.setString(2, rol.getNombre());
                    ps.executeUpdate();
                }
                return rol;
            }
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    public void actualizar(Rol rol) throws SQLException {
        Connection conn = getConnection();
        try {
            String sql = "UPDATE roles SET nombre = ?, is_activo = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, rol.getNombre());
                ps.setInt(2, rol.getIsActivo());
                ps.setString(3, rol.getId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("is_activo")) {
                String sql = "UPDATE roles SET nombre = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, rol.getNombre());
                    ps.setString(2, rol.getId());
                    ps.executeUpdate();
                }
                return;
            }
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    public void toggleEstado(String id) throws SQLException {
        Connection conn = getConnection();
        try {
            String sql = "UPDATE roles SET is_activo = CASE WHEN is_activo = 1 THEN 0 ELSE 1 END WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("is_activo")) {
                throw new SQLException("La columna is_activo no existe. Ejecute la migración 003.");
            }
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    public int countUsersByRole(String roleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        Connection conn = getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } finally {
            closeConn(conn);
        }
        return 0;
    }

    public void eliminar(String id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        String deleteSql = "DELETE FROM roles WHERE id = ?";
        Connection conn = getConnection();
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        conn.rollback();
                        throw new SQLException("No se puede eliminar el rol porque tiene usuarios asignados.");
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
            closeConn(conn);
        }
    }

    private Rol mapRow(ResultSet rs, boolean hasActivo) throws SQLException {
        Rol rol = new Rol();
        rol.setId(rs.getString("id"));
        rol.setNombre(rs.getString("nombre"));
        if (hasActivo) {
            rol.setIsActivo(rs.getInt("is_activo"));
        } else {
            rol.setIsActivo(1);
        }
        return rol;
    }

    private void closeConn(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
