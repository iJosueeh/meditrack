package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.DateTimeProvider;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.domain.ports.out.SedeRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del repositorio de Sedes.
 * Migrado desde SedeDAO.
 */
public class JdbcSedeRepository implements SedeRepository {
    private final DatabaseConfig dbConfig;

    public JdbcSedeRepository() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    private static final String SELECT_BASE =
        "SELECT s.*, u.nombres + ' ' + u.apellidos AS administrador_nombre " +
        "FROM sedes s " +
        "LEFT JOIN usuarios u ON s.administrador_id = u.id ";

    @Override
    public Optional<Sede> findById(String id) {
        String sql = SELECT_BASE + "WHERE s.id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapSede(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Sede> findAll() {
        List<Sede> list = new ArrayList<>();
        String sql = SELECT_BASE + "ORDER BY s.nombre";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSede(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Sede> findActivas() {
        List<Sede> list = new ArrayList<>();
        String sql = SELECT_BASE + "WHERE s.is_activa = 1 ORDER BY s.nombre";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapSede(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Sede save(Sede sede) {
        try {
            if (sede.getId() == null || sede.getId().isBlank()) {
                sede.setId(IdGenerator.generateId(dbConfig.getConnection(), "sedes", EntidadPrefix.SEDE, 3));
            }

            String sql = "INSERT INTO sedes (id, nombre, direccion, is_activa, telefono, ubigeo, tipo_sede, capacidad_almacen, administrador_id, is_bloqueada, motivo_bloqueo, fecha_bloqueo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dbConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, sede.getId());
                ps.setString(2, sede.getNombre());
                ps.setString(3, sede.getDireccion());
                ps.setInt(4, sede.getIsActiva());
                ps.setString(5, sede.getTelefono());
                ps.setString(6, sede.getUbigeo());
                ps.setString(7, sede.getTipoSede());
                ps.setInt(8, sede.getCapacidadAlmacen());
                ps.setString(9, sede.getAdministradorId());
                ps.setInt(10, sede.getIsBloqueada());
                ps.setString(11, sede.getMotivoBloqueo());
                ps.setTimestamp(12, sede.getFechaBloqueo() != null ? Timestamp.valueOf(sede.getFechaBloqueo()) : null);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar sede: " + e.getMessage(), e);
        }
        return sede;
    }

    @Override
    public Sede update(Sede sede) {
        String sql = "UPDATE sedes SET nombre = ?, direccion = ?, is_activa = ?, telefono = ?, ubigeo = ?, tipo_sede = ?, capacidad_almacen = ?, administrador_id = ?, is_bloqueada = ?, motivo_bloqueo = ?, fecha_bloqueo = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sede.getNombre());
            ps.setString(2, sede.getDireccion());
            ps.setInt(3, sede.getIsActiva());
            ps.setString(4, sede.getTelefono());
            ps.setString(5, sede.getUbigeo());
            ps.setString(6, sede.getTipoSede());
            ps.setInt(7, sede.getCapacidadAlmacen());
            ps.setString(8, sede.getAdministradorId());
            ps.setInt(9, sede.getIsBloqueada());
            ps.setString(10, sede.getMotivoBloqueo());
            ps.setTimestamp(11, sede.getFechaBloqueo() != null ? Timestamp.valueOf(sede.getFechaBloqueo()) : null);
            ps.setString(12, sede.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar sede: " + e.getMessage(), e);
        }
        return sede;
    }

    @Override
    public void toggleEstado(String id) {
        String sql = "UPDATE sedes SET is_activa = CASE WHEN is_activa = 1 THEN 0 ELSE 1 END WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al toggle estado de sede: " + e.getMessage(), e);
        }
    }

    @Override
    public void bloquear(String id, String motivo) {
        String sql = "UPDATE sedes SET is_bloqueada = 1, motivo_bloqueo = ?, fecha_bloqueo = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, motivo);
            ps.setTimestamp(2, Timestamp.valueOf(DateTimeProvider.now()));
            ps.setString(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al bloquear sede: " + e.getMessage(), e);
        }
    }

    @Override
    public void desbloquear(String id) {
        String sql = "UPDATE sedes SET is_bloqueada = 0, motivo_bloqueo = NULL, fecha_bloqueo = NULL WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al desbloquear sede: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isBloqueada(String id) {
        String sql = "SELECT is_bloqueada FROM sedes WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("is_bloqueada") == 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Usuario> findStaffBySede(String sedeId) {
        String sql = "SELECT u.*, r.nombre as rol_nombre FROM usuarios u " +
                     "JOIN roles r ON u.rol_id = r.id " +
                     "WHERE u.sede_id = ? AND u.is_activo = 1";
        List<Usuario> staff = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    staff.add(mapUsuario(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staff;
    }

    @Override
    public boolean assignUserToSede(String userId, String sedeId, String rolId) {
        String sql1 = "UPDATE usuarios SET sede_id = ?, rol_id = ? WHERE id = ?";
        String sql2 = "UPDATE sedes SET administrador_id = ? WHERE id = ?";
        try (Connection conn = dbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
                ps1.setString(1, sedeId);
                ps1.setString(2, rolId);
                ps1.setString(3, userId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setString(1, userId);
                ps2.setString(2, sedeId);
                ps2.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException("Error al asignar usuario a sede: " + e.getMessage(), e);
        }
    }

    @Override
    public int countTotalEmployees() {
        String sql = "SELECT COUNT(*) FROM usuarios";
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countEmployeesBySede(String sedeId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE sede_id = ? AND is_activo = 1";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countUsuariosBySede(String sedeId) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE sede_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countLotesBySede(String sedeId) {
        String sql = "SELECT COUNT(*) FROM lotes WHERE sede_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countMovimientosBySede(String sedeId) {
        String sql = "SELECT COUNT(*) FROM movimientos WHERE sede_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int countAtencionesBySede(String sedeId) {
        String sql = "SELECT COUNT(*) FROM atenciones WHERE sede_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sedeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM sedes WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar sede: " + e.getMessage(), e);
        }
    }

    private Sede mapSede(ResultSet rs) throws SQLException {
        Sede s = new Sede();
        s.setId(rs.getString("id"));
        s.setNombre(rs.getString("nombre"));
        s.setDireccion(rs.getString("direccion"));
        s.setIsActiva(rs.getInt("is_activa"));
        s.setTelefono(rs.getString("telefono"));
        try { s.setUbigeo(rs.getString("ubigeo")); } catch (SQLException ignored) {}
        try { s.setTipoSede(rs.getString("tipo_sede")); } catch (SQLException ignored) {}
        try { s.setCapacidadAlmacen(rs.getInt("capacidad_almacen")); } catch (SQLException ignored) {}
        try { s.setAdministradorId(rs.getString("administrador_id")); } catch (SQLException ignored) {}
        try { s.setAdministradorNombre(rs.getString("administrador_nombre")); } catch (SQLException ignored) {}
        try { s.setIsBloqueada(rs.getInt("is_bloqueada")); } catch (SQLException ignored) {}
        try { s.setMotivoBloqueo(rs.getString("motivo_bloqueo")); } catch (SQLException ignored) {}
        try {
            Timestamp ts = rs.getTimestamp("fecha_bloqueo");
            if (ts != null) s.setFechaBloqueo(ts.toLocalDateTime());
        } catch (SQLException ignored) {}
        return s;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getString("id"));
        u.setSedeId(rs.getString("sede_id"));
        u.setRolId(rs.getString("rol_id"));
        u.setTipoDocumento(rs.getString("tipo_documento"));
        u.setNumeroDocumento(rs.getString("numero_documento"));
        u.setNombres(rs.getString("nombres"));
        u.setApellidos(rs.getString("apellidos"));
        u.setIsActivo(rs.getInt("is_activo"));
        try { u.setRolNombre(rs.getString("rol_nombre")); } catch (SQLException ignored) {}
        return u;
    }
}
