package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.Movimiento;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO extends JdbcDaoSupport {

    public void registrarMovimiento(Connection connection, Movimiento movimiento) throws SQLException {
        if (movimiento.getId() == null || movimiento.getId().isBlank()) {
            movimiento.setId(IdGenerator.generateSedeDependentId(connection, "movimientos", EntidadPrefix.MOVIMIENTO, movimiento.getSedeId(), 6));
        }

        String sql = "INSERT INTO movimientos (id, tipo_id, motivo_id, sede_id, usuario_id, lote_id, cantidad, observacion, fecha_registro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, movimiento.getId());
            ps.setString(2, movimiento.getTipoId());
            ps.setString(3, movimiento.getMotivoId());
            ps.setString(4, movimiento.getSedeId());
            ps.setString(5, movimiento.getUsuarioId());
            ps.setString(6, movimiento.getLoteId());
            ps.setInt(7, movimiento.getCantidad());
            ps.setString(8, movimiento.getObservacion());
            ps.executeUpdate();
        }
    }

    public List<Movimiento> listarPorSede(String sedeId, String tipoId, String buscar) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT m.*, tm.nombre as tipo_nombre, mm.nombre as motivo_nombre, p.nombre as producto_nombre, l.numero_lote " +
            "FROM movimientos m " +
            "JOIN tipos_movimiento tm ON m.tipo_id = tm.id " +
            "JOIN motivos_movimiento mm ON m.motivo_id = mm.id " +
            "JOIN lotes l ON m.lote_id = l.id " +
            "JOIN productos p ON l.producto_id = p.id " +
            "WHERE m.sede_id = ?"
        );

        if (tipoId != null && !tipoId.isEmpty()) {
            sql.append(" AND m.tipo_id = ?");
        }
        if (buscar != null && !buscar.isEmpty()) {
            sql.append(" AND (p.nombre LIKE ? OR l.numero_lote LIKE ?)");
        }
        sql.append(" ORDER BY m.fecha_registro DESC");

        List<Movimiento> lista = new ArrayList<>();
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, sedeId);
            if (tipoId != null && !tipoId.isEmpty()) ps.setString(i++, tipoId);
            if (buscar != null && !buscar.isEmpty()) {
                String pattern = "%" + buscar + "%";
                ps.setString(i++, pattern);
                ps.setString(i++, pattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Movimiento m = mapMovimiento(rs);
                    // Usaremos campos extra o DTO si fuera necesario, pero por ahora mapeamos lo básico
                    // y podríamos setear nombres en el objeto si tuviera esos campos.
                    // Asumiremos que el modelo Movimiento tiene campos para nombres o usaremos un Wrapper.
                    lista.add(m);
                }
            }
        }
        return lista;
    }

    private Movimiento mapMovimiento(ResultSet rs) throws SQLException {
        Movimiento m = new Movimiento();
        m.setId(rs.getString("id"));
        m.setTipoId(rs.getString("tipo_id"));
        m.setMotivoId(rs.getString("motivo_id"));
        m.setSedeId(rs.getString("sede_id"));
        m.setUsuarioId(rs.getString("usuario_id"));
        m.setLoteId(rs.getString("lote_id"));
        m.setCantidad(rs.getInt("cantidad"));
        m.setObservacion(rs.getString("observacion"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) m.setFechaRegistro(ts.toLocalDateTime());
        
        // Map transient fields if present in ResultSet
        try { m.setTipoNombre(rs.getString("tipo_nombre")); } catch (SQLException e) {}
        try { m.setMotivoNombre(rs.getString("motivo_nombre")); } catch (SQLException e) {}
        try { m.setProductoNombre(rs.getString("producto_nombre")); } catch (SQLException e) {}
        try { m.setNumeroLote(rs.getString("numero_lote")); } catch (SQLException e) {}
        
        return m;
    }
}
