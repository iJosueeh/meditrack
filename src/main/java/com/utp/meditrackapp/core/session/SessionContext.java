package com.utp.meditrackapp.core.session;

import java.util.Optional;

public final class SessionContext {
    private static final ThreadLocal<String> USUARIO_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> SEDE_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> ROL_ID = new ThreadLocal<>();

    private SessionContext() {
    }

    public static void setUsuarioId(String usuarioId) {
        setThreadLocalValue(USUARIO_ID, usuarioId);
    }

    public static void setSedeId(String sedeId) {
        setThreadLocalValue(SEDE_ID, sedeId);
    }

    public static void setRolId(String rolId) {
        setThreadLocalValue(ROL_ID, rolId);
    }

    public static Optional<String> getUsuarioId() {
        return Optional.ofNullable(USUARIO_ID.get());
    }

    public static Optional<String> getSedeId() {
        return Optional.ofNullable(SEDE_ID.get());
    }

    public static Optional<String> getRolId() {
        return Optional.ofNullable(ROL_ID.get());
    }

    public static String requireSedeId() {
        return getSedeId().orElseThrow(() -> new IllegalStateException("No hay una sede activa en la sesion."));
    }

    public static void clear() {
        USUARIO_ID.remove();
        SEDE_ID.remove();
        ROL_ID.remove();
    }

    private static void setThreadLocalValue(ThreadLocal<String> threadLocal, String value) {
        if (value == null || value.isBlank()) {
            threadLocal.remove();
        } else {
            threadLocal.set(value.trim());
        }
    }
}