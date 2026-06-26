package com.utp.meditrackapp.domain.entities;

/**
 * Entidad de dominio Usuario con comportamiento de negocio.
 */
public class Usuario {
    private String id;
    private String sedeId;
    private String rolId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String password;
    private String sedeNombre;
    private String rolNombre;
    private int isActivo;

    public Usuario() {
    }

    // === Comportamiento de Dominio ===

    public boolean isActivo() {
        return isActivo == 1;
    }

    public String getNombreCompleto() {
        return (nombres != null ? nombres : "") + " " + (apellidos != null ? apellidos : "");
    }

    public boolean isAdmin() {
        return rolId != null && rolId.equals("ROL-00-0000001");
    }

    public boolean isJefeSede() {
        return rolNombre != null && rolNombre.toUpperCase().contains("JEFE");
    }

    public boolean isTecnico() {
        return rolId != null && rolId.equals("ROL-00-0000003");
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
        if (sedeId == null || sedeId.trim().isEmpty()) {
            return "La sede es obligatoria.";
        }
        if (rolId == null || rolId.trim().isEmpty()) {
            return "El rol es obligatorio.";
        }
        return null;
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSedeId() { return sedeId; }
    public void setSedeId(String sedeId) { this.sedeId = sedeId; }

    public String getRolId() { return rolId; }
    public void setRolId(String rolId) { this.rolId = rolId; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSedeNombre() { return sedeNombre; }
    public void setSedeNombre(String sedeNombre) { this.sedeNombre = sedeNombre; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public int getIsActivo() { return isActivo; }
    public void setIsActivo(int isActivo) { this.isActivo = isActivo; }
}
