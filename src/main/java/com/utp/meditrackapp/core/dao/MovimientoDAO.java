package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.Movimiento;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MovimientoDAO extends JdbcDaoSupport {

    /**
     * Registra un movimiento dentro de una conexión existente (transaccional).
     */
    public void registrarMovimiento(Connection connection, Movimiento movimiento) throws SQLException {
        if (movimiento.getId() == null || movimiento.getId().isBlank()) {
            movimiento.setId(IdGenerator.generateId(EntidadPrefix.MOVIMIENTO));
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
}
