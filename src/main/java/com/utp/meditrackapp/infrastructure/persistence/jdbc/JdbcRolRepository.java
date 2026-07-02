package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Permiso;
import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.ports.out.RolRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcRolRepository implements RolRepository {
    private final JdbcPermisoRepository permisoRepository;

    public JdbcRolRepository() {
        this.permisoRepository = new JdbcPermisoRepository();
    }

    @Override
    public Optional<Rol> findById(String id) {
        String sql = "SELECT id, nombre, descripcion, nivel, is_sistema, is_activo FROM roles WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Rol rol = mapResultSetToRol(rs);
                    // Cargar permisos del rol
                    List<Permiso> permisos = permisoRepository.findByRolId(id);
                    rol.setPermisos(permisos);
                    return Optional.of(rol);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Rol> findAll() {
        String sql = "SELECT id, nombre, descripcion, nivel, is_sistema, is_activo FROM roles ORDER BY nivel, nombre";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Rol> roles = new ArrayList<>();
            while (rs.next()) {
                Rol rol = mapResultSetToRol(rs);
                // Cargar permisos del rol
                List<Permiso> permisos = permisoRepository.findByRolId(rol.getId());
                rol.setPermisos(permisos);
                roles.add(rol);
            }
            return roles;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Rol save(Rol rol) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String id = rol.getId();
            if (id == null || id.isBlank()) {
                id = IdGenerator.generateId(conn, "roles", EntidadPrefix.ROL, 6);
                rol.setId(id);
            }

            String sql = "INSERT INTO roles (id, nombre, descripcion, nivel, is_sistema, is_activo) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, rol.getNombre());
                ps.setString(3, rol.getDescripcion());
                ps.setInt(4, rol.getNivel());
                ps.setInt(5, rol.getIsSistema());
                ps.setInt(6, rol.getIsActivo());
                ps.executeUpdate();
            }
            return rol;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Rol rol) {
        String sql = "UPDATE roles SET nombre = ?, descripcion = ?, nivel = ?, is_activo = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rol.getNombre());
            ps.setString(2, rol.getDescripcion());
            ps.setInt(3, rol.getNivel());
            ps.setInt(4, rol.getIsActivo());
            ps.setString(5, rol.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toggleEstado(String id) {
        String sql = "UPDATE roles SET is_activo = CASE WHEN is_activo = 1 THEN 0 ELSE 1 END WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM roles WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countUsersByRole(String roleId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Rol mapResultSetToRol(ResultSet rs) throws SQLException {
        Rol r = new Rol();
        r.setId(rs.getString("id"));
        r.setNombre(rs.getString("nombre"));
        r.setDescripcion(rs.getString("descripcion"));
        r.setNivel(rs.getInt("nivel"));
        r.setIsSistema(rs.getInt("is_sistema"));
        r.setIsActivo(rs.getInt("is_activo"));
        return r;
    }
}
