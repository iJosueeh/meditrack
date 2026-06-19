package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.domain.ports.out.DispensacionRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * Implementación JDBC del repositorio de dispensaciones.
 * Migrado desde AtencionDAO.listarDispensacionesReporte().
 */
public class JdbcDispensacionRepository implements DispensacionRepository {
    private final DatabaseConfig dbConfig;

    public JdbcDispensacionRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public List<Map<String, Object>> findByRangoFechas(String sedeId, LocalDate desde, LocalDate hasta) {
        StringBuilder sql = new StringBuilder(
            "SELECT a.fecha_atencion, p.nombres + ' ' + p.apellidos as paciente_nombre, a.numero_receta, " +
            "prod.nombre as producto_nombre, l.numero_lote, ad.cantidad_entregada " +
            "FROM atenciones a " +
            "JOIN atencion_detalles ad ON a.id = ad.atencion_id " +
            "JOIN pacientes p ON a.paciente_id = p.id " +
            "JOIN lotes l ON ad.lote_id = l.id " +
            "JOIN productos prod ON l.producto_id = prod.id " +
            "WHERE a.sede_id = ?"
        );

        if (desde != null) {
            sql.append(" AND CAST(a.fecha_atencion AS DATE) >= ?");
        }
        if (hasta != null) {
            sql.append(" AND CAST(a.fecha_atencion AS DATE) <= ?");
        }
        sql.append(" ORDER BY a.fecha_atencion DESC");

        List<Map<String, Object>> lista = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, sedeId);
            if (desde != null) ps.setDate(i++, java.sql.Date.valueOf(desde));
            if (hasta != null) ps.setDate(i++, java.sql.Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    Timestamp ts = rs.getTimestamp("fecha_atencion");
                    item.put("fecha", ts != null ? ts.toLocalDateTime() : null);
                    item.put("paciente", rs.getString("paciente_nombre"));
                    item.put("numeroReceta", rs.getString("numero_receta"));
                    item.put("producto", rs.getString("producto_nombre"));
                    item.put("lote", rs.getString("numero_lote"));
                    item.put("cantidad", rs.getInt("cantidad_entregada"));
                    lista.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
