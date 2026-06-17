package com.utp.meditrackapp.core.models.entity;

public class Sede {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String ubigeo;
    private String administradorId;
    private String tipoSede;
    private int capacidadAlmacen;
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

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getUbigeo() { return ubigeo; }
    public void setUbigeo(String ubigeo) { this.ubigeo = ubigeo; }

    public String getAdministradorId() { return administradorId; }
    public void setAdministradorId(String administradorId) { this.administradorId = administradorId; }

    public String getTipoSede() { return tipoSede; }
    public void setTipoSede(String tipoSede) { this.tipoSede = tipoSede; }

    public int getCapacidadAlmacen() { return capacidadAlmacen; }
    public void setCapacidadAlmacen(int capacidadAlmacen) { this.capacidadAlmacen = capacidadAlmacen; }

    public int getIsActiva() { return isActiva; }
    public void setIsActiva(int isActiva) { this.isActiva = isActiva; }
}
