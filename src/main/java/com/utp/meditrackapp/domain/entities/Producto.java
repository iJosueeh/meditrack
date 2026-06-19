package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio Producto con comportamiento de negocio.
 */
public class Producto {
    private String id;
    private String categoriaId;
    private String codigoDigemid;
    private String nombre;
    private String detalle;
    private String unidadMedida;
    private int isActivo;
    private Integer stockMinimo;
    private Double precioUnitario;
    private String categoriaNombre;

    public Producto() {
    }

    // === Comportamiento de Dominio ===

    public boolean isActivo() {
        return isActivo == 1;
    }

    public void activar() {
        this.isActivo = 1;
    }

    public void desactivar() {
        this.isActivo = 0;
    }

    public boolean tieneStockBajo(int stockActual) {
        return stockMinimo != null && stockActual < stockMinimo;
    }

    public String validate() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre del producto es obligatorio.";
        }
        if (codigoDigemid == null || codigoDigemid.trim().isEmpty()) {
            return "El código DIGEMID es obligatorio.";
        }
        if (categoriaId == null || categoriaId.trim().isEmpty()) {
            return "La categoría es obligatoria.";
        }
        if (unidadMedida == null || unidadMedida.trim().isEmpty()) {
            return "La unidad de medida es obligatoria.";
        }
        return null;
    }

    // === Getters y Setters ===

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
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }

    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }

    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }
}
