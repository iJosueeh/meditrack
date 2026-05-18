package com.utp.meditrackapp.core.service;

import com.utp.meditrackapp.core.dao.CategoriaDAO;
import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.ProductoDAO;
import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.models.entity.Categoria;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.models.entity.Producto;

import java.sql.SQLException;
import java.util.List;

public class InventarioService {
    private final CategoriaDAO categoriaDAO;
    private final ProductoDAO productoDAO;
    private final LoteDAO loteDAO;

    public InventarioService() {
        this(new CategoriaDAO(), new ProductoDAO(), new LoteDAO());
    }

    public InventarioService(CategoriaDAO categoriaDAO, ProductoDAO productoDAO, LoteDAO loteDAO) {
        this.categoriaDAO = categoriaDAO;
        this.productoDAO = productoDAO;
        this.loteDAO = loteDAO;
    }

    public Categoria registrarCategoria(Categoria categoria) throws SQLException {
        return categoriaDAO.crear(categoria);
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.listarTodas();
    }

    public Producto registrarProducto(Producto producto) throws SQLException {
        return productoDAO.crear(producto);
    }

    public Producto actualizarProducto(Producto producto) throws SQLException {
        return productoDAO.actualizar(producto);
    }

    public List<Producto> listarProductosActivos() throws SQLException {
        return productoDAO.listarActivos();
    }

    public Lote registrarLote(Lote lote) throws SQLException {
        return loteDAO.registrarIngreso(lote);
    }

    public List<Lote> listarLotesFefo(String sedeId, String productoId) throws SQLException {
        return loteDAO.listarLotesFefo(sedeId, productoId);
    }

    public List<StockCriticoItem> obtenerStockCritico(String sedeId) throws SQLException {
        return loteDAO.obtenerStockCritico(sedeId);
    }

    public List<StockCriticoItem> obtenerTopStockBajo(String sedeId, int limite) throws SQLException {
        return loteDAO.obtenerTopStockBajo(sedeId, limite);
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
}