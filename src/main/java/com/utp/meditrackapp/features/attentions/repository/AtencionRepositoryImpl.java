package com.utp.meditrackapp.features.attentions.repository;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.MovimientoDAO;
import com.utp.meditrackapp.core.models.entity.Movimiento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AtencionRepositoryImpl implements AtencionRepository {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();
    private final LoteDAO loteDAO = new LoteDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();

    @Override
    public boolean registrarAtencionCompleta(Atencion atencion, List<AtencionDetalle> detalles) {
        String sqlAtencion = "INSERT INTO atenciones (id, sede_id, paciente_id, usuario_id, numero_receta, fecha_atencion) VALUES (?, ?, ?, ?, ?, GETDATE())";
        String sqlDetalle = "INSERT INTO atencion_detalles (id, atencion_id, lote_id, cantidad_entregada) VALUES (?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false); // INICIO DE TRANSACCIÓN ACID

            // 1. Insertar Cabecera (Atención)
            String atencionId = IdGenerator.generateId(EntidadPrefix.ATENCION);
            try (PreparedStatement psA = conn.prepareStatement(sqlAtencion)) {
                psA.setString(1, atencionId);
                psA.setString(2, atencion.getSedeId());
                psA.setString(3, atencion.getPacienteId());
                psA.setString(4, atencion.getUsuarioId());
                psA.setString(5, atencion.getNumeroReceta());
                psA.executeUpdate();
            }

            // 2. Insertar Detalles, Actualizar Lotes y Registrar Movimientos
            for (AtencionDetalle det : detalles) {
                // Insertar Detalle
                String detalleId = IdGenerator.generateId(EntidadPrefix.ATENCION_DETALLE);
                try (PreparedStatement psD = conn.prepareStatement(sqlDetalle)) {
                    psD.setString(1, detalleId);
                    psD.setString(2, atencionId);
                    psD.setString(3, det.getLoteId());
                    psD.setInt(4, det.getCantidadEntregada());
                    psD.executeUpdate();
                }

                // Actualizar Stock en Lote usando LoteDAO (Compartido)
                loteDAO.reducirStock(conn, det.getLoteId(), det.getCantidadEntregada());

                // Registrar Movimiento de Salida usando MovimientoDAO (Compartido)
                Movimiento mov = new Movimiento();
                mov.setTipoId("MOV-T-02"); // salida
                mov.setMotivoId("MOV-M-03"); // atencion
                mov.setSedeId(atencion.getSedeId());
                mov.setUsuarioId(atencion.getUsuarioId());
                mov.setLoteId(det.getLoteId());
                mov.setCantidad(det.getCantidadEntregada());
                mov.setObservacion("Salida por Atención Médica - Receta: " + atencion.getNumeroReceta());
                
                movimientoDAO.registrarMovimiento(conn, mov);
            }

            conn.commit(); // ÉXITO: Confirmar todos los cambios
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    System.err.println("[ACID ROLLBACK] Error en atención, deshaciendo cambios: " + e.getMessage());
                    conn.rollback(); // FALLO: Deshacer todo el bloque
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public List<Atencion> findAll() {
        List<Atencion> atenciones = new ArrayList<>();
        String sql = "SELECT * FROM atenciones ORDER BY fecha_atencion DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                atenciones.add(new Atencion(
                    rs.getString("id"),
                    rs.getString("sede_id"),
                    rs.getString("paciente_id"),
                    rs.getString("usuario_id"),
                    rs.getString("numero_receta"),
                    rs.getTimestamp("fecha_atencion") != null ? rs.getTimestamp("fecha_atencion").toLocalDateTime() : null
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return atenciones;
    }

    @Override
    public Atencion findById(String id) {
        String sql = "SELECT * FROM atenciones WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Atencion(
                        rs.getString("id"),
                        rs.getString("sede_id"),
                        rs.getString("paciente_id"),
                        rs.getString("usuario_id"),
                        rs.getString("numero_receta"),
                        rs.getTimestamp("fecha_atencion").toLocalDateTime()
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
