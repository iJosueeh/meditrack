package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de Sedes.
 */
public interface SedeRepository {

    Optional<Sede> findById(String id);

    List<Sede> findAll();

    List<Sede> findActivas();

    Sede save(Sede sede);

    Sede update(Sede sede);

    void toggleEstado(String id);

    void bloquear(String id, String motivo);

    void desbloquear(String id);

    boolean isBloqueada(String id);

    List<Usuario> findStaffBySede(String sedeId);

    boolean assignUserToSede(String userId, String sedeId, String rolId);

    int countTotalEmployees();

    int countEmployeesBySede(String sedeId);

    int countUsuariosBySede(String sedeId);

    int countLotesBySede(String sedeId);

    int countMovimientosBySede(String sedeId);

    int countAtencionesBySede(String sedeId);

    void delete(String id);
}
