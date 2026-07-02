package com.utp.meditrackapp.domain.services.sede;

import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.ports.out.SedeRepository;
import com.utp.meditrackapp.domain.ports.out.UsuarioRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcRolRepository;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: Gestionar sedes (CRUD + validaciones).
 * Centraliza la lógica que estaba en SedeController y SedeDAO.
 */
public class GestionarSedeUseCase {
    private final SedeRepository sedeRepository;
    private final UsuarioRepository usuarioRepository;

    public GestionarSedeUseCase(SedeRepository sedeRepository, UsuarioRepository usuarioRepository) {
        this.sedeRepository = sedeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Lista todas las sedes registradas.
     */
    public List<Sede> listarSedes() {
        return sedeRepository.findAll();
    }

    /**
     * Lista solo las sedes activas.
     */
    public List<Sede> listarSedesActivas() {
        return sedeRepository.findActivas();
    }

    /**
     * Obtiene una sede por ID.
     */
    public Optional<Sede> obtenerPorId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }
        return sedeRepository.findById(id);
    }

    /**
     * Guarda o actualiza una sede aplicando validaciones de dominio.
     *
     * @param sede La sede a guardar
     * @return "OK" si fue exitoso, o un mensaje de error
     */
    public String guardarSede(Sede sede) {
        String validation = sede.validate();
        if (validation != null) {
            return validation;
        }

        try {
            if (sede.getId() == null || sede.getId().trim().isEmpty()) {
                sedeRepository.save(sede);
            } else {
                sedeRepository.update(sede);
            }
            return "OK";
        } catch (Exception e) {
            return "Error técnico al guardar la sede: " + e.getMessage();
        }
    }

    /**
     * Activa o desactiva una sede (toggle de estado).
     *
     * @param id ID de la sede
     * @return "OK" si fue exitoso, o un mensaje de error
     */
    public String toggleEstado(String id) {
        if (id == null || id.trim().isEmpty()) {
            return "ID de sede no válido.";
        }

        Optional<Sede> sedeOpt = sedeRepository.findById(id);
        if (sedeOpt.isEmpty()) {
            return "No se encontró la sede con ID: " + id;
        }

        try {
            sedeRepository.toggleEstado(id);
            return "OK";
        } catch (Exception e) {
            return "Error técnico al cambiar estado: " + e.getMessage();
        }
    }

    /**
     * Asigna un usuario como jefe de una sede.
     *
     * @param usuarioId ID del usuario
     * @param sedeId    ID de la sede
     * @param rolId     ID del rol a asignar
     * @return "OK" si fue exitoso, o un mensaje de error
     */
    public String asignarUsuarioASede(String usuarioId, String sedeId, String rolId) {
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            return "El usuario es obligatorio.";
        }
        if (sedeId == null || sedeId.trim().isEmpty()) {
            return "La sede es obligatoria.";
        }
        if (rolId == null || rolId.trim().isEmpty()) {
            return "El rol es obligatorio.";
        }

        try {
            boolean success = sedeRepository.assignUserToSede(usuarioId, sedeId, rolId);
            return success ? "OK" : "Error al asignar usuario a la sede.";
        } catch (Exception e) {
            return "Error técnico al asignar usuario: " + e.getMessage();
        }
    }

    /**
     * Obtiene el personal asignado a una sede.
     */
    public List<Usuario> obtenerStaffPorSede(String sedeId) {
        return sedeRepository.findStaffBySede(sedeId);
    }

    /**
     * Obtiene el total de empleados registrados en el sistema.
     */
    public int contarEmpleadosGlobales() {
        return sedeRepository.countTotalEmployees();
    }

    /**
     * Obtiene usuarios admin/jefe disponibles para asignar como administradores de sede.
     * Verifica permisos M2_SEDES (admin) o M5_ENTRADAS (jefe de sede) en el rol del usuario.
     */
    public List<Usuario> obtenerAdministradoresDisponibles() {
        var rolRepo = new JdbcRolRepository();
        return usuarioRepository.findAll().stream()
                .filter(u -> {
                    if (!u.isActivo()) return false;
                    var rol = rolRepo.findById(u.getRolId()).orElse(null);
                    if (rol == null) return false;
                    return rol.tienePermiso("M2_SEDES") || rol.tienePermiso("M5_ENTRADAS");
                })
                .toList();
    }

    public String eliminarSede(String id) {
        int usuarios = sedeRepository.countUsuariosBySede(id);
        int lotes = sedeRepository.countLotesBySede(id);
        int movimientos = sedeRepository.countMovimientosBySede(id);
        int atenciones = sedeRepository.countAtencionesBySede(id);
        if (usuarios > 0 || lotes > 0 || movimientos > 0 || atenciones > 0) {
            return "NO_HISTORY";
        }
        try {
            sedeRepository.delete(id);
            return "OK";
        } catch (Exception e) {
            return "Error al eliminar sede: " + e.getMessage();
        }
    }
}
