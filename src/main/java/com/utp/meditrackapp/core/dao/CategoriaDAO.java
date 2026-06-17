package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.Categoria;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAO extends JdbcDaoSupport {

    private boolean hasIsActivo(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT TOP(1) is_activo FROM categorias");
             ResultSet rs = ps.executeQuery()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Categoria> listarTodas() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection conn = getConnection()) {
            boolean activo = hasIsActivo(conn);
            String sql = activo
                ? "SELECT id, nombre, is_activo FROM categorias ORDER BY id ASC"
                : "SELECT id, nombre FROM categorias ORDER BY id ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categorias.add(mapCategoria(rs, activo));
                }
            }
        }
        return categorias;
    }

    public Optional<Categoria> buscarPorId(String id) throws SQLException {
        try (Connection conn = getConnection()) {
            boolean activo = hasIsActivo(conn);
            String sql = activo
                ? "SELECT id, nombre, is_activo FROM categorias WHERE id = ?"
                : "SELECT id, nombre FROM categorias WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapCategoria(rs, activo));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Categoria> buscarPorNombre(String nombre) throws SQLException {
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, nombre FROM categorias WHERE nombre = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nombre);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return Optional.of(mapCategoria(rs, false));
                }
            }
        }
        return Optional.empty();
    }

    public Categoria crear(Categoria categoria) throws SQLException {
        validarCategoria(categoria);
        try (Connection conn = getConnection()) {
            if (buscarPorNombre(conn, categoria.getNombre()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una categoria con ese nombre.");
            }
            if (categoria.getId() == null || categoria.getId().isBlank()) {
                categoria.setId(IdGenerator.generateId(conn, "categorias", EntidadPrefix.CATEGORIA, 3));
            }
            boolean activo = hasIsActivo(conn);
            if (activo) {
                String sql = "INSERT INTO categorias (id, nombre, is_activo) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, categoria.getId());
                    ps.setString(2, categoria.getNombre().trim());
                    ps.setInt(3, 1);
                    ps.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO categorias (id, nombre) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, categoria.getId());
                    ps.setString(2, categoria.getNombre().trim());
                    ps.executeUpdate();
                }
            }
        }
        return categoria;
    }

    public Categoria actualizar(Categoria categoria) throws SQLException {
        validarCategoria(categoria);
        try (Connection conn = getConnection()) {
            if (buscarPorNombre(conn, categoria.getNombre())
                    .filter(existing -> !existing.getId().equals(categoria.getId()))
                    .isPresent()) {
                throw new IllegalArgumentException("Ya existe otra categoria con ese nombre.");
            }
            String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, categoria.getNombre().trim());
                ps.setString(2, categoria.getId());
                ps.executeUpdate();
            }
        }
        return categoria;
    }

    public void toggleEstado(String id) throws SQLException {
        try (Connection conn = getConnection()) {
            if (!hasIsActivo(conn)) {
                throw new SQLException("La columna is_activo no existe. Ejecute la migración 004:\nALTER TABLE categorias ADD is_activo INT DEFAULT 1;");
            }
            String sql = "UPDATE categorias SET is_activo = CASE WHEN is_activo = 1 THEN 0 ELSE 1 END WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
        }
    }

    public void eliminar(String id) throws SQLException {
        try (Connection conn = getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM productos WHERE categoria_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new SQLException("No se puede eliminar la categoría porque tiene productos asignados.");
                    }
                }
            }
            String deleteSql = "DELETE FROM categorias WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, id);
                ps.executeUpdate();
            }
        }
    }

    private Optional<Categoria> buscarPorNombre(Connection connection, String nombre) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE nombre = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new Categoria(resultSet.getString("id"), resultSet.getString("nombre")));
                }
            }
        }
        return Optional.empty();
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("La categoria es obligatoria.");
        }
        if (categoria.getNombre() == null || categoria.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoria es obligatorio.");
        }
    }

    private Categoria mapCategoria(ResultSet rs, boolean hasActivo) throws SQLException {
        Categoria cat = new Categoria();
        cat.setId(rs.getString("id"));
        cat.setNombre(rs.getString("nombre"));
        if (hasActivo) {
            Object val = rs.getObject("is_activo");
            cat.setIsActivo(val == null ? 1 : rs.getInt("is_activo"));
        } else {
            cat.setIsActivo(1);
        }
        return cat;
    }
}
