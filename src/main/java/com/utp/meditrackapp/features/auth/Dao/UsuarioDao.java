package com.utp.meditrackapp.features.auth.Dao;


import java.sql.*;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.core.util.PasswordHasher;

public class UsuarioDao {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    public Usuario login(String numeroDocumento, String password) {
        String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                     "FROM usuarios u " +
                     "LEFT JOIN sedes s ON u.sede_id = s.id " +
                     "LEFT JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.numero_documento = ? AND u.is_activo = 1";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, numeroDocumento);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    
                    if (PasswordHasher.checkPassword(password, storedHash)) {
                        Usuario usuario = new Usuario(
                            rs.getString("id"),
                            rs.getString("sede_id"),
                            rs.getString("rol_id"),
                            rs.getString("tipo_documento"),
                            rs.getString("numero_documento"),
                            rs.getString("nombres"),
                            rs.getString("apellidos"),
                            storedHash,
                            rs.getInt("is_activo")
                        );
                        usuario.setSedeNombre(rs.getString("sede_nombre"));
                        usuario.setRolNombre(rs.getString("rol_nombre"));
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en login: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtiene la fecha de la última acción registrada por el usuario (movimiento o atención).
     */
    public String getUltimaActividad(String usuarioId) {
        String sql = "SELECT MAX(fecha) as ultima FROM (" +
                     "  SELECT fecha_registro as fecha FROM movimientos WHERE usuario_id = ? " +
                     "  UNION " +
                     "  SELECT fecha_atencion as fecha FROM atenciones WHERE usuario_id = ?" +
                     ") as Actividad";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
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
            System.err.println("[DB ERROR] Error al obtener actividad: " + e.getMessage());
        }
        return "Sin actividad reciente";
    }

    public boolean updateUser(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombres = ?, apellidos = ?, tipo_documento = ?, numero_documento = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usuario.getNombres());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getTipoDocumento());
            ps.setString(4, usuario.getNumeroDocumento());
            ps.setString(5, usuario.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean updatePassword(String usuarioId, String hashedPassword) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, hashedPassword);
            ps.setString(2, usuarioId);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al actualizar contraseña: " + e.getMessage());
            return false;
        }
    }
        
}
