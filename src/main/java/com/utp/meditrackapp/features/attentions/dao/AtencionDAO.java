package com.utp.meditrackapp.features.attentions.dao;

import com.utp.meditrackapp.core.dao.JdbcDaoSupport;
import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtencionDAO extends JdbcDaoSupport {

    public void insertar(Connection conn, Atencion atencion) throws SQLException {
        String sql = "INSERT INTO atenciones (id, sede_id, paciente_id, usuario_id, numero_receta, medico, fecha_atencion) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atencion.getId());
            ps.setString(2, atencion.getSedeId());
            ps.setString(3, atencion.getPacienteId());
            ps.setString(4, atencion.getUsuarioId());
            ps.setString(5, atencion.getNumeroReceta());
            ps.setString(6, atencion.getMedico());
            ps.executeUpdate();
        }
    }

    public boolean existeReceta(String sedeId, String numeroReceta) throws SQLException {
        String sql = "SELECT COUNT(*) FROM atenciones WHERE sede_id = ? AND numero_receta = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setString(2, numeroReceta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public void insertarDetalle(Connection conn, AtencionDetalle detalle) throws SQLException {
        String sql = "INSERT INTO atencion_detalles (id, atencion_id, lote_id, cantidad_entregada) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, detalle.getId());
            ps.setString(2, detalle.getAtencionId());
            ps.setString(3, detalle.getLoteId());
            ps.setInt(4, detalle.getCantidadEntregada());
            ps.executeUpdate();
        }
    }

    public List<Atencion> listarTodas() throws SQLException {
        List<Atencion> lista = new ArrayList<>();
        String sql = "SELECT * FROM atenciones ORDER BY fecha_atencion DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapAtencion(rs));
            }
        }
        return lista;
    }

    public Optional<Atencion> buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM atenciones WHERE id = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAtencion(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Atencion> listarPorPaciente(String pacienteId) throws SQLException {
        List<Atencion> lista = new ArrayList<>();
        String sql = "SELECT * FROM atenciones WHERE paciente_id = ? ORDER BY fecha_atencion DESC";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAtencion(rs));
                }
            }
        }
        return lista;
    }

    public List<com.utp.meditrackapp.core.models.dto.DispensacionReportItem> listarDispensacionesReporte(String sedeId, java.time.LocalDate desde, java.time.LocalDate hasta) throws SQLException {
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

        List<com.utp.meditrackapp.core.models.dto.DispensacionReportItem> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, sedeId);
            if (desde != null) ps.setDate(i++, java.sql.Date.valueOf(desde));
            if (hasta != null) ps.setDate(i++, java.sql.Date.valueOf(hasta));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    com.utp.meditrackapp.core.models.dto.DispensacionReportItem item = new com.utp.meditrackapp.core.models.dto.DispensacionReportItem();
                    Timestamp ts = rs.getTimestamp("fecha_atencion");
                    if (ts != null) item.setFecha(ts.toLocalDateTime());
                    item.setPaciente(rs.getString("paciente_nombre"));
                    item.setNumeroReceta(rs.getString("numero_receta"));
                    item.setProducto(rs.getString("producto_nombre"));
                    item.setLote(rs.getString("numero_lote"));
                    item.setCantidad(rs.getInt("cantidad_entregada"));
                    lista.add(item);
                }
            }
        }
        return lista;
    }

    private Atencion mapAtencion(ResultSet rs) throws SQLException {
        Atencion a = new Atencion();
        a.setId(rs.getString("id"));
        a.setSedeId(rs.getString("sede_id"));
        a.setPacienteId(rs.getString("paciente_id"));
        a.setUsuarioId(rs.getString("usuario_id"));
        a.setNumeroReceta(rs.getString("numero_receta"));
        a.setMedico(rs.getString("medico"));
        Timestamp ts = rs.getTimestamp("fecha_atencion");
        if (ts != null) {
            a.setFechaAtencion(ts.toLocalDateTime());
        }
        return a;
    }
}
