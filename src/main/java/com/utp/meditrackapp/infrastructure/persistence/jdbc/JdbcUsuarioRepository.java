package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.core.util.PasswordHasher;
import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.ports.out.UsuarioRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Usuarios.
 * Migrado desde UsuarioDao.
 */
public class JdbcUsuarioRepository implements UsuarioRepository {
    private final DatabaseConfig dbConfig;

    public JdbcUsuarioRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Optional<Usuario> findById(String id) {
        String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                     "FROM usuarios u " +
                     "LEFT JOIN sedes s ON u.sede_id = s.id " +
                     "LEFT JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Usuario findByDocumento(String numeroDocumento) {
        String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                     "FROM usuarios u " +
                     "LEFT JOIN sedes s ON u.sede_id = s.id " +
                     "LEFT JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.numero_documento = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Usuario authenticate(String numeroDocumento, String password) {
        String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                     "FROM usuarios u " +
                     "LEFT JOIN sedes s ON u.sede_id = s.id " +
                     "LEFT JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.numero_documento = ? AND u.is_activo = 1";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordHasher.checkPassword(password, storedHash)) {
                        Usuario u = mapUsuario(rs);
                        u.setPassword(storedHash);
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Usuario> findByRolId(String rolId) {
        String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                     "FROM usuarios u " +
                     "LEFT JOIN sedes s ON u.sede_id = s.id " +
                     "LEFT JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.rol_id = ? AND u.is_activo = 1 " +
                     "ORDER BY u.nombres ASC";
        List<Usuario> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rolId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                     "FROM usuarios u " +
                     "LEFT JOIN sedes s ON u.sede_id = s.id " +
                     "LEFT JOIN roles r ON u.rol_id = r.id " +
                     "ORDER BY u.nombres ASC";
        List<Usuario> list = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapUsuario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Usuario save(Usuario usuario, String rawPassword) {
        try (Connection conn = dbConfig.getConnection()) {
            if (usuario.getId() == null || usuario.getId().isBlank()) {
                usuario.setId(IdGenerator.generateSedeDependentId(
                    conn, "usuarios", EntidadPrefix.USUARIO, usuario.getSedeId(), 4));
            }

            String hashedPassword = PasswordHasher.hashPassword(rawPassword);

            String sql = "INSERT INTO usuarios (id, sede_id, rol_id, tipo_documento, numero_documento, nombres, apellidos, password, is_activo) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usuario.getId());
                ps.setString(2, usuario.getSedeId());
                ps.setString(3, usuario.getRolId());
                ps.setString(4, usuario.getTipoDocumento());
                ps.setString(5, usuario.getNumeroDocumento());
                ps.setString(6, usuario.getNombres());
                ps.setString(7, usuario.getApellidos());
                ps.setString(8, hashedPassword);
                ps.setInt(9, 1);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        }
        return usuario;
    }

    @Override
    public Usuario update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombres = ?, apellidos = ?, tipo_documento = ?, numero_documento = ?, sede_id = ?, rol_id = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getTipoDocumento());
            ps.setString(4, usuario.getNumeroDocumento());
            ps.setString(5, usuario.getSedeId());
            ps.setString(6, usuario.getRolId());
            ps.setString(7, usuario.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar usuario: " + e.getMessage(), e);
        }
        return usuario;
    }

    @Override
    public boolean updatePassword(String usuarioId, String hashedPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, usuarioId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void toggleEstado(String id, int nuevoEstado) {
        String sql = "UPDATE usuarios SET is_activo = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nuevoEstado);
            ps.setString(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al toggle estado de usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUltimaActividad(String usuarioId) {
        String sql = "SELECT MAX(fecha) as ultima FROM (" +
                     "  SELECT fecha_registro as fecha FROM movimientos WHERE usuario_id = ? " +
                     "  UNION " +
                     "  SELECT fecha_atencion as fecha FROM atenciones WHERE usuario_id = ?" +
                     ") as Actividad";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, usuarioId);
            ps.setString(2, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getTimestamp("ultima") != null) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                    return sdf.format(rs.getTimestamp("ultima"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Sin actividad reciente";
    }

    @Override
    public List<Rol> findAllRoles() {
        String sql = "SELECT * FROM roles ORDER BY nombre";
        List<Rol> roles = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new Rol(rs.getString("id"), rs.getString("nombre")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    @Override
    public List<Sede> findSedesActivas() {
        String sql = "SELECT * FROM sedes WHERE is_activa = 1 ORDER BY nombre";
        List<Sede> sedes = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Sede s = new Sede();
                s.setId(rs.getString("id"));
                s.setNombre(rs.getString("nombre"));
                s.setDireccion(rs.getString("direccion"));
                s.setIsActiva(rs.getInt("is_activa"));
                sedes.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sedes;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getString("id"));
        u.setSedeId(rs.getString("sede_id"));
        u.setRolId(rs.getString("rol_id"));
        u.setTipoDocumento(rs.getString("tipo_documento"));
        u.setNumeroDocumento(rs.getString("numero_documento"));
        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setIsActivo(rs.getInt("is_activo"));
        try { u.setSedeNombre(rs.getString("sede_nombre")); } catch (SQLException ignored) {}
        try { u.setRolNombre(rs.getString("rol_nombre")); } catch (SQLException ignored) {}
        return u;
    }
}
