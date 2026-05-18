package com.utp.meditrackapp.features.auth.service;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.features.auth.Dao.UsuarioDao;

public class AuthService {
    
    private final UsuarioDao usuarioDao;

    public AuthService() {
        this.usuarioDao = new UsuarioDao();
    }

    /**
     * Intenta autenticar a un usuario.
     * @param dni Documento de identidad.
     * @param password Contraseña plana.
     * @return true si la autenticación es exitosa, false de lo contrario.
     */
    public boolean authenticate(String dni, String password) {
        if (dni == null || dni.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        Usuario usuario = usuarioDao.login(dni, password);
        
        if (usuario != null && usuario.getIsActivo() == 1) {
            SessionManager.getInstance().login(usuario);
            return true;
        }
        
        return false;
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
