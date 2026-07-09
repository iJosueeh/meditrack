package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.domain.ports.out.PacienteRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Pacientes.
 * Migrado desde PacienteRepositoryImpl y PacienteDao.
 */
public class JdbcPacienteRepository implements PacienteRepository {
    private final DatabaseConfig dbConfig;

    public JdbcPacienteRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public List<Paciente> findAll() {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT * FROM pacientes WHERE is_activo = 1 ORDER BY apellidos ASC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pacientes.add(mapPaciente(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pacientes;
    }

    @Override
    public List<Paciente> findBySede(String sedeId) {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT p.* FROM pacientes p " +
                     "INNER JOIN atenciones a ON p.id = a.paciente_id " +
                     "WHERE a.sede_id = ? AND p.is_activo = 1 " +
                     "GROUP BY p.id, p.tipo_documento, p.numero_documento, p.nombres, p.apellidos, p.telefono, p.is_activo " +
                     "ORDER BY p.apellidos ASC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapPaciente(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                    pacientes.add(mapPaciente(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pacientes;
    }

    @Override
    public Optional<Paciente> findById(String id) {
        String sql = "SELECT * FROM pacientes WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPaciente(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Paciente findByDocumento(String numeroDocumento) {
        String sql = "SELECT * FROM pacientes WHERE numero_documento = ? AND is_activo = 1";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numeroDocumento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapPaciente(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Paciente save(Paciente paciente) {
        String sql = "INSERT INTO pacientes (id, tipo_documento, numero_documento, nombres, apellidos, telefono, is_activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection()) {
            var user = SessionManager.getInstance().getCurrentUser();
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
                ps.executeUpdate();
                paciente.setId(id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar paciente: " + e.getMessage(), e);
        }
        return paciente;
    }

    @Override
    public Paciente update(Paciente paciente) {
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
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar paciente: " + e.getMessage(), e);
        }
        return paciente;
    }

    @Override
    public boolean softDelete(String id) {
        String sql = "UPDATE pacientes SET is_activo = 0 WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al desactivar paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean reactivar(String id) {
        String sql = "UPDATE pacientes SET is_activo = 1 WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al reactivar paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hardDelete(String id) {
        String sql = "DELETE FROM pacientes WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar paciente: " + e.getMessage(), e);
        }
    }

    @Override
    public int countBySede(String sedeId) {
        String sql = "SELECT COUNT(DISTINCT p.id) FROM pacientes p " +
                     "INNER JOIN atenciones a ON p.id = a.paciente_id " +
                     "WHERE a.sede_id = ? AND p.is_activo = 1";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Paciente> findTypeahead(String query, int limit) {
        List<Paciente> pacientes = new ArrayList<>();
        if (query == null || query.trim().length() < 2) {
            return pacientes;
        }

        String trimmed = query.trim();
        StringBuilder sql = new StringBuilder("SELECT TOP (?) * FROM pacientes WHERE is_activo = 1");
        List<String> params = new ArrayList<>();

        if (trimmed.matches("\\d+")) {
            sql.append(" AND numero_documento LIKE ?");
            params.add("%" + trimmed + "%");
        } else {
            String[] terms = trimmed.split("\\s+");
            for (String term : terms) {
                sql.append(" AND (nombres LIKE ? OR apellidos LIKE ?)");
                params.add("%" + term + "%");
                params.add("%" + term + "%");
            }
        }

        sql.append(" ORDER BY apellidos ASC");

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, limit);
            int paramIndex = 2;
            for (String param : params) {
                ps.setString(paramIndex++, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapPaciente(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pacientes;
    }

    /**
     * Cuenta pacientes atendidos hoy en una sede.
     */
    public int countTodayAttentions(String sedeId) {
        String sql = "SELECT COUNT(DISTINCT paciente_id) FROM atenciones WHERE sede_id = ? AND CAST(fecha_atencion AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cuenta pacientes nuevos este mes en una sede.
     */
    public int countNewPatientsMonth(String sedeId) {
        String sql = "SELECT COUNT(*) FROM (" +
                     "  SELECT paciente_id, MIN(fecha_atencion) as primera_atencion " +
                     "  FROM atenciones " +
                     "  WHERE sede_id = ? " +
                     "  GROUP BY paciente_id" +
                     ") AS PrimerasAtenciones " +
                     "WHERE MONTH(primera_atencion) = MONTH(GETDATE()) " +
                     "AND YEAR(primera_atencion) = YEAR(GETDATE())";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Paciente mapPaciente(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getString("id"));
        p.setTipoDocumento(rs.getString("tipo_documento"));
        p.setNumeroDocumento(rs.getString("numero_documento"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        p.setTelefono(rs.getString("telefono"));
        p.setIsActivo(rs.getInt("is_activo"));
        return p;
    }
}
