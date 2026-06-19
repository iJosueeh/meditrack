package com.utp.meditrackapp.domain.services.auth;

import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.ports.out.UsuarioRepository;

/**
 * Caso de uso: Autenticación de usuarios.
 * Centraliza la lógica que estaba en AuthService.
 */
public class AutenticarUsuarioUseCase {
    private final UsuarioRepository usuarioRepository;

    public AutenticarUsuarioUseCase(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Autentica un usuario con número de documento y contraseña.
     *
     * @param numeroDocumento Número de documento del usuario
     * @param password        Contraseña en texto plano
     * @return El usuario autenticado si es exitoso, null si falla
     */
    public Usuario autenticar(String numeroDocumento, String password) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return null;
        }
        if (password == null || password.trim().isEmpty()) {
            return null;
        }

        return usuarioRepository.authenticate(numeroDocumento.trim(), password);
    }

    /**
     * Valida que las credenciales no estén vacías.
     *
     * @return null si son válidas, mensaje de error si no lo son
     */
    public String validarCredenciales(String numeroDocumento, String password) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return "El número de documento es obligatorio.";
        }
        if (password == null || password.trim().isEmpty()) {
            return "La contraseña es obligatoria.";
        }
        return null;
    }
}
