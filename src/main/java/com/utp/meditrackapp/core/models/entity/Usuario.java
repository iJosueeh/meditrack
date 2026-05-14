package com.utp.meditrackapp.core.models.entity;

public class Usuario {

    private String id;
    private String sedeId;
    private String rolId;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String password;
    private boolean isActivo;

    public Usuario() {
    }
    public Usuario(String id, String sedeId, String rolId, String tipoDocumento,
         String numeroDocumento, String nombres, String apellidos,
          String password, boolean isActivo) {
        this.id = id;
        this.sedeId = sedeId;
        this.rolId = rolId;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.password = password;
        this.isActivo = isActivo;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSedeId() {
        return sedeId;
    }
    public void setSedeId(String sedeId) {
        this.sedeId = sedeId;
    }
    public String getRolId() {
        return rolId;
    }
    public void setRolId(String rolId) {
        this.rolId = rolId;
    }
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public String getNumeroDocumento() {
        return numeroDocumento;
    }
    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
    public String getNombres() {
        return nombres;
    }
    public void setNombres(String nombres) {
        this.nombres = nombres;
    }
    public String getApellidos() {
        return apellidos;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean isActivo() {
        return isActivo;
    }
    public void setActivo(boolean isActivo) {
        this.isActivo = isActivo;
    }

    
}
