package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.DateTimeProvider;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.core.validation.SedeAccessValidator;
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

            String sql = "INSERT INTO atenciones (id, sede_id, paciente_id, usuario_id, numero_receta, medico, fecha_atencion) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, atencion.getId());
                ps.setString(2, atencion.getSedeId());
                ps.setString(3, atencion.getPacienteId());
                ps.setString(4, atencion.getUsuarioId());
                ps.setString(5, atencion.getNumeroReceta());
                ps.setString(6, atencion.getMedico());
                ps.setTimestamp(7, Timestamp.valueOf(DateTimeProvider.now()));
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
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        List<Atencion> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM atenciones");
        
        if (sedeId != null) {
            sql.append(" WHERE sede_id = ?");
        }
        sql.append(" ORDER BY fecha_atencion DESC");
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
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
    public Optional<Atencion> findById(String id) {
        String sql = "SELECT * FROM atenciones WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Atencion atencion = mapAtencion(rs);
                    // Validar acceso por sede (Admin tiene acceso global)
                    SedeAccessValidator.validarAcceso(atencion.getSedeId(), "Atención " + id);
                    return Optional.of(atencion);
                }
            }
        } catch (SedeAccessValidator.AccesoSedeDenegadoException e) {
            throw e;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Atencion> findByPaciente(String pacienteId) {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        
        List<Atencion> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM atenciones WHERE paciente_id = ?");
        
        if (sedeId != null) {
            sql.append(" AND sede_id = ?");
        }
        sql.append(" ORDER BY fecha_atencion DESC");
        
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setString(i++, pacienteId);
            if (sedeId != null) {
                ps.setString(i++, sedeId);
            }
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

    @Override
    public List<String> findMedicosDistinct() {
        String sedeId = SedeAccessValidator.getSedeParaConsulta();
        List<String> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT medico FROM atenciones WHERE medico IS NOT NULL AND medico != ''");
        if (sedeId != null) {
            sql.append(" AND sede_id = ?");
        }
        sql.append(" ORDER BY medico");
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (sedeId != null) {
                ps.setString(1, sedeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(rs.getString("medico"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public int countBySedeAndYear(String sedeId, int year) {
        String sql = "SELECT COUNT(*) FROM atenciones WHERE sede_id = ? AND YEAR(fecha_atencion) = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            ps.setInt(2, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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

    @Override
    public List<AtencionDetalle> findDetallesByAtencionId(String atencionId) {
        List<AtencionDetalle> lista = new ArrayList<>();
        String sql = "SELECT ad.*, l.numero_lote, l.fecha_vencimiento, l.producto_id, p.nombre as producto_nombre " +
            "FROM atencion_detalles ad " +
            "JOIN lotes l ON ad.lote_id = l.id " +
            "JOIN productos p ON l.producto_id = p.id " +
            "WHERE ad.atencion_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atencionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AtencionDetalle d = new AtencionDetalle();
                    d.setId(rs.getString("id"));
                    d.setAtencionId(rs.getString("atencion_id"));
                    d.setLoteId(rs.getString("lote_id"));
                    d.setCantidadEntregada(rs.getInt("cantidad_entregada"));
                    d.setLoteNumero(rs.getString("numero_lote"));
                    d.setProductoId(rs.getString("producto_id"));
                    d.setProductoNombre(rs.getString("producto_nombre"));
                    Timestamp vence = rs.getTimestamp("fecha_vencimiento");
                    if (vence != null) {
                        d.setFechaVencimiento(vence.toLocalDateTime().toLocalDate().toString());
                    }
                    lista.add(d);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    @Override
    public void updateAtencion(Atencion atencion) {
        String sql = "UPDATE atenciones SET numero_receta = ?, medico = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atencion.getNumeroReceta());
            ps.setString(2, atencion.getMedico());
            ps.setString(3, atencion.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar atención: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateDetalle(String detalleId, String nuevoLoteId, int nuevaCantidad) {
        String sql = "UPDATE atencion_detalles SET lote_id = ?, cantidad_entregada = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoLoteId);
            ps.setInt(2, nuevaCantidad);
            ps.setString(3, detalleId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar detalle: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAtencion(String atencionId) {
        String sqlDetalles = "DELETE FROM atencion_detalles WHERE atencion_id = ?";
        String sqlAtencion = "DELETE FROM atenciones WHERE id = ?";
        try (Connection conn = dbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlDetalles);
                 PreparedStatement ps2 = conn.prepareStatement(sqlAtencion)) {
                ps1.setString(1, atencionId);
                ps1.executeUpdate();
                ps2.setString(1, atencionId);
                ps2.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar atención: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteDetallesByAtencionId(java.sql.Connection conn, String atencionId) {
        String sql = "DELETE FROM atencion_detalles WHERE atencion_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atencionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar detalles: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteMovimientosByAtencionId(java.sql.Connection conn, String atencionId) {
        String sql = "DELETE FROM movimientos WHERE lote_id IN " +
            "(SELECT lote_id FROM atencion_detalles WHERE atencion_id = ?) " +
            "AND observacion LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, atencionId);
            ps.setString(2, "%Receta:%");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar movimientos: " + e.getMessage(), e);
        }
    }
}
