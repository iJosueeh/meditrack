package com.utp.meditrackapp.core.models.entity;

public class Sede {
    private String id;
    private String nombre;
    private String direccion;
    private boolean activa = true;

    public Sede() {
    }

    public Sede(String id, String nombre, String direccion, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.activa = activa;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}