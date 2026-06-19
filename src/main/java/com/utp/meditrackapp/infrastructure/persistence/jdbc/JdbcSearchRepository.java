package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.domain.ports.out.SearchRepository;
import com.utp.meditrackapp.features.search.models.SearchResult;
import com.utp.meditrackapp.features.search.models.SearchResult.ResultType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementacion JDBC del repositorio de busqueda global.
 * Migrado desde GlobalSearchDAO.
 */
public class JdbcSearchRepository implements SearchRepository {

    @Override
    public List<SearchResult> searchGlobal(String query) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) return results;

        String[] terms = query.trim().split("\\s+");
        
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            results.addAll(searchPatients(conn, terms));
            results.addAll(searchProducts(conn, terms));
            results.addAll(searchBatches(conn, query.trim()));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String q = query.toLowerCase();
        if (q.contains("inventario")) results.add(new SearchResult("Modulo de Inventario", "Navegar a existencias", ResultType.MODULE, "NAV_INV"));
        if (q.contains("atenciones") || q.contains("dispensacion")) results.add(new SearchResult("Registro de Atenciones", "Navegar a dispensacion", ResultType.MODULE, "NAV_ATT"));
        if (q.contains("sedes")) results.add(new SearchResult("Gestion de Sedes", "Administracion global", ResultType.MODULE, "NAV_SEDE"));

        return results;
    }

    private List<SearchResult> searchPatients(Connection conn, String[] terms) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP 5 id, (nombres + ' ' + apellidos) as title, numero_documento as subtitle FROM pacientes WHERE 1=1");
        for (int i = 0; i < terms.length; i++) {
            sql.append(" AND (nombres LIKE ? OR apellidos LIKE ? OR numero_documento LIKE ?)");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (String term : terms) {
                String pattern = "%" + term + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                        rs.getString("title"),
                        rs.getString("subtitle"),
                        ResultType.PATIENT,
                        rs.getString("id")));
                }
            }
        }
        return results;
    }

    private List<SearchResult> searchProducts(Connection conn, String[] terms) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT TOP 5 id, nombre as title, codigo_digemid as subtitle FROM productos WHERE 1=1");
        for (int i = 0; i < terms.length; i++) {
            sql.append(" AND (nombre LIKE ? OR codigo_digemid LIKE ?)");
        }

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            for (String term : terms) {
                String pattern = "%" + term + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                        rs.getString("title"),
                        rs.getString("subtitle"),
                        ResultType.PRODUCT,
                        rs.getString("id")));
                }
            }
        }
        return results;
    }

    private List<SearchResult> searchBatches(Connection conn, String query) throws SQLException {
        List<SearchResult> results = new ArrayList<>();
        String sql = "SELECT TOP 5 l.id, l.numero_lote as title, p.nombre as subtitle FROM lotes l " +
                     "JOIN productos p ON l.producto_id = p.id " +
                     "WHERE l.numero_lote LIKE ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + query + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SearchResult(
                        rs.getString("title"),
                        rs.getString("subtitle"),
                        ResultType.BATCH,
                        rs.getString("id")));
                }
            }
        }
        return results;
    }
}
