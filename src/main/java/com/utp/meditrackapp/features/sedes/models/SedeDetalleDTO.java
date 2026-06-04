package com.utp.meditrackapp.features.sedes.models;

import com.utp.meditrackapp.core.models.entity.Sede;
import com.utp.meditrackapp.core.models.entity.Usuario;
import java.util.ArrayList;
import java.util.List;

public class SedeDetalleDTO extends Sede {
    private String administrador;
    private int totalEmpleados;
    private String estadoInventario; // "Óptimo", "Bajo Stock", "Crítico"
    private int itemsCriticos;
    
    // Management Fields (Stored in DTO to keep Entity clean)
    private List<Usuario> staff = new ArrayList<>();

    public SedeDetalleDTO(String id, String nombre, String direccion, int isActiva) {
        super(id, nombre, direccion, isActiva);
    }

    public String getAdministrador() { return administrador; }
    public void setAdministrador(String administrador) { this.administrador = administrador; }

    public int getTotalEmpleados() { return totalEmpleados; }
    public void setTotalEmpleados(int totalEmpleados) { this.totalEmpleados = totalEmpleados; }

    public String getEstadoInventario() { return estadoInventario; }
    public void setEstadoInventario(String estadoInventario) { this.estadoInventario = estadoInventario; }

    public int getItemsCriticos() { return itemsCriticos; }
    public void setItemsCriticos(int itemsCriticos) { this.itemsCriticos = itemsCriticos; }

    public List<Usuario> getStaff() { return staff; }
    public void setStaff(List<Usuario> staff) { this.staff = staff; }
}
