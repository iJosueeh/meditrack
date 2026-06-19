package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio MotivoMovimiento.
 */
public class MotivoMovimiento {
    private String id;
    private String nombre;
    private int isActivo;

    public MotivoMovimiento() {
    }

    // === Comportamiento de Dominio ===

    public boolean isCompra() {
        return nombre != null && nombre.toLowerCase().contains("compra");
    }

    public boolean isTransferencia() {
        return nombre != null && nombre.toLowerCase().contains("transferencia");
    }

    public boolean isMerma() {
        return nombre != null && nombre.toLowerCase().contains("merma");
    }

    public boolean isAtencion() {
        return nombre != null && nombre.toLowerCase().contains("atención");
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }
}
