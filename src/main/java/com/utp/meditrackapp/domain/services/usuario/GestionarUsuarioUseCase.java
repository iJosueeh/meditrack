package com.utp.meditrackapp.domain.services.usuario;

import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.ports.out.RolRepository;
import com.utp.meditrackapp.domain.ports.out.SedeRepository;
import com.utp.meditrackapp.domain.ports.out.UsuarioRepository;

import java.util.List;
import java.util.Optional;

/**
 * Caso de uso: Gestionar usuarios (CRUD + validaciones).
 */
public class GestionarUsuarioUseCase {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SedeRepository sedeRepository;

    public GestionarUsuarioUseCase(UsuarioRepository usuarioRepository, RolRepository rolRepository, SedeRepository sedeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.sedeRepository = sedeRepository;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarUsuariosPorSede(String sedeId) {
        return usuarioRepository.findAllBySedeId(sedeId);
    }

    public Optional<Usuario> buscarPorId(String id) {
        return usuarioRepository.findById(id);
    }

    public String guardar(Usuario usuario, String rawPassword) {
        String validation = usuario.validate();
        if (validation != null) return validation;

        if (usuario.getNumeroDocumento() != null && usuarioRepository.findByDocumento(usuario.getNumeroDocumento()) != null) {
            return "Ya existe un usuario con ese número de documento.";
        }

        try {
            usuarioRepository.save(usuario, rawPassword);
            return "OK";
        } catch (Exception e) {
            return "Error al guardar usuario: " + e.getMessage();
        }
    }

    public String actualizar(Usuario usuario) {
        String validation = usuario.validate();
        if (validation != null) return validation;

        try {
            usuarioRepository.update(usuario);
            return "OK";
        } catch (Exception e) {
            return "Error al actualizar usuario: " + e.getMessage();
        }
    }

    public String toggleEstado(String id) {
        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return "Usuario no encontrado.";
        try {
            int nuevoEstado = opt.get().getIsActivo() == 1 ? 0 : 1;
            usuarioRepository.toggleEstado(id, nuevoEstado);
            return "OK";
        } catch (Exception e) {
            return "Error al cambiar estado: " + e.getMessage();
        }
    }

    public String actualizarPassword(String usuarioId, String hashedPassword) {
        try {
            boolean ok = usuarioRepository.updatePassword(usuarioId, hashedPassword);
            return ok ? "OK" : "No se pudo actualizar la contraseña.";
        } catch (Exception e) {
            return "Error al actualizar contraseña: " + e.getMessage();
        }
    }

    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    public List<Sede> listarSedes() {
        return sedeRepository.findAll();
    }
}
