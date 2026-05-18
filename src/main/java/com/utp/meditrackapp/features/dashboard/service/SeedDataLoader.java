package com.utp.meditrackapp.features.dashboard.service;

import com.utp.meditrackapp.core.config.DatabaseConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SeedDataLoader {
    private final DatabaseConfig dbConfig = DatabaseConfig.getInstance();

    /**
     * Verifica si hay al menos `minProductos` y `minLotes` en la base de datos.
     * Si no, intenta ejecutar el script `database/seed_data.sql`.
     * Retorna true si hay datos suficientes o se cargaron correctamente.
     */
    public boolean ensureSeedData(int minProductos, int minLotes) {
        if (!dbConfig.isReachable()) {
            System.err.println("[SEED] Base de datos no alcanzable, no se pueden cargar datos.");
            return false;
        }

        try (Connection conn = dbConfig.getConnection(); Statement stmt = conn.createStatement()) {
            int productos = queryCount(stmt, "SELECT COUNT(*) as c FROM productos");
            int lotes = queryCount(stmt, "SELECT COUNT(*) as c FROM lotes");

            if (productos >= minProductos && lotes >= minLotes) {
                System.out.println("[SEED] Ya hay datos suficientes (productos=" + productos + ", lotes=" + lotes + ").");
                return true;
            }

            System.out.println("[SEED] Datos insuficientes. Productos=" + productos + ", Lotes=" + lotes + ". Cargando seed_data.sql...");
            Path script = Path.of("database", "seed_data.sql");
            if (!Files.exists(script)) {
                System.err.println("[SEED] No se encontró database/seed_data.sql");
                return false;
            }

            String sql = Files.readString(script);
            // SQL Server scripts often use GO as batch separator — dividir por líneas con GO
            String[] batches = sql.split("(?m)^GO\\s*$");
            conn.setAutoCommit(false);
            try {
                for (String batch : batches) {
                    String s = batch.trim();
                    if (s.isEmpty()) continue;
                    // Ejecutar cada statement individualmente
                    try {
                        stmt.execute(s);
                    } catch (SQLException e) {
                        System.err.println("[SEED] Error ejecutando batch: " + e.getMessage());
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            productos = queryCount(stmt, "SELECT COUNT(*) as c FROM productos");
            lotes = queryCount(stmt, "SELECT COUNT(*) as c FROM lotes");
            System.out.println("[SEED] Después de cargar: productos=" + productos + ", lotes=" + lotes + ".");
            return productos >= minProductos && lotes >= minLotes;
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private int queryCount(Statement stmt, String sql) throws SQLException {
        try (ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("c");
        }
        return 0;
    }
}
