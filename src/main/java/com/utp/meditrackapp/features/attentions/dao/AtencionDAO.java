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
        String sql = "INSERT INTO atenciones (id, sede_id, paciente_id, usuario_id, numero_receta, fecha_atencion) VALUES (?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atencion.getId());
            ps.setString(2, atencion.getSedeId());
            ps.setString(3, atencion.getPacienteId());
            ps.setString(4, atencion.getUsuarioId());
            ps.setString(5, atencion.getNumeroReceta());
            ps.executeUpdate();
        }
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

    private Atencion mapAtencion(ResultSet rs) throws SQLException {
        Atencion a = new Atencion();
        a.setId(rs.getString("id"));
        a.setSedeId(rs.getString("sede_id"));
        a.setPacienteId(rs.getString("paciente_id"));
        a.setUsuarioId(rs.getString("usuario_id"));
        a.setNumeroReceta(rs.getString("numero_receta"));
        Timestamp ts = rs.getTimestamp("fecha_atencion");
        if (ts != null) {
            a.setFechaAtencion(ts.toLocalDateTime());
        }
        return a;
    }
}
