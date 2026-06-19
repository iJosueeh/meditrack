package com.utp.meditrackapp.domain.entities;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Atención con comportamiento de negocio.
 * Refactorizada desde core.models.entity.Atencion
 */
public class Atencion {
    private String id;
    private String sedeId;
    private String pacienteId;
    private String usuarioId;
    private String numeroReceta;
    private String medico;
    private LocalDateTime fechaAtencion;

    // Transient fields for UI
    private String pacienteNombre;
    private String sedeNombre;

    public Atencion() {
    }

    // === Comportamiento de Dominio ===

    /**
     * Verifica si la atención es reciente (últimas 24 horas).
     */
    public boolean isReciente() {
        if (fechaAtencion == null) return false;
        return fechaAtencion.isAfter(LocalDateTime.now().minusHours(24));
    }

    /**
     * Obtiene un resumen de la atención para UI.
     */
    public String getResumen() {
        return "Receta: " + (numeroReceta != null ? numeroReceta : "N/A") +
               " | Fecha: " + (fechaAtencion != null ? fechaAtencion.toLocalDate() : "N/A");
    }

    /**
     * Valida que la atención tenga todos los campos obligatorios.
     *
     * @return null si es válido, mensaje de error si no lo es
     */
    public String validate() {
        if (sedeId == null || sedeId.trim().isEmpty()) {
            return "La sede es obligatoria.";
        }
        if (pacienteId == null || pacienteId.trim().isEmpty()) {
            return "El paciente es obligatorio.";
        }
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            return "El usuario es obligatorio.";
        }
        if (numeroReceta == null || numeroReceta.trim().isEmpty()) {
            return "El número de receta es obligatorio.";
        }
        return null;
    }

    // === Getters y Setters ===

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

    public String getMedico() { return medico; }
    public void setMedico(String medico) { this.medico = medico; }

    public LocalDateTime getFechaAtencion() { return fechaAtencion; }
    public void setFechaAtencion(LocalDateTime fechaAtencion) { this.fechaAtencion = fechaAtencion; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }
}
