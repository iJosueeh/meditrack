package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

public class IdGenerator {

    private static final Set<String> ALLOWED_TABLES = Set.of(
        "sedes", "usuarios", "atenciones", "atencion_detalles",
        "categorias", "tipos_movimiento", "motivos_movimiento",
        "pacientes", "roles", "lotes", "productos", "movimientos"
    );

    /**
     * Generates a global sequential ID.
     */
    public static String generateId(Connection conn, String tableName, EntidadPrefix prefix, int padding) throws SQLException {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        String basePrefix = prefix.getPrefix();
        String sql = "SELECT id FROM " + tableName + " WHERE id LIKE ?";
        
        int maxVal = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, basePrefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    int val = extractNumericValue(id);
                    if (val > maxVal) maxVal = val;
                }
            }
        }
        return formatId(basePrefix, maxVal + 1, padding);
    }

    /**
     * Generates a sequential ID dependent on a Sede.
     * Format: [PREFIX]-[SEDE_NUM]-[SEQUENCE]
     */
    public static String generateSedeDependentId(Connection conn, String tableName, EntidadPrefix prefix, String sedeId, int padding) throws SQLException {
        if (!ALLOWED_TABLES.contains(tableName)) {
            throw new IllegalArgumentException("Invalid table name: " + tableName);
        }
        // Extract numeric part of sedeId, e.g., SED-001 -> 001
        String sedeNum = sedeId.contains("-") ? sedeId.split("-")[1] : sedeId;
        String basePrefix = prefix.getPrefix() + "-" + sedeNum;
        
        String sql = "SELECT id FROM " + tableName + " WHERE id LIKE ?";
        int maxVal = 0;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, basePrefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String id = rs.getString(1);
                    int val = extractNumericValue(id);
                    if (val > maxVal) maxVal = val;
                }
            }
        }
        return formatId(basePrefix, maxVal + 1, padding);
    }

    private static int extractNumericValue(String id) {
        if (id == null || id.isBlank()) return 0;
        try {
            String trimmed = id.trim();
            // Buscamos la última secuencia de números
            StringBuilder numPart = new StringBuilder();
            for (int i = trimmed.length() - 1; i >= 0; i--) {
                char c = trimmed.charAt(i);
                if (Character.isDigit(c)) {
                    numPart.insert(0, c);
                } else if (numPart.length() > 0) {
                    break;
                }
            }
            if (numPart.length() > 0) {
                return Integer.parseInt(numPart.toString());
            }
        } catch (Exception e) {
            // Fallback
        }
        return 0;
    }

    private static int extractNextValue(String maxId) {
        return extractNumericValue(maxId) + 1;
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
