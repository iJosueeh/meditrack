package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Atencion;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;
import com.utp.meditrackapp.domain.ports.out.AtencionRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Atenciones.
 * Migrado desde AtencionDAO.
 */
public class JdbcAtencionRepository implements AtencionRepository {
    private final DatabaseConfig dbConfig;

    public JdbcAtencionRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    @Override
    public Atencion save(Connection conn, Atencion atencion) {
        try {
            if (atencion.getId() == null || atencion.getId().isBlank()) {
                atencion.setId(IdGenerator.generateSedeDependentId(conn, "atenciones", EntidadPrefix.ATENCION, atencion.getSedeId(), 6));
            }

            String sql = "INSERT INTO atenciones (id, sede_id, paciente_id, usuario_id, numero_receta, medico, fecha_atencion) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, atencion.getId());
                ps.setString(2, atencion.getSedeId());
                ps.setString(3, atencion.getPacienteId());
                ps.setString(4, atencion.getUsuarioId());
                ps.setString(5, atencion.getNumeroReceta());
                ps.setString(6, atencion.getMedico());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar atención: " + e.getMessage(), e);
        }
        return atencion;
    }

    @Override
    public AtencionDetalle saveDetalle(Connection conn, AtencionDetalle detalle) {
        try {
            if (detalle.getId() == null || detalle.getId().isBlank()) {
                detalle.setId(IdGenerator.generateId(conn, "atencion_detalles", EntidadPrefix.ATENCION_DETALLE, 8));
            }

            String sql = "INSERT INTO atencion_detalles (id, atencion_id, lote_id, cantidad_entregada) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, detalle.getId());
                ps.setString(2, detalle.getAtencionId());
                ps.setString(3, detalle.getLoteId());
                ps.setInt(4, detalle.getCantidadEntregada());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar detalle de atención: " + e.getMessage(), e);
        }
        return detalle;
    }

    @Override
    public boolean existeReceta(String sedeId, String numeroReceta) {
        String sql = "SELECT COUNT(*) FROM atenciones WHERE sede_id = ? AND numero_receta = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setString(2, numeroReceta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Atencion> findAll() {
        List<Atencion> lista = new ArrayList<>();
        String sql = "SELECT * FROM atenciones ORDER BY fecha_atencion DESC";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapAtencion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public Optional<Atencion> findById(String id) {
        String sql = "SELECT * FROM atenciones WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapAtencion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Atencion> findByPaciente(String pacienteId) {
        List<Atencion> lista = new ArrayList<>();
        String sql = "SELECT * FROM atenciones WHERE paciente_id = ? ORDER BY fecha_atencion DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pacienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAtencion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Atencion> findByReceta(String sedeId, String numeroReceta) {
        List<Atencion> lista = new ArrayList<>();
        String sql = "SELECT * FROM atenciones WHERE sede_id = ? AND numero_receta LIKE ? ORDER BY fecha_atencion DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setString(2, "%" + numeroReceta + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAtencion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public List<Atencion> findBySede(String sedeId) {
        List<Atencion> lista = new ArrayList<>();
        String sql = "SELECT * FROM atenciones WHERE sede_id = ? ORDER BY fecha_atencion DESC";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapAtencion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private Atencion mapAtencion(ResultSet rs) throws SQLException {
        Atencion a = new Atencion();
        a.setId(rs.getString("id"));
        a.setSedeId(rs.getString("sede_id"));
        a.setPacienteId(rs.getString("paciente_id"));
        a.setUsuarioId(rs.getString("usuario_id"));
        a.setNumeroReceta(rs.getString("numero_receta"));
        a.setMedico(rs.getString("medico"));
        Timestamp ts = rs.getTimestamp("fecha_atencion");
        if (ts != null) {
            a.setFechaAtencion(ts.toLocalDateTime());
        }
        return a;
    }
}
