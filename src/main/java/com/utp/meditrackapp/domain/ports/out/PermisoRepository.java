package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Permiso;
import java.util.List;

/**
 * Puerto de salida para operaciones de permisos.
 */
public interface PermisoRepository {
    List<Permiso> findAll();
    List<Permiso> findByRolId(String rolId);
    List<Permiso> findByModulo(String modulo);
    void saveRolPermisos(String rolId, List<String> permisoIds);
    void deleteRolPermisos(String rolId);
    int countPermisosByRol(String rolId);
}
