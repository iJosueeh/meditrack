package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import java.util.List;
import java.util.Optional;

public interface TipoMovimientoRepository {
    Optional<TipoMovimiento> findById(String id);
    Optional<TipoMovimiento> findByNombre(String nombre);
    List<TipoMovimiento> findAll();
    TipoMovimiento save(TipoMovimiento tipo);
    void update(TipoMovimiento tipo);
    void delete(String id);
}
