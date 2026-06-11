package com.utp.meditrackapp.features.inventory.service;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.dao.CategoriaDAO;
import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.MovimientoDAO;
import com.utp.meditrackapp.core.dao.ProductoDAO;
import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.dao.TipoMovimientoDAO;
import com.utp.meditrackapp.core.models.entity.*;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.core.service.InventoryHealthCalculator;
import com.utp.meditrackapp.features.sedes.dao.SedeDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class InventarioService {
    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO;
    private final LoteDAO loteDAO;
    private final MovimientoDAO movimientoDAO;
    private final TipoMovimientoDAO tipoMovimientoDAO;
    private final SedeDAO sedeDAO;
    private final DatabaseConfig dbConfig;

    public InventarioService() {
        this.categoriaDAO = new CategoriaDAO();
        this.productoDAO = new ProductoDAO();
        this.loteDAO = new LoteDAO();
        this.movimientoDAO = new MovimientoDAO();
        this.tipoMovimientoDAO = new TipoMovimientoDAO();
        this.sedeDAO = new SedeDAO();
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // --- Métodos de Consulta ---

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listarTodas();
    }

    public List<Producto> listarProductosActivos() throws SQLException {
        return productoDAO.listarActivos();
    }

    public List<TipoMovimiento> listarTiposMovimiento() throws SQLException {
        return tipoMovimientoDAO.listarTodas();
    }

    public List<Movimiento> listarMovimientosConFiltros(String sedeId, String tipoId, String buscar, LocalDate desde, LocalDate hasta) throws SQLException {
        return movimientoDAO.listarConFiltros(sedeId, tipoId, buscar, desde, hasta);
    }

    public List<Lote> listarLotesConProducto(String sedeId) throws SQLException {
        return loteDAO.listarLotesConProducto(sedeId);
    }

    public List<Lote> listarLotesFefo(String sedeId, String productoId) throws SQLException {
        return loteDAO.listarLotesFefo(sedeId, productoId);
    }

    public List<Movimiento> listarMovimientos(String sedeId, String tipoId, String buscar) throws SQLException {
        return movimientoDAO.listarPorSede(sedeId, tipoId, buscar);
    }

    public List<StockCriticoItem> obtenerStockCritico(String sedeId) throws SQLException {
        return loteDAO.obtenerStockCritico(sedeId);
    }

    public int calcularSaludInventario(String sedeId) throws SQLException {
        List<StockCriticoItem> criticos = obtenerStockCritico(sedeId);
        int productosCriticos = 0;
        int lotesPorVencer30 = 0;
        int lotesPorVencer60 = 0;

        for (StockCriticoItem item : criticos) {
            if (item.isStockBajo()) {
                productosCriticos++;
            }
            if (item.getDiasParaVencer() >= 0 && item.getDiasParaVencer() <= 30) {
                lotesPorVencer30++;
            } else if (item.getDiasParaVencer() <= 60) {
                lotesPorVencer60++;
            }
        }

        return InventoryHealthCalculator.calcularSaludInventario(productosCriticos, lotesPorVencer30, lotesPorVencer60);
    }

    public List<MotivoMovimiento> listarMotivosMovimiento() throws SQLException {
        return new com.utp.meditrackapp.core.dao.MotivoMovimientoDAO().listarTodas();
    }

    // --- Métodos Operacionales (Transaccionales) ---

    public void registrarMovimiento(Lote lote, String usuarioId, String tipoId, String motivoId, int cantidad, String observacion) throws SQLException {
        Optional<Sede> sedeOpt = sedeDAO.buscarPorId(lote.getSedeId());
        if (sedeOpt.isEmpty() || sedeOpt.get().getIsActiva() == 0) {
            throw new SQLException("La sede se encuentra inactiva. No se pueden registrar movimientos.");
        }

        TipoMovimientoEnum tipoEnum = TipoMovimientoEnum.fromId(tipoId);
        if (tipoEnum == null) {
            throw new SQLException("Tipo de movimiento no válido: " + tipoId);
        }
        boolean isEntrada = tipoEnum == TipoMovimientoEnum.ENTRADA;

        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Actualizar o Registrar Lote
            if (isEntrada) {
                if (lote.getId() == null || lote.getId().isEmpty()) {
                    loteDAO.registrarIngreso(conn, lote);
                } else {
                    loteDAO.aumentarStock(conn, lote.getId(), cantidad);
                }
            } else { // SALIDA
                loteDAO.reducirStock(conn, lote.getId(), cantidad);
            }

            // 2. Registrar Movimiento
            registrarMovimientoInterno(conn, lote, usuarioId, tipoId, motivoId, cantidad, observacion);

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw e; 
        } finally {
            close(conn);
        }
    }

    private void registrarMovimientoInterno(Connection conn, Lote lote, String usuarioId, 
                                          String tipoId, String motivoId, 
                                          int cantidad, String obs) throws SQLException {
        Movimiento mov = new Movimiento();
        mov.setTipoId(tipoId);
        mov.setMotivoId(motivoId);
        mov.setSedeId(lote.getSedeId());
        mov.setUsuarioId(usuarioId);
        mov.setLoteId(lote.getId());
        mov.setCantidad(cantidad);
        mov.setObservacion(obs);
        movimientoDAO.registrarMovimiento(conn, mov);
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
