package com.utp.meditrackapp.features.patients.Dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Paciente;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDao {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    public List<Paciente> getAll() {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE is_activo = 1";
        
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al obtener pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public List<Paciente> search(String query) {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE (numero_documento LIKE ? OR nombres LIKE ? OR apellidos LIKE ?) AND is_activo = 1";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + query + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapResultSetToPaciente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al buscar pacientes: " + e.getMessage());
        }
        return pacientes;
    }

    public boolean create(Paciente paciente) {
        String sql = "INSERT INTO pacientes (id, tipo_documento, numero_documento, nombres, apellidos, telefono, is_activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConfig.getConnection()) {
            var user = com.utp.meditrackapp.core.config.SessionManager.getInstance().getCurrentUser();
            String sedeId = user != null ? user.getSedeId() : "SED-001";
            String id = IdGenerator.generateSedeDependentId(conn, "pacientes", EntidadPrefix.PACIENTE, sedeId, 6);
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, paciente.getTipoDocumento());
                ps.setString(3, paciente.getNumeroDocumento());
                ps.setString(4, paciente.getNombres());
                ps.setString(5, paciente.getApellidos());
                ps.setString(6, paciente.getTelefono());
                ps.setInt(7, 1); // Activo por defecto
                
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al crear paciente: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Paciente paciente) {
        String sql = "UPDATE pacientes SET tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, telefono = ? WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, paciente.getTipoDocumento());
            ps.setString(2, paciente.getNumeroDocumento());
            ps.setString(3, paciente.getNombres());
            ps.setString(4, paciente.getApellidos());
            ps.setString(5, paciente.getTelefono());
            ps.setString(6, paciente.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al actualizar paciente: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String id) {
        String sql = "UPDATE pacientes SET is_activo = 0 WHERE id = ?";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al eliminar paciente: " + e.getMessage());
            return false;
        }
    }

    public Paciente getByDocumento(String numeroDocumento) {
        String sql = "SELECT * FROM pacientes WHERE numero_documento = ? AND is_activo = 1";
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, numeroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaciente(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error al obtener paciente por documento: " + e.getMessage());
        }
        return null;
    }

    public int countTotal(String sedeId) {
        if (sedeId != null && !sedeId.isEmpty()) {
            String sql = "SELECT COUNT(DISTINCT a.paciente_id) FROM atenciones a JOIN pacientes p ON a.paciente_id = p.id WHERE a.sede_id = ? AND p.is_activo = 1";
            try (Connection conn = dbConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sedeId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            } catch (SQLException e) { e.printStackTrace(); }
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM pacientes WHERE is_activo = 1";
        try (Connection conn = dbConfig.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countTodayAttentions(String sedeId) {
        String sql = "SELECT COUNT(DISTINCT paciente_id) FROM atenciones WHERE sede_id = ? AND CAST(fecha_atencion AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = dbConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countNewPatientsMonth(String sedeId) {
        String sql = "SELECT COUNT(*) FROM (" +
                     "  SELECT paciente_id, MIN(fecha_atencion) as primera_atencion " +
                     "  FROM atenciones " +
                     "  WHERE sede_id = ? " +
                     "  GROUP BY paciente_id" +
                     ") AS PrimerasAtenciones " +
                     "WHERE MONTH(primera_atencion) = MONTH(GETDATE()) " +
                     "AND YEAR(primera_atencion) = YEAR(GETDATE())";
        try (Connection conn = dbConfig.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private Paciente mapResultSetToPaciente(ResultSet rs) throws SQLException {
        return new Paciente(
            rs.getString("id"),
            rs.getString("tipo_documento"),
            rs.getString("numero_documento"),
            rs.getString("nombres"),
            rs.getString("apellidos"),
            rs.getString("telefono"),
            rs.getInt("is_activo")
        );
    }
}
