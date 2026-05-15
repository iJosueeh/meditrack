package com.utp.meditrackapp.core.models.entity;

public class Producto {
    private String id;
    private String categoriaId;
    private String codigoDigemid;
    private String nombre;
    private String detalle;
    private String unidadMedida;
    private int isActivo;

    public Producto() {}

    public Producto(String id, String categoriaId, String codigoDigemid, String nombre, String detalle, String unidadMedida, int isActivo) {
        this.id = id;
        this.categoriaId = categoriaId;
        this.codigoDigemid = codigoDigemid;
        this.nombre = nombre;
        this.detalle = detalle;
        this.unidadMedida = unidadMedida;
        this.isActivo = isActivo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }

    public String getCodigoDigemid() { return codigoDigemid; }
    public void setCodigoDigemid(String codigoDigemid) { this.codigoDigemid = codigoDigemid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActiva) { this.isActivo = isActiva; }
}
