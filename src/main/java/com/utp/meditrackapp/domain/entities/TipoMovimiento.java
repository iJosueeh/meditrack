package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio TipoMovimiento.
 */
public class TipoMovimiento {
    private String id;
    private String nombre;
    private int isActivo;

    public TipoMovimiento() {
    }

    // === Comportamiento de Dominio ===

    public boolean isEntrada() {
        return nombre != null && nombre.toLowerCase().contains("entrada");
    }

    public boolean isSalida() {
        return nombre != null && nombre.toLowerCase().contains("salida");
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }
}
