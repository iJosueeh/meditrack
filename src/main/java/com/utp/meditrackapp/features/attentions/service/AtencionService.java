package com.utp.meditrackapp.features.attentions.service;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.MovimientoDAO;
import com.utp.meditrackapp.core.models.entity.*;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.features.attentions.dao.AtencionDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AtencionService {
    private final AtencionDAO atencionDAO;
    private final LoteDAO loteDAO;
    private final MovimientoDAO movimientoDAO;
    private final SessionManager sessionManager;
    private final DatabaseConfig dbConfig;

    public AtencionService() {
        this.atencionDAO = new AtencionDAO();
        this.loteDAO = new LoteDAO();
        this.movimientoDAO = new MovimientoDAO();
        this.sessionManager = SessionManager.getInstance();
        this.dbConfig = DatabaseConfig.getInstance();
    }

    public String registrarAtencion(Atencion atencion, List<AtencionDetalle> detalles) {
        if (!sessionManager.isLoggedIn()) return "Sesión no iniciada.";

        Usuario user = sessionManager.getCurrentUser();
        if (!isAuthorized(user)) return "No tiene permisos para esta operación.";

        atencion.setUsuarioId(user.getId());
        atencion.setSedeId(user.getSedeId());

        if (atencion.getPacienteId() == null || atencion.getPacienteId().isEmpty()) return "El paciente es obligatorio.";
        if (detalles == null || detalles.isEmpty()) return "Debe agregar medicamentos.";
        if (atencion.getNumeroReceta() == null || atencion.getNumeroReceta().trim().isEmpty()) return "La receta es obligatoria.";

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Cabecera
            atencion.setId(IdGenerator.generateId(EntidadPrefix.ATENCION));
            atencionDAO.insertar(conn, atencion);

            // 2. Detalles y Stock
            for (AtencionDetalle det : detalles) {
                det.setId(IdGenerator.generateId(EntidadPrefix.ATENCION_DETALLE));
                det.setAtencionId(atencion.getId());
                atencionDAO.insertarDetalle(conn, det);

                loteDAO.reducirStock(conn, det.getLoteId(), det.getCantidadEntregada());

                // Movimiento usando Enums
                Movimiento mov = new Movimiento();
                mov.setTipoId(TipoMovimientoEnum.SALIDA.getId());
                mov.setMotivoId(MotivoMovimientoEnum.ATENCION.getId());
                mov.setSedeId(atencion.getSedeId());
                mov.setUsuarioId(atencion.getUsuarioId());
                mov.setLoteId(det.getLoteId());
                mov.setCantidad(det.getCantidadEntregada());
                mov.setObservacion("Atención Médica - Receta: " + atencion.getNumeroReceta());
                movimientoDAO.registrarMovimiento(conn, mov);
            }

            conn.commit();
            return "OK";
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return "Error: " + e.getMessage();
        } finally {
            close(conn);
        }
    }

    public List<Atencion> listarHistorial() {
        try {
            return atencionDAO.listarTodas();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private boolean isAuthorized(Usuario user) {
        return sessionManager.isAdmin() || sessionManager.isQuimico() || sessionManager.isTecnico();
    }

    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try { 
                conn.setAutoCommit(true);
                conn.close(); 
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}
