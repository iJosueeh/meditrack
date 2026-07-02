package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio Permiso para el sistema RBAC.
 * Representa un permiso individual que puede ser asignado a roles.
 */
public class Permiso {
    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private String modulo;
    private int orden;
    private int isActivo;

    public Permiso() {
    }

    public Permiso(String id, String codigo, String nombre, String modulo) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.modulo = modulo;
        this.isActivo = 1;
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }

    @Override
    public String toString() {
        return nombre;
    }
}
