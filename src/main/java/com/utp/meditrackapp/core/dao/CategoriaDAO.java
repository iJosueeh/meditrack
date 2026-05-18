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

    public List<Categoria> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM categorias ORDER BY nombre";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categorias.add(mapCategoria(resultSet));
            }
        }

        return categorias;
    }

    public Optional<Categoria> buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCategoria(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Categoria> buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE nombre = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCategoria(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public Categoria crear(Categoria categoria) throws SQLException {
        validarCategoria(categoria);

        try (Connection connection = getConnection()) {
            if (buscarPorNombre(connection, categoria.getNombre()).isPresent()) {
                throw new IllegalArgumentException("Ya existe una categoria con ese nombre.");
            }

            if (categoria.getId() == null || categoria.getId().isBlank()) {
                categoria.setId(IdGenerator.generateId(EntidadPrefix.CATEGORIA));
            }

            String sql = "INSERT INTO categorias (id, nombre) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, categoria.getId());
                statement.setString(2, categoria.getNombre().trim());
                statement.executeUpdate();
            }
        }

        return categoria;
    }

    public Categoria actualizar(Categoria categoria) throws SQLException {
        validarCategoria(categoria);

        try (Connection connection = getConnection()) {
            if (buscarPorNombre(connection, categoria.getNombre())
                    .filter(existing -> !existing.getId().equals(categoria.getId()))
                    .isPresent()) {
                throw new IllegalArgumentException("Ya existe otra categoria con ese nombre.");
            }

            String sql = "UPDATE categorias SET nombre = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, categoria.getNombre().trim());
                statement.setString(2, categoria.getId());
                statement.executeUpdate();
            }
        }

        return categoria;
    }

    public void eliminar(String id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
        }
    }

    private Optional<Categoria> buscarPorNombre(Connection connection, String nombre) throws SQLException {
        String sql = "SELECT id, nombre FROM categorias WHERE nombre = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCategoria(resultSet));
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

    private Categoria mapCategoria(ResultSet resultSet) throws SQLException {
        return new Categoria(resultSet.getString("id"), resultSet.getString("nombre"));
    }
}