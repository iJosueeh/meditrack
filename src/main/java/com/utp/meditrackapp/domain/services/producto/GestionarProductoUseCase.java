package com.utp.meditrackapp.domain.services.producto;

import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.ports.out.CategoriaRepository;
import com.utp.meditrackapp.domain.ports.out.ProductoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: Gestionar productos (CRUD + validaciones).
 */
public class GestionarProductoUseCase {
    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public GestionarProductoUseCase(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> buscarPorId(String id) {
        return productoRepository.findById(id);
    }

    public String guardar(Producto producto) {
        String validation = producto.validate();
        if (validation != null) return validation;

        if (productoRepository.existeCodigoDigemid(producto.getCodigoDigemid())) {
            return "Ya existe un producto con ese código DIGEMID.";
        }

        try {
            productoRepository.save(producto);
            return "OK";
        } catch (Exception e) {
            return "Error al guardar producto: " + e.getMessage();
        }
    }

    public String actualizar(Producto producto) {
        String validation = producto.validate();
        if (validation != null) return validation;

        try {
            productoRepository.update(producto);
            return "OK";
        } catch (Exception e) {
            return "Error al actualizar producto: " + e.getMessage();
        }
    }

    public String desactivar(String id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isEmpty()) return "Producto no encontrado.";

        try {
            productoRepository.desactivar(id);
            return "OK";
        } catch (Exception e) {
            return "Error al desactivar producto: " + e.getMessage();
        }
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }
}
