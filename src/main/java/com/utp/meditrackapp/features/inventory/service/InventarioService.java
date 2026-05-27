package com.utp.meditrackapp.features.inventory.service;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.dao.CategoriaDAO;
import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.MovimientoDAO;
import com.utp.meditrackapp.core.dao.ProductoDAO;
import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.models.entity.Categoria;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.models.entity.Movimiento;
import com.utp.meditrackapp.core.models.entity.Producto;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.core.service.InventoryHealthCalculator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class InventarioService {
    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO;
    private final LoteDAO loteDAO;
    private final MovimientoDAO movimientoDAO;
    private final DatabaseConfig dbConfig;

    public InventarioService() {
        this.categoriaDAO = new CategoriaDAO();
        this.productoDAO = new ProductoDAO();
        this.loteDAO = new LoteDAO();
        this.movimientoDAO = new MovimientoDAO();
        this.dbConfig = DatabaseConfig.getInstance();
    }

    // --- Métodos de Consulta ---

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listarTodas();
    }

    public List<Producto> listarProductosActivos() throws SQLException {
        return productoDAO.listarActivos();
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

    // --- Métodos Operacionales (Transaccionales) ---

    public boolean registrarEntrada(Lote lote, String usuarioId, String observacion) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            if (lote.getId() == null || lote.getId().isEmpty()) {
                loteDAO.registrarIngreso(conn, lote);
            } else {
                loteDAO.aumentarStock(conn, lote.getId(), lote.getCantidad());
            }

            registrarMovimientoInterno(conn, lote, usuarioId, 
                    TipoMovimientoEnum.ENTRADA, MotivoMovimientoEnum.COMPRA, lote.getCantidad(), observacion);

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

    public boolean registrarMerma(String loteId, int cantidad, String usuarioId, String observacion) {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            Optional<Lote> loteOpt = loteDAO.buscarPorId(loteId);
            if (loteOpt.isEmpty()) throw new SQLException("Lote no encontrado.");
            Lote lote = loteOpt.get();

            loteDAO.reducirStock(conn, loteId, cantidad);

            registrarMovimientoInterno(conn, lote, usuarioId, 
                    TipoMovimientoEnum.SALIDA, MotivoMovimientoEnum.MERMA, cantidad, observacion);

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

    private void registrarMovimientoInterno(Connection conn, Lote lote, String usuarioId, 
                                          TipoMovimientoEnum tipo, MotivoMovimientoEnum motivo, 
                                          int cantidad, String obs) throws SQLException {
        Movimiento mov = new Movimiento();
        mov.setTipoId(tipo.getId());
        mov.setMotivoId(motivo.getId());
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
