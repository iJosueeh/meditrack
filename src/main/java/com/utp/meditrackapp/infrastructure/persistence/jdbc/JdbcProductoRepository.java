package com.utp.meditrackapp.infrastructure.persistence.jdbc;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.ports.out.ProductoRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcProductoRepository implements ProductoRepository {

    private static final String SELECT_BASE = "SELECT p.id, p.categoria_id, p.codigo_digemid, p.nombre, p.detalle, " +
        "p.unidad_medida, p.is_activo, p.stock_minimo, p.precio_unitario, c.nombre AS categoria_nombre " +
        "FROM productos p LEFT JOIN categorias c ON c.id = p.categoria_id";

    @Override
    public Optional<Producto> findById(String id) {
        String sql = SELECT_BASE + " WHERE p.id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProducto(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Producto> findAll() {
        String sql = SELECT_BASE + " ORDER BY p.nombre";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Producto> findActivos() {
        String sql = SELECT_BASE + " WHERE p.is_activo = 1 ORDER BY p.nombre";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Producto> productos = new ArrayList<>();
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
            return productos;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Producto save(Producto producto) {
        try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
            String id = producto.getId();
            if (id == null || id.isBlank()) {
                id = IdGenerator.generateId(conn, "productos", EntidadPrefix.PRODUCTO, 6);
                producto.setId(id);
            }

            String sql = "INSERT INTO productos (id, categoria_id, codigo_digemid, nombre, detalle, unidad_medida, " +
                "is_activo, stock_minimo, precio_unitario) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                ps.setString(2, producto.getCategoriaId());
                ps.setString(3, producto.getCodigoDigemid());
                ps.setString(4, producto.getNombre());
                ps.setString(5, producto.getDetalle());
                ps.setString(6, producto.getUnidadMedida());
                ps.setInt(7, producto.getIsActivo());
                if (producto.getStockMinimo() != null) {
                    ps.setInt(8, producto.getStockMinimo());
                } else {
                    ps.setNull(8, java.sql.Types.INTEGER);
                }
                if (producto.getPrecioUnitario() != null) {
                    ps.setDouble(9, producto.getPrecioUnitario());
                } else {
                    ps.setNull(9, java.sql.Types.DOUBLE);
                }
                ps.executeUpdate();
            }
            return producto;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Producto update(Producto producto) {
        String sql = "UPDATE productos SET categoria_id = ?, codigo_digemid = ?, nombre = ?, detalle = ?, " +
            "unidad_medida = ?, is_activo = ?, stock_minimo = ?, precio_unitario = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, producto.getCategoriaId());
            ps.setString(2, producto.getCodigoDigemid());
            ps.setString(3, producto.getNombre());
            ps.setString(4, producto.getDetalle());
            ps.setString(5, producto.getUnidadMedida());
            ps.setInt(6, producto.getIsActivo());
            if (producto.getStockMinimo() != null) {
                ps.setInt(7, producto.getStockMinimo());
            } else {
                ps.setNull(7, java.sql.Types.INTEGER);
            }
            if (producto.getPrecioUnitario() != null) {
                ps.setDouble(8, producto.getPrecioUnitario());
            } else {
                ps.setNull(8, java.sql.Types.DOUBLE);
            }
            ps.setString(9, producto.getId());
            ps.executeUpdate();
            return producto;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void desactivar(String id) {
        String sql = "UPDATE productos SET is_activo = 0 WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean existeCodigoDigemid(String codigoDigemid) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo_digemid = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, codigoDigemid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto p = new Producto();
        p.setId(rs.getString("id"));
        p.setCategoriaId(rs.getString("categoria_id"));
        p.setCodigoDigemid(rs.getString("codigo_digemid"));
        p.setNombre(rs.getString("nombre"));
        p.setDetalle(rs.getString("detalle"));
        p.setUnidadMedida(rs.getString("unidad_medida"));
        p.setIsActivo(rs.getInt("is_activo"));
        int stockMinimo = rs.getInt("stock_minimo");
        if (!rs.wasNull()) {
            p.setStockMinimo(stockMinimo);
        }
        double precioUnitario = rs.getDouble("precio_unitario");
        if (!rs.wasNull()) {
            p.setPrecioUnitario(precioUnitario);
        }
        p.setCategoriaNombre(rs.getString("categoria_nombre"));
        return p;
    }
}
