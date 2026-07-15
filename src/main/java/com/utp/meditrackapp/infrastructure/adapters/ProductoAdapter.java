package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.core.cache.ReferenceCacheManager;
import com.utp.meditrackapp.core.cache.ReferenceCacheManager.CacheType;
import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.services.inventario.CalcularStockUseCase;
import com.utp.meditrackapp.domain.services.producto.GestionarProductoUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcCategoriaRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcLoteRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcProductoRepository;

import java.util.List;
import java.util.Map;

public class ProductoAdapter {
    private final GestionarProductoUseCase useCase;
    private final CalcularStockUseCase stockUseCase;
    private final ReferenceCacheManager cache = ReferenceCacheManager.getInstance();

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
        String r = useCase.guardar(producto);
        if ("OK".equals(r)) cache.invalidate(CacheType.PRODUCTOS, CacheType.CATEGORIAS);
        return r;
    }

    public String actualizarProducto(Producto producto) {
        String r = useCase.actualizar(producto);
        cache.invalidate(CacheType.PRODUCTOS, CacheType.CATEGORIAS);
        return r;
    }

    public String desactivarProducto(String id) {
        String r = useCase.desactivar(id);
        if ("OK".equals(r)) cache.invalidate(CacheType.PRODUCTOS);
        return r;
    }

    public String eliminarProducto(String id) {
        String r = useCase.eliminar(id);
        if ("OK".equals(r)) cache.invalidate(CacheType.PRODUCTOS);
        return r;
    }

    public List<Categoria> listarCategorias() {
        return cache.get(CacheType.CATEGORIAS, () -> useCase.listarCategorias());
    }

    public int obtenerStockTotal(String sedeId, String productoId) {
        return stockUseCase.obtenerStockTotal(sedeId, productoId);
    }

    public boolean productoTieneLotes(String productoId) {
        return stockUseCase.tieneLotes(productoId);
    }

    public Map<String, Integer> obtenerStockTotalPorSede(String sedeId) {
        return stockUseCase.obtenerStockPorSede(sedeId);
    }
}
