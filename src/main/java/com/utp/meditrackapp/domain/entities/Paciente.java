package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio Paciente con comportamiento de negocio.
 * Refactorizada desde core.models.entity.Paciente
 */
public class Paciente {
    private String id;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String telefono;
    private int isActivo;

    public Paciente() {
    }

    // === Comportamiento de Dominio ===

    /**
     * Obtiene el nombre completo del paciente.
     */
    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    /**
     * Verifica si el paciente está activo en el sistema.
     */
    public boolean isActivo() {
        return isActivo == 1;
    }

    /**
     * Desactiva el paciente (borrado lógico).
     */
    public void desactivar() {
        this.isActivo = 0;
    }

    /**
     * Activa el paciente.
     */
    public void activar() {
        this.isActivo = 1;
    }

    /**
     * Valida si el documento es un DNI válido (8 dígitos).
     */
    public boolean isDniValido() {
        if (!"DNI".equalsIgnoreCase(tipoDocumento)) return false;
        return numeroDocumento != null && numeroDocumento.matches("\\d{8}");
    }

    /**
     * Valida si el documento es un Carnet de Extranjería válido (8-12 alfanuméricos).
     */
    public boolean isCeValido() {
        if (!"CE".equalsIgnoreCase(tipoDocumento)) return false;
        return numeroDocumento != null && numeroDocumento.matches("[a-zA-Z0-9]{8,12}");
    }

    /**
     * Valida si el tipo de documento es soportado.
     */
    public boolean isTipoDocumentoValido() {
        return "DNI".equalsIgnoreCase(tipoDocumento) || "CE".equalsIgnoreCase(tipoDocumento);
    }

    /**
     * Valida que los campos obligatorios estén presentes.
     *
     * @return null si es válido, mensaje de error si no lo es
     */
    public String validate() {
        if (nombres == null || nombres.trim().isEmpty()) {
            return "Los nombres son obligatorios.";
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            return "Los apellidos son obligatorios.";
        }
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            return "El número de documento es obligatorio.";
        }
        if (!isTipoDocumentoValido()) {
            return "Tipo de documento no válido. Use DNI o CE.";
        }
        if ("DNI".equalsIgnoreCase(tipoDocumento) && !isDniValido()) {
            return "El DNI debe contener exactamente 8 dígitos numéricos.";
        }
        if ("CE".equalsIgnoreCase(tipoDocumento) && !isCeValido()) {
            return "El Carnet de Extranjería (CE) debe tener entre 8 y 12 caracteres alfanuméricos.";
        }
        return null;
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }
}
