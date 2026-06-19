package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.ports.out.CategoriaRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoriaRepository implements CategoriaRepository {

    @Override
    public Optional<Categoria> findById(String id) {
        String sql = "SELECT id, nombre, is_activo FROM categorias WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCategoria(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Categoria> findAll() {
        String sql = "SELECT id, nombre, is_activo FROM categorias ORDER BY nombre";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Categoria> categorias = new ArrayList<>();
            while (rs.next()) {
                categorias.add(mapResultSetToCategoria(rs));
            }
            return categorias;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Categoria save(Categoria categoria) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String id = categoria.getId();
            if (id == null || id.isBlank()) {
                id = IdGenerator.generateId(conn, "categorias", EntidadPrefix.CATEGORIA, 6);
                categoria.setId(id);
            }

            String sql = "INSERT INTO categorias (id, nombre, is_activo) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, categoria.getNombre());
                ps.setInt(3, categoria.getIsActivo());
                ps.executeUpdate();
            }
            return categoria;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Categoria update(Categoria categoria) {
        String sql = "UPDATE categorias SET nombre = ?, is_activo = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoria.getNombre());
            ps.setInt(2, categoria.getIsActivo());
            ps.setString(3, categoria.getId());
            ps.executeUpdate();
            return categoria;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void toggleEstado(String id) {
        String sql = "UPDATE categorias SET is_activo = CASE WHEN is_activo = 1 THEN 0 ELSE 1 END WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(String id) {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int countProductosByCategoria(String categoriaId) {
        String sql = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, categoriaId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Categoria mapResultSetToCategoria(ResultSet rs) throws SQLException {
        Categoria c = new Categoria();
        c.setId(rs.getString("id"));
        c.setNombre(rs.getString("nombre"));
        c.setIsActivo(rs.getInt("is_activo"));
        return c;
    }
}
