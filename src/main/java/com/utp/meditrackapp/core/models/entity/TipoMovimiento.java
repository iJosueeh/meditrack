package com.utp.meditrackapp.core.models.entity;

public class TipoMovimiento {
    private String id;
    private String nombre;

    public TipoMovimiento() {}

    public TipoMovimiento(String id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
