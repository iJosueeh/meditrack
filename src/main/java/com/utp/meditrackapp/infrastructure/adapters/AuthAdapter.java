package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.services.auth.AutenticarUsuarioUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcUsuarioRepository;

/**
 * Adaptador que puentea el AuthService antiguo con el nuevo AutenticarUsuarioUseCase.
 */
public class AuthAdapter {
    private final AutenticarUsuarioUseCase useCase;
    private final SessionManager sessionManager;

    public AuthAdapter() {
        this.useCase = new AutenticarUsuarioUseCase(new JdbcUsuarioRepository());
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Autentica un usuario y lo guarda en la sesión.
     *
     * @param dni      Número de documento
     * @param password Contraseña en texto plano
     * @return true si la autenticación fue exitosa
     */
    public boolean authenticate(String dni, String password) {
        String validation = useCase.validarCredenciales(dni, password);
        if (validation != null) {
            return false;
        }

        Usuario usuario = useCase.autenticar(dni, password);
        if (usuario != null) {
            sessionManager.login(usuario);
            return true;
        }
        return false;
    }

    public void logout() {
        sessionManager.logout();
    }
}
