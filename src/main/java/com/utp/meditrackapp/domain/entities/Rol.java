package com.utp.meditrackapp.domain.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad de dominio Rol con soporte RBAC y jerarquía.
 */
public class Rol {
    private String id;
    private String nombre;
    private String descripcion;
    private int nivel;
    private int isSistema;
    private int isActivo;
    private List<Permiso> permisos;

    public Rol() {
        this.permisos = new ArrayList<>();
    }

    public Rol(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.permisos = new ArrayList<>();
    }

    // === Comportamiento de Dominio ===

    /**
     * Verifica si el rol tiene un permiso específico por código.
     */
    public boolean tienePermiso(String codigo) {
        if (permisos == null || permisos.isEmpty()) return false;
        return permisos.stream()
            .anyMatch(p -> p.getCodigo().equals(codigo) && p.getIsActivo() == 1);
    }

    /**
     * Verifica si este rol puede gestionar otro rol (debe tener menor nivel/jerarquía).
     */
    public boolean puedeGestionarRol(Rol otroRol) {
        if (otroRol == null) return false;
        return this.nivel < otroRol.nivel;
    }

    /**
     * Verifica si es un rol del sistema (no eliminable).
     */
    public boolean esSistema() {
        return isSistema == 1;
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }

    public int getIsSistema() { return isSistema; }
    public void setIsSistema(int isSistema) { this.isSistema = isSistema; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }

    public List<Permiso> getPermisos() { return permisos; }
    public void setPermisos(List<Permiso> permisos) { this.permisos = permisos; }

    // Métodos de compatibilidad con código existente (serán removidos gradualmente)

    public boolean isAdmin() {
        return id != null && id.equals("ROL-001");
    }

    public boolean isJefeSede() {
        return nombre != null && nombre.toUpperCase().contains("JEFE");
    }

    public boolean isTecnico() {
        return id != null && id.equals("ROL-003");
    }
}
