package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.services.usuario.GestionarUsuarioUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcRolRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcSedeRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcUsuarioRepository;

import java.util.Optional;

/**
 * Adaptador para ProfileController.
 * Delega al GestionarUsuarioUseCase del dominio.
 */
public class ProfileAdapter {
    private final GestionarUsuarioUseCase useCase;

    public ProfileAdapter() {
        this.useCase = new GestionarUsuarioUseCase(
            new JdbcUsuarioRepository(),
            new JdbcRolRepository(),
            new JdbcSedeRepository()
        );
    }

    public Optional<Usuario> buscarPorId(String id) {
        return useCase.buscarPorId(id);
    }

    public String actualizarUsuario(Usuario usuario) {
        return useCase.actualizar(usuario);
    }

    public String actualizarPassword(String usuarioId, String hashedPassword) {
        return useCase.actualizarPassword(usuarioId, hashedPassword);
    }
}
