package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.core.service.InventoryRules;
import com.utp.meditrackapp.core.session.SessionContext;
import com.utp.meditrackapp.core.util.IdGenerator;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LoteDAO extends JdbcDaoSupport {
    private static final int STOCK_MINIMO_DEFAULT = Integer.getInteger("inventory.defaultStockMinimo", 10);

    public Lote registrarIngreso(Lote lote) throws SQLException {
        validarLote(lote);

        try (Connection connection = getConnection()) {
            if (lote.getId() == null || lote.getId().isBlank()) {
                lote.setId(IdGenerator.generateId(EntidadPrefix.LOTE));
            }

            String sql = "INSERT INTO lotes (id, producto_id, sede_id, numero_lote, fecha_vencimiento, fecha_fabricacion, cantidad) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, lote.getId());
                statement.setString(2, lote.getProductoId());
                statement.setString(3, lote.getSedeId());
                statement.setString(4, lote.getNumeroLote());
                statement.setDate(5, Date.valueOf(lote.getFechaVencimiento()));
                statement.setDate(6, lote.getFechaFabricacion() == null ? null : Date.valueOf(lote.getFechaFabricacion()));
                statement.setInt(7, lote.getCantidad());
                statement.executeUpdate();
            }
        }

        return lote;
    }

    public Optional<Lote> buscarPorId(String id) throws SQLException {
        String sql = "SELECT id, producto_id, sede_id, numero_lote, fecha_vencimiento, fecha_fabricacion, cantidad FROM lotes WHERE id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapLote(resultSet));
                }
            }
        }

        return Optional.empty();
    }

    public List<Lote> listarPorSede(String sedeId) throws SQLException {
        return listarPorSedeYProducto(sedeId, null);
    }

    public List<Lote> listarLotesFefo(String sedeId, String productoId) throws SQLException {
        String sql = "SELECT id, producto_id, sede_id, numero_lote, fecha_vencimiento, fecha_fabricacion, cantidad FROM lotes WHERE sede_id = ? AND producto_id = ? AND cantidad > 0 ORDER BY fecha_vencimiento ASC, fecha_fabricacion ASC, numero_lote ASC";
        List<Lote> lotes = new ArrayList<>();

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sedeId);
            statement.setString(2, productoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lotes.add(mapLote(resultSet));
                }
            }
        }

        return lotes;
    }

    public int obtenerStockTotal(String sedeId, String productoId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(cantidad), 0) AS stock_total FROM lotes WHERE sede_id = ? AND producto_id = ?";
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, sedeId);
            statement.setString(2, productoId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("stock_total");
                }
            }
        }

        return 0;
    }

    public List<StockCriticoItem> obtenerStockCritico(String sedeId) throws SQLException {
        String stockMinimoExpr = hasStockMinimoColumn() ? "COALESCE(p.stock_minimo, ?)" : "?";
        String sql = "WITH stock_por_producto AS (" +
                " SELECT p.id AS producto_id, p.codigo_digemid, p.nombre AS producto_nombre, COALESCE(c.nombre, '') AS categoria_nombre," +
                " COALESCE(SUM(l.cantidad), 0) AS stock_actual," +
                " MAX(" + stockMinimoExpr + ") AS stock_minimo," +
                " MIN(l.fecha_vencimiento) AS fecha_vencimiento_mas_proxima," +
                " MIN(CASE WHEN l.fecha_vencimiento BETWEEN CAST(GETDATE() AS date) AND DATEADD(DAY, 30, CAST(GETDATE() AS date)) THEN l.fecha_vencimiento END) AS vence_30," +
                " MIN(CASE WHEN l.fecha_vencimiento BETWEEN DATEADD(DAY, 31, CAST(GETDATE() AS date)) AND DATEADD(DAY, 60, CAST(GETDATE() AS date)) THEN l.fecha_vencimiento END) AS vence_60" +
                " FROM productos p" +
                " LEFT JOIN categorias c ON c.id = p.categoria_id" +
                " LEFT JOIN lotes l ON l.producto_id = p.id AND l.sede_id = ? AND l.cantidad > 0" +
                " WHERE p.is_activo = 1" +
                " GROUP BY p.id, p.codigo_digemid, p.nombre, c.nombre" +
                ") SELECT * FROM stock_por_producto WHERE stock_actual < stock_minimo OR fecha_vencimiento_mas_proxima <= DATEADD(DAY, 60, CAST(GETDATE() AS date)) ORDER BY stock_actual ASC, fecha_vencimiento_mas_proxima ASC";

        List<StockCriticoItem> criticos = new ArrayList<>();

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            statement.setInt(index++, STOCK_MINIMO_DEFAULT);
            if (hasStockMinimoColumn()) {
                statement.setString(index++, sedeId);
            } else {
                statement.setString(index++, sedeId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    criticos.add(mapStockCritico(resultSet));
                }
            }
        }

        return criticos;
    }

    public List<StockCriticoItem> obtenerTopStockBajo(String sedeId, int limite) throws SQLException {
        List<StockCriticoItem> stockCritico = obtenerStockCritico(sedeId);
        return stockCritico.size() <= limite ? stockCritico : stockCritico.subList(0, limite);
    }

    public List<Lote> listarPorSedeActual() throws SQLException {
        return listarPorSede(SessionContext.requireSedeId());
    }

    private List<Lote> listarPorSedeYProducto(String sedeId, String productoId) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, producto_id, sede_id, numero_lote, fecha_vencimiento, fecha_fabricacion, cantidad FROM lotes WHERE sede_id = ?");
        if (productoId != null) {
            sql.append(" AND producto_id = ?");
        }
        sql.append(" ORDER BY fecha_vencimiento ASC, numero_lote ASC");

        List<Lote> lotes = new ArrayList<>();
        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            statement.setString(1, sedeId);
            if (productoId != null) {
                statement.setString(2, productoId);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    lotes.add(mapLote(resultSet));
                }
            }
        }

        return lotes;
    }

    private boolean hasStockMinimoColumn() {
        try (Connection connection = getConnection()) {
            return SchemaInspector.hasColumn(connection, "productos", "stock_minimo");
        } catch (SQLException error) {
            return false;
        }
    }

    private void validarLote(Lote lote) {
        if (lote == null) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }

        if (lote.getProductoId() == null || lote.getProductoId().isBlank()) {
            throw new IllegalArgumentException("El producto del lote es obligatorio.");
        }

        if (lote.getSedeId() == null || lote.getSedeId().isBlank()) {
            throw new IllegalArgumentException("La sede del lote es obligatoria.");
        }

        if (lote.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad del lote debe ser mayor a cero.");
        }

        InventoryRules.validarFechaVencimiento(lote.getFechaVencimiento());
    }

    private Lote mapLote(ResultSet resultSet) throws SQLException {
        Lote lote = new Lote();
        lote.setId(resultSet.getString("id"));
        lote.setProductoId(resultSet.getString("producto_id"));
        lote.setSedeId(resultSet.getString("sede_id"));
        lote.setNumeroLote(resultSet.getString("numero_lote"));
        Date fechaVencimiento = resultSet.getDate("fecha_vencimiento");
        if (fechaVencimiento != null) {
            lote.setFechaVencimiento(fechaVencimiento.toLocalDate());
        }
        Date fechaFabricacion = resultSet.getDate("fecha_fabricacion");
        if (fechaFabricacion != null) {
            lote.setFechaFabricacion(fechaFabricacion.toLocalDate());
        }
        lote.setCantidad(resultSet.getInt("cantidad"));
        return lote;
    }

    private StockCriticoItem mapStockCritico(ResultSet resultSet) throws SQLException {
        Date vencimiento = resultSet.getDate("fecha_vencimiento_mas_proxima");
        int diasParaVencer = 9999;
        if (vencimiento != null) {
            diasParaVencer = (int) (vencimiento.toLocalDate().toEpochDay() - LocalDate.now().toEpochDay());
        }

        return new StockCriticoItem(
                resultSet.getString("producto_id"),
                resultSet.getString("codigo_digemid"),
                resultSet.getString("producto_nombre"),
                resultSet.getString("categoria_nombre"),
                resultSet.getInt("stock_actual"),
                resultSet.getInt("stock_minimo"),
                vencimiento == null ? null : vencimiento.toLocalDate(),
                diasParaVencer
        );
    }
}