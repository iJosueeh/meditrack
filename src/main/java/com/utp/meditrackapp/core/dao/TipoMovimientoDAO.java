package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.models.entity.TipoMovimiento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoMovimientoDAO extends JdbcDaoSupport {

    public List<TipoMovimiento> listarTodas() throws SQLException {
        String sql = "SELECT id, nombre FROM tipos_movimiento ORDER BY nombre";
        List<TipoMovimiento> lista = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new TipoMovimiento(
                    rs.getString("id"),
                    rs.getString("nombre")
                ));
            }
        }
        return lista;
    }
}
