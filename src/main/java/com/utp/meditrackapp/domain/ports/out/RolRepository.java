package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Rol;
import java.util.List;
import java.util.Optional;

public interface RolRepository {
    Optional<Rol> findById(String id);
    List<Rol> findAll();
    Rol save(Rol rol);
    void update(Rol rol);
    void toggleEstado(String id);
    void delete(String id);
    int countUsersByRole(String roleId);
}
