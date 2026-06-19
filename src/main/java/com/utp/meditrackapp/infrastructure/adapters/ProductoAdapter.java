package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.services.inventario.CalcularStockUseCase;
import com.utp.meditrackapp.domain.services.producto.GestionarProductoUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcCategoriaRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcLoteRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcProductoRepository;

import java.util.List;
import java.util.Map;

/**
 * Adaptador para ProductoController.
 * Delega al GestionarProductoUseCase del dominio.
 */
public class ProductoAdapter {
    private final GestionarProductoUseCase useCase;
    private final CalcularStockUseCase stockUseCase;

    public ProductoAdapter() {
        JdbcLoteRepository loteRepo = new JdbcLoteRepository();
        this.useCase = new GestionarProductoUseCase(
            new JdbcProductoRepository(),
            new JdbcCategoriaRepository()
        );
        this.stockUseCase = new CalcularStockUseCase(loteRepo);
    }

    public List<Producto> listarProductos() {
        return useCase.listarProductos();
    }

    public String guardarProducto(Producto producto) {
        return useCase.guardar(producto);
    }

    public String actualizarProducto(Producto producto) {
        return useCase.actualizar(producto);
    }

    public String desactivarProducto(String id) {
        return useCase.desactivar(id);
    }

    public List<Categoria> listarCategorias() {
        return useCase.listarCategorias();
    }

    public int obtenerStockTotal(String sedeId, String productoId) {
        return stockUseCase.obtenerStockTotal(sedeId, productoId);
    }

    public Map<String, Integer> obtenerStockTotalPorSede(String sedeId) {
        return stockUseCase.obtenerStockPorSede(sedeId);
    }
}
