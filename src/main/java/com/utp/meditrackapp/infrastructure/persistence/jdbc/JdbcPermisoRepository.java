package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.domain.entities.Permiso;
import com.utp.meditrackapp.domain.ports.out.PermisoRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del repositorio de Permisos.
 */
public class JdbcPermisoRepository implements PermisoRepository {
    private final DatabaseConfig dbConfig;

    public JdbcPermisoRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public List<Permiso> findAll() {
        String sql = "SELECT id, codigo, nombre, descripcion, modulo, orden, is_activo " +
                     "FROM permisos WHERE is_activo = 1 ORDER BY modulo, orden";
        List<Permiso> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPermiso(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Permiso> findByRolId(String rolId) {
        String sql = "SELECT p.id, p.codigo, p.nombre, p.descripcion, p.modulo, p.orden, p.is_activo " +
                     "FROM permisos p " +
                     "INNER JOIN rol_permisos rp ON p.id = rp.permiso_id " +
                     "WHERE rp.rol_id = ? AND p.is_activo = 1 " +
                     "ORDER BY p.modulo, p.orden";
        List<Permiso> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPermiso(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Permiso> findByModulo(String modulo) {
        String sql = "SELECT id, codigo, nombre, descripcion, modulo, orden, is_activo " +
                     "FROM permisos WHERE modulo = ? AND is_activo = 1 ORDER BY orden";
        List<Permiso> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, modulo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPermiso(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public void saveRolPermisos(String rolId, List<String> permisoIds) {
        String deleteSql = "DELETE FROM rol_permisos WHERE rol_id = ?";
        String insertSql = "INSERT INTO rol_permisos (rol_id, permiso_id) VALUES (?, ?)";
        
        try (Connection conn = dbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Primero eliminar permisos existentes
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setString(1, rolId);
                    ps.executeUpdate();
                }
                
                // Luego insertar nuevos permisos
                if (permisoIds != null && !permisoIds.isEmpty()) {
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        for (String permisoId : permisoIds) {
                            ps.setString(1, rolId);
                            ps.setString(2, permisoId);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar permisos del rol: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRolPermisos(String rolId) {
        String sql = "DELETE FROM rol_permisos WHERE rol_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rolId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar permisos del rol: " + e.getMessage(), e);
        }
    }

    @Override
    public int countPermisosByRol(String rolId) {
        String sql = "SELECT COUNT(*) FROM rol_permisos WHERE rol_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Permiso mapPermiso(ResultSet rs) throws SQLException {
        Permiso p = new Permiso();
        p.setId(rs.getString("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setModulo(rs.getString("modulo"));
        p.setOrden(rs.getInt("orden"));
        p.setIsActivo(rs.getInt("is_activo"));
        return p;
    }
}
