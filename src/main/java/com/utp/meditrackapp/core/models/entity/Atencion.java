package com.utp.meditrackapp.core.models.entity;

import java.time.LocalDateTime;

public class Atencion {
    private String id;
    private String sedeId;
    private String pacienteId;
    private String usuarioId;
    private String numeroReceta;
    private LocalDateTime fechaAtencion;

    public Atencion() {}

    public Atencion(String id, String sedeId, String pacienteId, String usuarioId, String numeroReceta, LocalDateTime fechaAtencion) {
        this.id = id;
        this.sedeId = sedeId;
        this.pacienteId = pacienteId;
        this.usuarioId = usuarioId;
        this.numeroReceta = numeroReceta;
        this.fechaAtencion = fechaAtencion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSedeId() { return sedeId; }
    public void setSedeId(String sedeId) { this.sedeId = sedeId; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getNumeroReceta() { return numeroReceta; }
    public void setNumeroReceta(String numeroReceta) { this.numeroReceta = numeroReceta; }

    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }
}
