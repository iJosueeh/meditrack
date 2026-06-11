package com.utp.meditrackapp.features.search.dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.features.search.models.SearchResult;
import com.utp.meditrackapp.features.search.models.SearchResult.ResultType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalSearchDAO {

    public List<SearchResult> searchGlobal(String query) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return results;

        String[] terms = query.trim().split("\\s+");
        
        // Dynamic SQL for Pacientes
        StringBuilder sqlPatients = new StringBuilder("SELECT TOP 5 id, (nombres + ' ' + apellidos) as title, numero_documento as subtitle FROM pacientes WHERE 1=1");
        for (int i = 0; i < terms.length; i++) {
            sqlPatients.append(" AND (nombres LIKE ? OR apellidos LIKE ? OR numero_documento LIKE ?)");
        }
        
        // Dynamic SQL for Productos
        StringBuilder sqlProducts = new StringBuilder("SELECT TOP 5 id, nombre as title, codigo_digemid as subtitle FROM productos WHERE 1=1");
        for (int i = 0; i < terms.length; i++) {
            sqlProducts.append(" AND (nombre LIKE ? OR codigo_digemid LIKE ?)");
        }

        // Query para lotes (simple search by batch number)
        String sqlBatches = "SELECT TOP 5 l.id, l.numero_lote as title, p.nombre as subtitle FROM lotes l " +
                            "JOIN productos p ON l.producto_id = p.id " +
                            "WHERE l.numero_lote LIKE ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            // Buscar Pacientes
            try (PreparedStatement ps = conn.prepareStatement(sqlPatients.toString())) {
                int paramIndex = 1;
                for (String term : terms) {
                    String pattern = "%" + term + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult(rs.getString("title"), rs.getString("subtitle"), ResultType.PATIENT, rs.getString("id")));
                    }
                }
            }

            // Buscar Productos
            try (PreparedStatement ps = conn.prepareStatement(sqlProducts.toString())) {
                int paramIndex = 1;
                for (String term : terms) {
                    String pattern = "%" + term + "%";
                    ps.setString(paramIndex++, pattern);
                    ps.setString(paramIndex++, pattern);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult(rs.getString("title"), rs.getString("subtitle"), ResultType.PRODUCT, rs.getString("id")));
                    }
                }
            }

            // Buscar Lotes
            try (PreparedStatement ps = conn.prepareStatement(sqlBatches)) {
                ps.setString(1, "%" + query.trim() + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        results.add(new SearchResult(rs.getString("title"), rs.getString("subtitle"), ResultType.BATCH, rs.getString("id")));
                    }
                }
            }
        }

        // Búsqueda estática de módulos (hardcoded para velocidad)
        String q = query.toLowerCase();
        if (q.contains("inventario")) results.add(new SearchResult("Módulo de Inventario", "Navegar a existencias", ResultType.MODULE, "NAV_INV"));
        if (q.contains("atenciones") || q.contains("dispensacion")) results.add(new SearchResult("Registro de Atenciones", "Navegar a dispensación", ResultType.MODULE, "NAV_ATT"));
        if (q.contains("sedes")) results.add(new SearchResult("Gestión de Sedes", "Administración global", ResultType.MODULE, "NAV_SEDE"));

        return results;
    }
}
