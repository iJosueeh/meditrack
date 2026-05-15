package com.utp.meditrackapp.core.models.entity;

public class Sede {
    private String id;
    private String nombre;
    private String direccion;
    private int isActiva;

    public Sede() {}

    public Sede(String id, String nombre, String direccion, int isActiva) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.isActiva = isActiva;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public int getIsActiva() { return isActiva; }
    public void setIsActiva(int isActiva) { this.isActiva = isActiva; }
}
