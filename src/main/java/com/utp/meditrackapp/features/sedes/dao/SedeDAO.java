package com.utp.meditrackapp.features.sedes.dao;

import com.utp.meditrackapp.core.dao.JdbcDaoSupport;
import com.utp.meditrackapp.core.models.entity.Sede;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.features.sedes.models.SedeDetalleDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SedeDAO extends JdbcDaoSupport {

    private static String safeGetString(ResultSet rs, String column) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnLabel(i).equalsIgnoreCase(column)) {
                return rs.getString(i);
            }
        }
        return null;
    }

    public Optional<Sede> buscarPorId(String id) throws SQLException {
        String sql = "SELECT * FROM sedes WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Sede s = new Sede(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getInt("is_activa")
                    );
                    s.setTelefono(rs.getString("telefono"));
                    s.setUbigeo(safeGetString(rs, "ubigeo"));
                    return Optional.of(s);
                }
            }
        }
        return Optional.empty();
    }

    public List<SedeDetalleDTO> getAllWithDetails() throws SQLException {
        List<SedeDetalleDTO> list = new ArrayList<>();
        String sql = "SELECT s.*, " +
                     "(SELECT TOP 1 (u.nombres + ' ' + u.apellidos) " +
                     " FROM usuarios u " +
                     " JOIN roles r ON u.rol_id = r.id " +
                     " WHERE u.sede_id = s.id " +
                     " AND (UPPER(r.nombre) LIKE '%ADMIN%' OR UPPER(r.nombre) LIKE '%JEFE%')) as admin_name, " +
                     "(SELECT TOP 1 u.id " +
                     " FROM usuarios u " +
                     " JOIN roles r ON u.rol_id = r.id " +
                     " WHERE u.sede_id = s.id " +
                     " AND (UPPER(r.nombre) LIKE '%ADMIN%' OR UPPER(r.nombre) LIKE '%JEFE%')) as admin_id, " +
                     "(SELECT COUNT(*) FROM usuarios WHERE sede_id = s.id) as emp_count, " +
                     "(SELECT COUNT(*) FROM lotes l WHERE l.sede_id = s.id AND l.cantidad < 10) as critico_count " +
                     "FROM sedes s ORDER BY s.nombre";

        try (Connection conn = getConnection();
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
                dto.setAdministradorId(rs.getString("admin_id"));
                dto.setTotalEmpleados(rs.getInt("emp_count"));
                dto.setItemsCriticos(rs.getInt("critico_count"));
                dto.setTelefono(rs.getString("telefono"));
                dto.setUbigeo(safeGetString(rs, "ubigeo"));

                if (dto.getItemsCriticos() > 5) dto.setEstadoInventario("Crítico");
                else if (dto.getItemsCriticos() > 0) dto.setEstadoInventario("Bajo Stock");
                else dto.setEstadoInventario("Óptimo");
                
                list.add(dto);
            }
        }
        return list;
    }

    public List<Usuario> getStaffBySede(String sedeId) throws SQLException {
        String sql = "SELECT u.*, r.nombre as rol_nombre FROM usuarios u " +
                     "JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.sede_id = ? AND u.is_activo = 1";
        List<Usuario> staff = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = new Usuario(
                        rs.getString("id"), rs.getString("sede_id"), rs.getString("rol_id"),
                        rs.getString("tipo_documento"), rs.getString("numero_documento"),
                        rs.getString("nombres"), rs.getString("apellidos"), null, rs.getInt("is_activo")
                    );
                    u.setRolNombre(rs.getString("rol_nombre"));
                    staff.add(u);
                }
            }
        }
        return staff;
    }

    public boolean assignUserToSede(String userId, String sedeId, String rolId) throws SQLException {
        String sql = "UPDATE usuarios SET sede_id = ?, rol_id = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setString(2, rolId);
            ps.setString(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getTotalEmployeesGlobal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean save(Sede sede) throws SQLException {
        validarSede(sede);
        Connection conn = getConnection();
        try {
            if (sede.getId() == null || sede.getId().isBlank()) {
                sede.setId(IdGenerator.generateId(conn, "sedes", EntidadPrefix.SEDE, 3));
            }
            String sql = "INSERT INTO sedes (id, nombre, direccion, is_activa, telefono, ubigeo) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sede.getId());
                ps.setString(2, sede.getNombre());
                ps.setString(3, sede.getDireccion());
                ps.setInt(4, sede.getIsActiva());
                ps.setString(5, sede.getTelefono());
                ps.setString(6, sede.getUbigeo());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("ubigeo")) {
                String sql = "INSERT INTO sedes (id, nombre, direccion, is_activa, telefono) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sede.getId());
                    ps.setString(2, sede.getNombre());
                    ps.setString(3, sede.getDireccion());
                    ps.setInt(4, sede.getIsActiva());
                    ps.setString(5, sede.getTelefono());
                    return ps.executeUpdate() > 0;
                }
            }
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    public boolean update(Sede sede) throws SQLException {
        validarSede(sede);
        Connection conn = getConnection();
        try {
            String sql = "UPDATE sedes SET nombre = ?, direccion = ?, is_activa = ?, telefono = ?, ubigeo = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sede.getNombre());
                ps.setString(2, sede.getDireccion());
                ps.setInt(3, sede.getIsActiva());
                ps.setString(4, sede.getTelefono());
                ps.setString(5, sede.getUbigeo());
                ps.setString(6, sede.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("ubigeo")) {
                String sql = "UPDATE sedes SET nombre = ?, direccion = ?, is_activa = ?, telefono = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, sede.getNombre());
                    ps.setString(2, sede.getDireccion());
                    ps.setInt(3, sede.getIsActiva());
                    ps.setString(4, sede.getTelefono());
                    ps.setString(5, sede.getId());
                    return ps.executeUpdate() > 0;
                }
            }
            throw e;
        } finally {
            closeConn(conn);
        }
    }

    public void toggleEstado(String id) throws SQLException {
        String sql = "UPDATE sedes SET is_activa = CASE WHEN is_activa = 1 THEN 0 ELSE 1 END WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    private void closeConn(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }

    private void validarSede(Sede sede) {
        if (sede == null) {
            throw new IllegalArgumentException("Los datos de la sede son obligatorios.");
        }
        if (sede.getNombre() == null || sede.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la sede es obligatorio.");
        }
        if (sede.getDireccion() == null || sede.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección de la sede es obligatoria.");
        }
        String tel = sede.getTelefono();
        if (tel == null || tel.trim().length() != 9 || !tel.trim().matches("\\d+")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 9 dígitos numéricos.");
        }
    }
}
