package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SedeResolver {
    
    public static String getSedeName(Usuario user) {
        if (user == null) return "Sede Desconocida";
        
        String currentName = user.getSedeNombre();
        
        // If the name is valid and doesn't look like an ID, return it
        if (currentName != null && !currentName.isEmpty() && !currentName.startsWith("SED-")) {
            return currentName;
        }
        
        // Fallback: Fetch from database using the ID
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT nombre FROM sedes WHERE id = ?")) {
            ps.setString(1, user.getSedeId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("nombre");
                    user.setSedeNombre(name); // Cache it in the session object
                    return name;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return "Sede Central"; // Final fallback
    }
}
