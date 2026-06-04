package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IdGenerator {

    /**
     * Generates a global sequential ID.
     */
    public static String generateId(Connection conn, String tableName, EntidadPrefix prefix, int padding) throws SQLException {
        String basePrefix = prefix.getPrefix();
        // Usar TOP 1 con ORDER BY es más fiable para encontrar el "último" ID alfanumérico en SQL Server
        String sql = "SELECT TOP 1 id FROM " + tableName + " WHERE id LIKE ? ORDER BY id DESC";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, basePrefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maxId = rs.getString(1);
                    if (maxId != null && !maxId.isBlank()) {
                        int nextVal = extractNextValue(maxId);
                        return formatId(basePrefix, nextVal, padding);
                    }
                }
            }
        }
        return formatId(basePrefix, 1, padding);
    }

    /**
     * Generates a sequential ID dependent on a Sede.
     * Format: [PREFIX]-[SEDE_NUM]-[SEQUENCE]
     */
    public static String generateSedeDependentId(Connection conn, String tableName, EntidadPrefix prefix, String sedeId, int padding) throws SQLException {
        // Extract numeric part of sedeId, e.g., SED-001 -> 001
        String sedeNum = sedeId.contains("-") ? sedeId.split("-")[1] : sedeId;
        String basePrefix = prefix.getPrefix() + "-" + sedeNum;
        
        String sql = "SELECT TOP 1 id FROM " + tableName + " WHERE id LIKE ? ORDER BY id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, basePrefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String maxId = rs.getString(1);
                    if (maxId != null && !maxId.isBlank()) {
                        int nextVal = extractNextValue(maxId);
                        return formatId(basePrefix, nextVal, padding);
                    }
                }
            }
        }
        return formatId(basePrefix, 1, padding);
    }

    private static int extractNextValue(String maxId) {
        if (maxId == null || maxId.isBlank()) return 1;
        try {
            String trimmed = maxId.trim();
            // Extraer la última secuencia de dígitos al final del string
            StringBuilder numPart = new StringBuilder();
            for (int i = trimmed.length() - 1; i >= 0; i--) {
                char c = trimmed.charAt(i);
                if (Character.isDigit(c)) {
                    numPart.insert(0, c);
                } else if (numPart.length() > 0) {
                    // Ya encontramos números y ahora llegamos a un separador
                    break;
                }
            }
            
            if (numPart.length() > 0) {
                return Integer.parseInt(numPart.toString()) + 1;
            }
        } catch (Exception e) {
            // Si algo falla, intentamos el split clásico como fallback
            try {
                String[] parts = maxId.split("-");
                return Integer.parseInt(parts[parts.length - 1]) + 1;
            } catch (Exception ex) {
                return 1;
            }
        }
        return 1;
    }

    private static String formatId(String prefix, int value, int padding) {
        return prefix + "-" + String.format("%0" + padding + "d", value);
    }
    
    // Legacy support for UUID if needed (will be phased out)
    @Deprecated
    public static String generateId(EntidadPrefix entidad) {
        java.util.UUID uuid = java.util.UUID.randomUUID();
        return entidad.getPrefix() + "-" + uuid.toString().substring(0, 8).toUpperCase();
    }
}
