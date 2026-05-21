package com.utp.meditrackapp.features.inventory.service;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.MovimientoDAO;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.models.entity.Movimiento;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class MovimientoService {
    private final LoteDAO loteDAO = new LoteDAO();
    private final MovimientoDAO movimientoDAO = new MovimientoDAO();
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    public List<Movimiento> listarMovimientos(String sedeId, String tipoId, String buscar) throws SQLException {
        return movimientoDAO.listarPorSede(sedeId, tipoId, buscar);
    }

    /**
     * Registra una entrada de stock (Compra).
     */
    public boolean registrarEntrada(Lote lote, String usuarioId, String observacion) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Registrar el Lote (Si es nuevo lo inserta, si ya tiene ID debería existir)
            if (lote.getId() == null || lote.getId().isEmpty()) {
                loteDAO.registrarIngreso(conn, lote);
            } else {
                loteDAO.aumentarStock(conn, lote.getId(), lote.getCantidad());
            }

            // 2. Registrar Movimiento
            Movimiento mov = new Movimiento();
            mov.setTipoId("MOV-T-01"); // entrada
            mov.setMotivoId("MOV-M-01"); // compra
            mov.setSedeId(lote.getSedeId());
            mov.setUsuarioId(usuarioId);
            mov.setLoteId(lote.getId());
            mov.setCantidad(lote.getCantidad());
            mov.setObservacion(observacion);
            movimientoDAO.registrarMovimiento(conn, mov);

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally {
            close(conn);
        }
    }

    /**
     * Registra una merma (Pérdida/Dañado).
     */
    public boolean registrarMerma(String loteId, int cantidad, String usuarioId, String observacion) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Obtener info del lote para la sede
            Optional<Lote> loteOpt = loteDAO.buscarPorId(loteId);
            if (loteOpt.isEmpty()) throw new SQLException("Lote no encontrado.");
            Lote lote = loteOpt.get();

            // 2. Reducir Stock (Con validación de concurrencia integrada)
            loteDAO.reducirStock(conn, loteId, cantidad);

            // 3. Registrar Movimiento
            Movimiento mov = new Movimiento();
            mov.setTipoId("MOV-T-02"); // salida
            mov.setMotivoId("MOV-M-04"); // merma
            mov.setSedeId(lote.getSedeId());
            mov.setUsuarioId(usuarioId);
            mov.setLoteId(loteId);
            mov.setCantidad(cantidad);
            mov.setObservacion(observacion);
            movimientoDAO.registrarMovimiento(conn, mov);

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally {
            close(conn);
        }
    }

    /**
     * Registra una transferencia entre sedes.
     */
    public boolean registrarTransferencia(String loteId, String sedeDestinoId, int cantidad, String usuarioId, String observacion) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Origen: Reducir Stock
            Optional<Lote> loteOrigenOpt = loteDAO.buscarPorId(loteId);
            if (loteOrigenOpt.isEmpty()) throw new SQLException("Lote origen no encontrado.");
            Lote loteOrigen = loteOrigenOpt.get();
            loteDAO.reducirStock(conn, loteId, cantidad);

            // 2. Origen: Registrar Movimiento de Salida
            Movimiento movSalida = new Movimiento();
            movSalida.setTipoId("MOV-T-02"); // salida
            movSalida.setMotivoId("MOV-M-02"); // transferencia
            movSalida.setSedeId(loteOrigen.getSedeId());
            movSalida.setUsuarioId(usuarioId);
            movSalida.setLoteId(loteId);
            movSalida.setCantidad(cantidad);
            movSalida.setObservacion("Transferencia a sede: " + sedeDestinoId + ". " + observacion);
            movimientoDAO.registrarMovimiento(conn, movSalida);

            // 3. Destino: Crear o aumentar stock
            // Para simplificar, creamos un nuevo registro de lote en la sede destino con el mismo número de lote
            Lote loteDestino = new Lote();
            loteDestino.setProductoId(loteOrigen.getProductoId());
            loteDestino.setSedeId(sedeDestinoId);
            loteDestino.setNumeroLote(loteOrigen.getNumeroLote());
            loteDestino.setFechaVencimiento(loteOrigen.getFechaVencimiento());
            loteDestino.setFechaFabricacion(loteOrigen.getFechaFabricacion());
            loteDestino.setCantidad(cantidad);
            
            // Usamos la conexión actual para que sea atómico
            loteDAO.registrarIngreso(conn, loteDestino); 

            // 4. Destino: Registrar Movimiento de Entrada
            Movimiento movEntrada = new Movimiento();
            movEntrada.setTipoId("MOV-T-01"); // entrada
            movEntrada.setMotivoId("MOV-M-02"); // transferencia
            movEntrada.setSedeId(sedeDestinoId);
            movEntrada.setUsuarioId(usuarioId);
            movEntrada.setLoteId(loteDestino.getId());
            movEntrada.setCantidad(cantidad);
            movEntrada.setObservacion("Recepción desde sede: " + loteOrigen.getSedeId());
            movimientoDAO.registrarMovimiento(conn, movEntrada);

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally {
            close(conn);
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
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
