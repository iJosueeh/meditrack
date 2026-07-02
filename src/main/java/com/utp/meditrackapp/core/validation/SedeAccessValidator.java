package com.utp.meditrackapp.core.validation;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Usuario;

/**
 * Validador centralizado de acceso por sede.
 * Previene que usuarios accedan a datos fuera de su sede.
 */
public class SedeAccessValidator {

    /**
     * Excepción lanzada cuando se intenta acceder a datos de otra sede.
     */
    public static class AccesoSedeDenegadoException extends RuntimeException {
        public AccesoSedeDenegadoException(String mensaje) {
            super(mensaje);
        }
    }

    /**
     * Excepción lanzada cuando la sede está bloqueada.
     */
    public static class SedeBloqueadaException extends RuntimeException {
        public SedeBloqueadaException(String mensaje) {
            super(mensaje);
        }
    }

    private SedeAccessValidator() {
        // Utility class
    }

    /**
     * Obtiene la sede ID del usuario actual desde SessionManager.
     * @return sede_id del usuario logueado, o null si no hay sesión
     */
    public static String getSedeActual() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        return user != null ? user.getSedeId() : null;
    }

    /**
     * Verifica si el usuario actual es Administrador (acceso global).
     */
    public static boolean isAdmin() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        return user != null && user.isAdmin();
    }

    /**
     * Valida que la sede del usuario actual no esté bloqueada.
     * Admin tiene acceso global (no valida bloqueo).
     * 
     * @throws SedeBloqueadaException si la sede está bloqueada
     */
    public static void validarSedeActiva() {
        if (isAdmin()) return; // Admin tiene acceso global
        
        SessionManager session = SessionManager.getInstance();
        if (session.isSedeBloqueada()) {
            String motivo = session.getMotivoBloqueoSede();
            String mensaje = "Su sede está bloqueada y no puede realizar esta operación.";
            if (motivo != null && !motivo.isEmpty()) {
                mensaje += " Motivo: " + motivo;
            }
            throw new SedeBloqueadaException(mensaje);
        }
    }

    /**
     * Valida que un recurso pertenezca a la sede del usuario actual.
     * Si es Admin, tiene acceso global (no valida).
     * 
     * @param recursoSedeId la sede_id del recurso a validar
     * @param recursoNombre nombre del recurso para el mensaje de error
     * @throws AccesoSedeDenegadoException si el recurso no pertenece a la sede del usuario
     */
    public static void validarAcceso(String recursoSedeId, String recursoNombre) {
        if (isAdmin()) return; // Admin tiene acceso global

        String sedeActual = getSedeActual();
        if (sedeActual == null) {
            throw new AccesoSedeDenegadoException("No hay sesión activa.");
        }
        if (recursoSedeId == null) {
            throw new AccesoSedeDenegadoException(recursoNombre + " no tiene sede asignada.");
        }
        if (!sedeActual.equals(recursoSedeId)) {
            throw new AccesoSedeDenegadoException(
                "Acceso denegado: " + recursoNombre + " no pertenece a su sede."
            );
        }
    }

    /**
     * Retorna la sede_id del usuario actual para usar en consultas SQL.
     * Admin puede recibir null (para queries globales) o una sede específica.
     * 
     * @return sede_id del usuario actual, o null si es Admin
     */
    public static String getSedeParaConsulta() {
        if (isAdmin()) {
            return null; // Admin puede ver todo
        }
        return getSedeActual();
    }

    /**
     * Retorna la sede_id del usuario actual para forzar filtro.
     * Incluso Admin se filtra por su sede asignada.
     * 
     * @return sede_id del usuario actual
     * @throws AccesoSedeDenegadoException si no hay sesión activa
     */
    public static String getSedeForzada() {
        String sede = getSedeActual();
        if (sede == null) {
            throw new AccesoSedeDenegadoException("No hay sesión activa.");
        }
        return sede;
    }
}
