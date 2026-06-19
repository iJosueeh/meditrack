package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio Rol.
 */
public class Rol {
    private String id;
    private String nombre;
    private int isActivo;

    public Rol() {
    }

    public Rol(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // === Comportamiento de Dominio ===

    public boolean isAdmin() {
        return nombre != null && nombre.toUpperCase().contains("ADMIN");
    }

    public boolean isJefeSede() {
        return nombre != null && nombre.toUpperCase().contains("JEFE");
    }

    public boolean isTecnico() {
        return nombre != null && nombre.toUpperCase().contains("TECNICO");
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }
}
