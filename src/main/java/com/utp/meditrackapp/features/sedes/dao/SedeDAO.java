package com.utp.meditrackapp.features.sedes.dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Sede;
import com.utp.meditrackapp.features.sedes.models.SedeDetalleDTO;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SedeDAO {

    public List<SedeDetalleDTO> getAllWithDetails() throws SQLException {
        List<SedeDetalleDTO> list = new ArrayList<>();
        // Query complejo para traer métricas por sede
        String sql = "SELECT s.*, " +
                     "(SELECT TOP 1 (nombres + ' ' + apellidos) FROM usuarios WHERE sede_id = s.id AND rol_id = 'ROL-001') as admin_name, " +
                     "(SELECT COUNT(*) FROM usuarios WHERE sede_id = s.id) as emp_count, " +
                     "(SELECT COUNT(*) FROM lotes l WHERE l.sede_id = s.id AND l.cantidad < 10) as critico_count " +
                     "FROM sedes s ORDER BY s.nombre";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                SedeDetalleDTO dto = new SedeDetalleDTO(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("direccion"),
                    rs.getInt("is_activa")
                );
                dto.setAdministrador(rs.getString("admin_name") != null ? rs.getString("admin_name") : "No asignado");
                dto.setTotalEmpleados(rs.getInt("emp_count"));
                dto.setItemsCriticos(rs.getInt("critico_count"));
                
                // Lógica de estado de inventario
                if (dto.getItemsCriticos() > 5) dto.setEstadoInventario("Crítico");
                else if (dto.getItemsCriticos() > 0) dto.setEstadoInventario("Bajo Stock");
                else dto.setEstadoInventario("Óptimo");
                
                list.add(dto);
            }
        }
        return list;
    }

    public int getTotalEmployeesGlobal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean save(Sede sede) throws SQLException {
        String sql = "INSERT INTO sedes (id, nombre, direccion, is_activa) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sede.getId());
            ps.setString(2, sede.getNombre());
            ps.setString(3, sede.getDireccion());
            ps.setInt(4, sede.getIsActiva());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean update(Sede sede) throws SQLException {
        String sql = "UPDATE sedes SET nombre = ?, direccion = ?, is_activa = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sede.getNombre());
            ps.setString(2, sede.getDireccion());
            ps.setInt(3, sede.getIsActiva());
            ps.setString(4, sede.getId());
            return ps.executeUpdate() > 0;
        }
    }
}
