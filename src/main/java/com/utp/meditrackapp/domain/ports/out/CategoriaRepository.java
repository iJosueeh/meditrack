package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    Optional<Categoria> findById(String id);
    List<Categoria> findAll();
    Categoria save(Categoria categoria);
    Categoria update(Categoria categoria);
    void toggleEstado(String id);
    void delete(String id);
    int countProductosByCategoria(String categoriaId);
}
