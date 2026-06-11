package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.Producto;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDAO extends JdbcDaoSupport {
    private static final int STOCK_MINIMO_DEFAULT = Integer.getInteger("inventory.defaultStockMinimo", 10);
    private volatile Boolean hasStockMinimoColumn;
    private volatile Boolean hasPrecioUnitarioColumn;

    public List<Producto> listarTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(selectProductoSql(connection) + " ORDER BY p.nombre"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                productos.add(mapProducto(resultSet, connection));
            }
        }

        return productos;
    }

    public List<Producto> listarActivos() throws SQLException {
        List<Producto> productos = new ArrayList<>();

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(selectProductoSql(connection) + " WHERE p.is_activo = 1 ORDER BY p.nombre"); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                productos.add(mapProducto(resultSet, connection));
            }
        }

        return productos;
    }

    public Optional<Producto> buscarPorId(String id) throws SQLException {
        try (Connection connection = getConnection()) {
            String sql = selectProductoSql(connection) + " WHERE p.id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, id);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapProducto(resultSet, connection));
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Optional<Producto> buscarPorCodigoDigemid(String codigoDigemid) throws SQLException {
        try (Connection connection = getConnection()) {
            String sql = selectProductoSql(connection) + " WHERE p.codigo_digemid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, codigoDigemid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapProducto(resultSet, connection));
                    }
                }
            }
        }

        return Optional.empty();
    }

    public Producto crear(Producto producto) throws SQLException {
        validarProducto(producto);

        try (Connection connection = getConnection()) {
            if (existeCodigoDigemid(connection, producto.getCodigoDigemid(), null)) {
                throw new IllegalArgumentException("El codigo DIGEMID ya existe.");
            }

            if (producto.getId() == null || producto.getId().isBlank()) {
                producto.setId(IdGenerator.generateId(connection, "productos", EntidadPrefix.PRODUCTO, 5));
            }

            StringBuilder sql = new StringBuilder("INSERT INTO productos (id, categoria_id, codigo_digemid, nombre, detalle, unidad_medida, is_activo");
            StringBuilder values = new StringBuilder("VALUES (?, ?, ?, ?, ?, ?, ?");
            
            if (hasStockMinimoColumn(connection)) {
                sql.append(", stock_minimo");
                values.append(", ?");
            }
            if (hasPrecioUnitarioColumn(connection)) {
                sql.append(", precio_unitario");
                values.append(", ?");
            }
            sql.append(") ").append(values).append(")");

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                bindProducto(statement, producto, hasStockMinimoColumn(connection), hasPrecioUnitarioColumn(connection), true);
                statement.executeUpdate();
            }
        }

        return producto;
    }

    public Producto actualizar(Producto producto) throws SQLException {
        validarProducto(producto);

        try (Connection connection = getConnection()) {
            if (existeCodigoDigemid(connection, producto.getCodigoDigemid(), producto.getId())) {
                throw new IllegalArgumentException("El codigo DIGEMID ya existe.");
            }

            StringBuilder sql = new StringBuilder("UPDATE productos SET categoria_id = ?, codigo_digemid = ?, nombre = ?, detalle = ?, unidad_medida = ?, is_activo = ?");
            
            if (hasStockMinimoColumn(connection)) {
                sql.append(", stock_minimo = ?");
            }
            if (hasPrecioUnitarioColumn(connection)) {
                sql.append(", precio_unitario = ?");
            }
            sql.append(" WHERE id = ?");

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                bindProducto(statement, producto, hasStockMinimoColumn(connection), hasPrecioUnitarioColumn(connection), false);
                statement.executeUpdate();
            }
        }

        return producto;
    }

    public void desactivar(String id) throws SQLException {
        String sql = "UPDATE productos SET is_activo = 0 WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            statement.executeUpdate();
        }
    }

    public boolean existeCodigoDigemid(String codigoDigemid) throws SQLException {
        try (Connection connection = getConnection()) {
            return existeCodigoDigemid(connection, codigoDigemid, null);
        }
    }

    private boolean existeCodigoDigemid(Connection connection, String codigoDigemid, String excludedId) throws SQLException {
        String sql = excludedId == null
                ? "SELECT 1 FROM productos WHERE codigo_digemid = ?"
                : "SELECT 1 FROM productos WHERE codigo_digemid = ? AND id <> ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, codigoDigemid);
            if (excludedId != null) {
                statement.setString(2, excludedId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String selectProductoSql(Connection connection) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT p.id, p.categoria_id, p.codigo_digemid, p.nombre, p.detalle, p.unidad_medida, p.is_activo, ");
        
        if (hasStockMinimoColumn(connection)) sql.append("p.stock_minimo, ");
        else sql.append("CAST(NULL AS INT) AS stock_minimo, ");

        if (hasPrecioUnitarioColumn(connection)) sql.append("p.precio_unitario, ");
        else sql.append("CAST(0 AS DECIMAL(18,2)) AS precio_unitario, ");

        sql.append("c.nombre AS categoria_nombre FROM productos p LEFT JOIN categorias c ON c.id = p.categoria_id");
        return sql.toString();
    }

    private boolean hasStockMinimoColumn(Connection connection) throws SQLException {
        Boolean cached = hasStockMinimoColumn;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (hasStockMinimoColumn == null) {
                hasStockMinimoColumn = SchemaInspector.hasColumn(connection, "productos", "stock_minimo");
            }
            return hasStockMinimoColumn;
        }
    }

    private boolean hasPrecioUnitarioColumn(Connection connection) throws SQLException {
        Boolean cached = hasPrecioUnitarioColumn;
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            if (hasPrecioUnitarioColumn == null) {
                hasPrecioUnitarioColumn = SchemaInspector.hasColumn(connection, "productos", "precio_unitario");
            }
            return hasPrecioUnitarioColumn;
        }
    }

    private void bindProducto(PreparedStatement statement, Producto producto, boolean includeStockMinimo, boolean includePrecioUnitario, boolean isInsert) throws SQLException {
        int index = 1;
        if (isInsert) {
            statement.setString(index++, producto.getId());
        }

        statement.setString(index++, producto.getCategoriaId());
        statement.setString(index++, producto.getCodigoDigemid().trim());
        statement.setString(index++, producto.getNombre().trim());
        statement.setString(index++, producto.getDetalle());
        statement.setString(index++, producto.getUnidadMedida());
        statement.setInt(index++, producto.getIsActivo());

        if (includeStockMinimo) {
            statement.setObject(index++, producto.getStockMinimo() == null ? STOCK_MINIMO_DEFAULT : producto.getStockMinimo());
        }
        
        if (includePrecioUnitario) {
            statement.setObject(index++, producto.getPrecioUnitario() == null ? 0.0 : producto.getPrecioUnitario());
        }

        if (!isInsert) {
            statement.setString(index, producto.getId());
        }
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto es obligatorio.");
        }

        if (producto.getCodigoDigemid() == null || producto.getCodigoDigemid().isBlank()) {
            throw new IllegalArgumentException("El codigo DIGEMID es obligatorio.");
        }

        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }

        if (producto.getCategoriaId() == null || producto.getCategoriaId().isBlank()) {
            throw new IllegalArgumentException("La categoría es obligatoria.");
        }
    }

    private Producto mapProducto(ResultSet resultSet, Connection connection) throws SQLException {
        Producto producto = new Producto();
        producto.setId(resultSet.getString("id"));
        producto.setCategoriaId(resultSet.getString("categoria_id"));
        producto.setCodigoDigemid(resultSet.getString("codigo_digemid"));
        producto.setNombre(resultSet.getString("nombre"));
        producto.setDetalle(resultSet.getString("detalle"));
        producto.setUnidadMedida(resultSet.getString("unidad_medida"));
        producto.setIsActivo(resultSet.getInt("is_activo"));
        producto.setStockMinimo(hasStockMinimoColumn(connection) ? resultSet.getObject("stock_minimo", Integer.class) : STOCK_MINIMO_DEFAULT);
        producto.setPrecioUnitario(hasPrecioUnitarioColumn(connection) ? resultSet.getDouble("precio_unitario") : 0.0);
        try {
            producto.setCategoriaNombre(resultSet.getString("categoria_nombre"));
        } catch (SQLException e) {
            // Field not present in ResultSet
        }
        return producto;
    }
}