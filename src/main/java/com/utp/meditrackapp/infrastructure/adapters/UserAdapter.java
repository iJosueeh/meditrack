package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.services.usuario.GestionarUsuarioUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcRolRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcSedeRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcUsuarioRepository;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador para UsuarioController.
 * Delega al GestionarUsuarioUseCase del dominio.
 */
public class UserAdapter {
    private final GestionarUsuarioUseCase useCase;

    public UserAdapter() {
        this.useCase = new GestionarUsuarioUseCase(
            new JdbcUsuarioRepository(),
            new JdbcRolRepository(),
            new JdbcSedeRepository()
        );
    }

    public List<Usuario> listarUsuarios() {
        return useCase.listarUsuarios();
    }

    public List<Usuario> listarUsuariosPorSede(String sedeId) {
        return useCase.listarUsuariosPorSede(sedeId);
    }

    public Optional<Usuario> buscarPorId(String id) {
        return useCase.buscarPorId(id);
    }

    public String guardarUsuario(Usuario usuario, String rawPassword) {
        return useCase.guardar(usuario, rawPassword);
    }

    public String actualizarUsuario(Usuario usuario) {
        return useCase.actualizar(usuario);
    }

    public String toggleEstado(String id) {
        return useCase.toggleEstado(id);
    }

    public String actualizarPassword(String usuarioId, String hashedPassword) {
        return useCase.actualizarPassword(usuarioId, hashedPassword);
    }

    public List<Rol> listarRoles() {
        return useCase.listarRoles();
    }

    public List<Sede> listarSedes() {
        return useCase.listarSedes();
    }
}
