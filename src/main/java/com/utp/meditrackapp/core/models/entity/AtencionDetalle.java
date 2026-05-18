package com.utp.meditrackapp.core.models.entity;

public class AtencionDetalle {
    private String id;
    private String atencionId;
    private String loteId;
    private int cantidadEntregada;

    public AtencionDetalle() {}

    public AtencionDetalle(String id, String atencionId, String loteId, int cantidadEntregada) {
        this.id = id;
        this.atencionId = atencionId;
        this.loteId = loteId;
        this.cantidadEntregada = cantidadEntregada;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAtencionId() { return atencionId; }
    public void setAtencionId(String atencionId) { this.atencionId = atencionId; }

    public String getLoteId() { return loteId; }
    public void setLoteId(String loteId) { this.loteId = loteId; }

    public int getCantidadEntregada() { return cantidadEntregada; }
    public void setCantidadEntregada(int cantidadEntregada) { this.cantidadEntregada = cantidadEntregada; }
}
