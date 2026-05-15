package com.utp.meditrackapp.core.config;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseIntegrationTest {

    @BeforeEach
    public void setUp() {
        Assumptions.assumeTrue(DatabaseConfig.getInstance().isReachable(), 
            "Abortando test: Base de datos no disponible");
    }

    @Test
    public void testConnection() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DatabaseConfig.getInstance().getConnection()) {
                assertNotNull(conn, "La conexión no debe ser nula");
                assertFalse(conn.isClosed(), "La conexión debe estar abierta");
            }
        });
    }

    @Test
    public void testTablesExist() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DatabaseConfig.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {
                
                String[] tables = {"roles", "sedes", "usuarios", "pacientes", "categorias", "productos", "lotes"};
                
                for (String table : tables) {
                    ResultSet rs = stmt.executeQuery("SELECT TOP 1 * FROM " + table);
                    assertNotNull(rs, "El ResultSet para la tabla " + table + " no debe ser nulo");
                }
            }
        });
    }

    @Test
    public void testSeedData() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DatabaseConfig.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {
                
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM usuarios WHERE numero_documento = '12345678'");
                assertTrue(rs.next());
                assertEquals(1, rs.getInt("total"), "Debe existir al menos el usuario admin de prueba");
            }
        });
    }
}
