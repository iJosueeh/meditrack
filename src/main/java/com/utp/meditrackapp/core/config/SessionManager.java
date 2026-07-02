package com.utp.meditrackapp.core.config;

import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcRolRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcSedeRepository;

public class SessionManager {
    private static final SessionManager instance = new SessionManager();
    private volatile Usuario currentUser;
    private final JdbcRolRepository rolRepository;
    private final JdbcSedeRepository sedeRepository;

    private SessionManager() {
        this.rolRepository = new JdbcRolRepository();
        this.sedeRepository = new JdbcSedeRepository();
    }
    
    public static SessionManager getInstance() {
        return instance;
    }

    public void login(Usuario user){
        this.currentUser = user;
    }

    public void logout(){
        this.currentUser = null;
    }

    public Usuario getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // === Métodos de validación de sede bloqueada ===

    /**
     * Verifica si la sede del usuario actual está bloqueada.
     * @return true si la sede está bloqueada, false si no lo está o no hay sesión
     */
    public boolean isSedeBloqueada() {
        Usuario user = getCurrentUser();
        if (user == null || user.getSedeId() == null) return false;
        
        // Admin tiene acceso global, no se valida bloqueo
        if (tienePermiso("M2_SEDES")) return false;
        
        return sedeRepository.isBloqueada(user.getSedeId());
    }

    /**
     * Obtiene el motivo del bloqueo de la sede del usuario actual.
     * @return motivo del bloqueo, o null si no está bloqueada
     */
    public String getMotivoBloqueoSede() {
        Usuario user = getCurrentUser();
        if (user == null || user.getSedeId() == null) return null;
        
        var sede = sedeRepository.findById(user.getSedeId()).orElse(null);
        return sede != null ? sede.getMotivoBloqueo() : null;
    }

    /**
     * Valida que la sede del usuario actual no esté bloqueada.
     * Lanza una excepción si está bloqueada.
     * @throws IllegalStateException si la sede está bloqueada
     */
    public void validarSedeNoBloqueada() {
        if (isSedeBloqueada()) {
            String motivo = getMotivoBloqueoSede();
            String mensaje = "Su sede está bloqueada y no puede realizar esta operación.";
            if (motivo != null && !motivo.isEmpty()) {
                mensaje += " Motivo: " + motivo;
            }
            throw new IllegalStateException(mensaje);
        }
    }

    // === Métodos de permisos dinámicos (RBAC) ===

    /**
     * Verifica si el usuario actual tiene un permiso específico por código.
     * Usa los permisos asignados al rol en la BD (sin bypass por rol).
     */
    public boolean tienePermiso(String codigoPermiso) {
        Usuario user = getCurrentUser();
        if (user == null) return false;
        
        Rol rol = getRolUsuario();
        return rol != null && rol.tienePermiso(codigoPermiso);
    }

    /**
     * Obtiene el rol del usuario actual con sus permisos cargados.
     */
    public Rol getRolUsuario() {
        Usuario user = getCurrentUser();
        if (user == null || user.getRolId() == null) return null;
        
        try {
            return rolRepository.findById(user.getRolId()).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Obtiene el nivel de jerarquía del usuario actual.
     * Menor número = Mayor jerarquía (1 = Super Admin)
     */
    public int getNivelUsuario() {
        Rol rol = getRolUsuario();
        return rol != null ? rol.getNivel() : Integer.MAX_VALUE;
    }

    /**
     * Verifica si el usuario actual puede gestionar un rol específico.
     * Solo puede gestionar roles de menor jerarquía (nivel mayor).
     */
    public boolean puedeGestionarRol(String rolId) {
        Usuario user = getCurrentUser();
        if (user == null) return false;
        
        Rol rolActual = getRolUsuario();
        if (rolActual == null) return false;
        
        Rol rolTarget = rolRepository.findById(rolId).orElse(null);
        if (rolTarget == null) return false;
        
        return rolActual.puedeGestionarRol(rolTarget);
    }

    /**
     * Verifica si el usuario actual puede asignar un rol específico a otro usuario.
     */
    public boolean puedeAsignarRol(String rolId) {
        return puedeGestionarRol(rolId);
    }
}
