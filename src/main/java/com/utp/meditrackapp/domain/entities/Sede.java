package com.utp.meditrackapp.domain.entities;

import com.utp.meditrackapp.core.util.DateTimeProvider;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Sede con comportamiento de negocio.
 */
public class Sede {
    private String id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String ubigeo;
    private String administradorId;
    private String tipoSede;
    private int capacidadAlmacen;
    private int isActiva;
    private int isBloqueada;
    private String motivoBloqueo;
    private LocalDateTime fechaBloqueo;

    private String administradorNombre;
    private int totalEmpleados;

    public Sede() {
    }

    public Sede(String id, String nombre, String direccion, int isActiva) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.isActiva = isActiva;
    }

    // === Comportamiento de Dominio ===

    public boolean isActiva() {
        return isActiva == 1;
    }

    public boolean isBloqueada() {
        return isBloqueada == 1;
    }

    public boolean isOperativa() {
        return isActiva == 1 && isBloqueada == 0;
    }

    public void activar() {
        this.isActiva = 1;
    }

    public void desactivar() {
        this.isActiva = 0;
    }

    public void toggleEstado() {
        this.isActiva = isActiva == 1 ? 0 : 1;
    }

    /**
     * Bloquea la sede con un motivo específico.
     * @param motivo razón del bloqueo
     */
    public void bloquear(String motivo) {
        this.isBloqueada = 1;
        this.motivoBloqueo = motivo;
        this.fechaBloqueo = DateTimeProvider.now();
    }

    /**
     * Desbloquea la sede.
     */
    public void desbloquear() {
        this.isBloqueada = 0;
        this.motivoBloqueo = null;
        this.fechaBloqueo = null;
    }

    /**
     * Valida que los campos obligatorios estén presentes.
     *
     * @return null si es válido, mensaje de error si no lo es
     */
    public String validate() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "El nombre de la sede es obligatorio.";
        }
        if (direccion == null || direccion.trim().isEmpty()) {
            return "La dirección de la sede es obligatoria.";
        }
        if (telefono == null || telefono.trim().length() != 9 || !telefono.trim().matches("\\d+")) {
            return "El teléfono debe tener exactamente 9 dígitos numéricos.";
        }
        return null;
    }

    // === Getters y Setters ===

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getUbigeo() { return ubigeo; }
    public void setUbigeo(String ubigeo) { this.ubigeo = ubigeo; }

    public String getAdministradorId() { return administradorId; }
    public void setAdministradorId(String administradorId) { this.administradorId = administradorId; }

    public String getAdministradorNombre() { return administradorNombre; }
    public void setAdministradorNombre(String administradorNombre) { this.administradorNombre = administradorNombre; }

    public int getTotalEmpleados() { return totalEmpleados; }
    public void setTotalEmpleados(int totalEmpleados) { this.totalEmpleados = totalEmpleados; }

    public String getTipoSede() { return tipoSede; }
    public void setTipoSede(String tipoSede) { this.tipoSede = tipoSede; }

    public int getCapacidadAlmacen() { return capacidadAlmacen; }
    public void setCapacidadAlmacen(int capacidadAlmacen) { this.capacidadAlmacen = capacidadAlmacen; }

    public int getIsActiva() { return isActiva; }
    public void setIsActiva(int isActiva) { this.isActiva = isActiva; }

    public int getIsBloqueada() { return isBloqueada; }
    public void setIsBloqueada(int isBloqueada) { this.isBloqueada = isBloqueada; }

    public String getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    public LocalDateTime getFechaBloqueo() { return fechaBloqueo; }
    public void setFechaBloqueo(LocalDateTime fechaBloqueo) { this.fechaBloqueo = fechaBloqueo; }
}
