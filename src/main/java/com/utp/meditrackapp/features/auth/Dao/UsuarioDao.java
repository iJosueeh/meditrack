package com.utp.meditrackapp.features.auth.Dao;


import java.sql.*;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.core.util.PasswordHasher;

public class UsuarioDao {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();


    public Usuario login(String dni, String password){
        String sql = "SELECT * FROM usuarios WHERE numero_documento = ? AND is_activo = 1";

        try(Connection conn = dbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, dni);

            try (ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    String storedHash = rs.getString("password");
                    if (PasswordHasher.checkPassword(password, storedHash)){

                        return new Usuario(
                            rs.getString("id"),
                            rs.getString("sede_id"),
                            rs.getString("rol_id"),
                            rs.getString("tipo_documento"),
                            rs.getString("numero_documento"),
                            rs.getString("nombres"),
                            rs.getString("apellidos"),
                            storedHash,
                            rs.getInt("is_activo") == 1
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en login: " + e.getMessage());
        }
        return null;
    }
        
}
