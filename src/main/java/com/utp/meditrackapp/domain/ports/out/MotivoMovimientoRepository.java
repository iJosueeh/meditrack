package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import java.util.List;
import java.util.Optional;

public interface MotivoMovimientoRepository {
    Optional<MotivoMovimiento> findById(String id);
    List<MotivoMovimiento> findAll();
    MotivoMovimiento save(MotivoMovimiento motivo);
    void update(MotivoMovimiento motivo);
    void delete(String id);
}
