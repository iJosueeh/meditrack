package com.utp.meditrackapp.features.patients.repository;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Paciente;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class PacienteRepositoryImpl implements PacienteRepository {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    @Override
    public List<Paciente> findAll() {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE is_activo = 1 ORDER BY apellidos ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pacientes.add(mapResultSetToPaciente(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en findAll: " + e.getMessage());
        }
        return pacientes;
    }

    @Override
    public List<Paciente> findByQuery(String query) {
        List<Paciente> pacientes = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }
        
        String[] terms = query.trim().split("\\s+");
        StringBuilder sql = new StringBuilder("SELECT * FROM pacientes WHERE is_activo = 1");
        for (int i = 0; i < terms.length; i++) {
            sql.append(" AND (numero_documento LIKE ? OR nombres LIKE ? OR apellidos LIKE ?)");
        }
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
             
            int paramIndex = 1;
            for (String term : terms) {
                String pattern = "%" + term + "%";
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
                ps.setString(paramIndex++, pattern);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapResultSetToPaciente(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en findByQuery: " + e.getMessage());
        }
        return pacientes;
    }

    @Override
    public Paciente findByDocumento(String numeroDocumento) {
        String sql = "SELECT * FROM pacientes WHERE numero_documento = ? AND is_activo = 1";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToPaciente(rs);
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en findByDocumento: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean save(Paciente paciente) {
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
                ps.setInt(7, paciente.getIsActivo() != 0 ? paciente.getIsActivo() : 1);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en save: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Paciente paciente) {
        String sql = "UPDATE pacientes SET tipo_documento = ?, numero_documento = ?, nombres = ?, apellidos = ?, telefono = ?, is_activo = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, paciente.getTipoDocumento());
            ps.setString(2, paciente.getNumeroDocumento());
            ps.setString(3, paciente.getNombres());
            ps.setString(4, paciente.getApellidos());
            ps.setString(5, paciente.getTelefono());
            ps.setInt(6, paciente.getIsActivo());
            ps.setString(7, paciente.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en update: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "UPDATE pacientes SET is_activo = 0 WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DB ERROR] Error en delete: " + e.getMessage());
            return false;
        }
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
