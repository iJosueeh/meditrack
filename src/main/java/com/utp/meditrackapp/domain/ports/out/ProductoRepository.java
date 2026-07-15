package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    Optional<Producto> findById(String id);
    List<Producto> findAll();
    List<Producto> findActivos();
    Producto save(Producto producto);
    Producto update(Producto producto);
    void desactivar(String id);
    void eliminar(String id);
    boolean existeCodigoDigemid(String codigoDigemid);
}
