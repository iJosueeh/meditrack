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
        String sql = "SELECT MAX(id) FROM [" + tableName + "] WHERE id LIKE ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, basePrefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    String maxId = rs.getString(1);
                    int nextVal = extractNextValue(maxId);
                    return formatId(basePrefix, nextVal, padding);
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
        
        String sql = "SELECT MAX(id) FROM [" + tableName + "] WHERE id LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, basePrefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString(1) != null) {
                    String maxId = rs.getString(1);
                    int nextVal = extractNextValue(maxId);
                    return formatId(basePrefix, nextVal, padding);
                }
            }
        }
        return formatId(basePrefix, 1, padding);
    }

    private static int extractNextValue(String maxId) {
        try {
            String[] parts = maxId.split("-");
            String lastPart = parts[parts.length - 1];
            return Integer.parseInt(lastPart) + 1;
        } catch (Exception e) {
            return 1;
        }
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
