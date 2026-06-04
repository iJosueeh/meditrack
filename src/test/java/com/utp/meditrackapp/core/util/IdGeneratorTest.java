package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
    @DisplayName("Debería generar un ID con el prefijo correcto (Legacy)")
    void shouldGenerateIdWithCorrectPrefix() {
        @SuppressWarnings("deprecation")
        String userId = IdGenerator.generateId(EntidadPrefix.USUARIO);
        assertTrue(userId.startsWith("USR-"), "El ID de usuario debería empezar con USR-");

        @SuppressWarnings("deprecation")
        String personalId = IdGenerator.generateId(EntidadPrefix.PERSONAL);
        assertTrue(personalId.startsWith("PER-"), "El ID de personal debería empezar con PER-");
    }

    @Test
    @DisplayName("Debería generar IDs únicos (Legacy)")
    void shouldGenerateUniqueIds() {
        @SuppressWarnings("deprecation")
        String id1 = IdGenerator.generateId(EntidadPrefix.USUARIO);
        @SuppressWarnings("deprecation")
        String id2 = IdGenerator.generateId(EntidadPrefix.USUARIO);

        assertNotEquals(id1, id2, "Los IDs generados deberían ser únicos");
    }

    @Test
    @DisplayName("El ID generado debería tener la longitud esperada (Legacy)")
    void shouldHaveCorrectLength() {
        @SuppressWarnings("deprecation")
        String id = IdGenerator.generateId(EntidadPrefix.USUARIO);
        // "USR" (3) + "-" (1) + shortId (8) = 12 caracteres
        assertEquals(12, id.length(), "El ID debería tener exactamente 12 caracteres");
    }

    @Test
    @DisplayName("Debería generar IDs secuenciales globales (DB)")
    void shouldGenerateGlobalSequentialIds() throws java.sql.SQLException {
        com.utp.meditrackapp.core.config.DatabaseConfig db = com.utp.meditrackapp.core.config.DatabaseConfig.getInstance();
        org.junit.jupiter.api.Assumptions.assumeTrue(db.isReachable());

        try (java.sql.Connection conn = db.getConnection()) {
            String id1 = IdGenerator.generateId(conn, "categorias", EntidadPrefix.CATEGORIA, 3);
            String id2 = IdGenerator.generateId(conn, "categorias", EntidadPrefix.CATEGORIA, 3);
            
            assertNotNull(id1);
            assertNotNull(id2);
            assertTrue(id1.startsWith("CAT-"));
            
            // Extract numbers
            int val1 = Integer.parseInt(id1.split("-")[1]);
            int val2 = Integer.parseInt(id2.split("-")[1]);
            
            // Since we don't know the exact count in DB, we at least check consistency
            // If they were generated in the same test run, id2 might not be val1+1 if id1 wasn't inserted.
            // But generateId queries the DB. If we don't insert val1, val2 will be the same as val1.
            assertEquals(val1, val2, "Sin inserción, el ID generado debería ser el mismo");
        }
    }

    @Test
    @DisplayName("Debería generar IDs secuenciales por sede (DB)")
    void shouldGenerateSedeDependentIds() throws java.sql.SQLException {
        com.utp.meditrackapp.core.config.DatabaseConfig db = com.utp.meditrackapp.core.config.DatabaseConfig.getInstance();
        org.junit.jupiter.api.Assumptions.assumeTrue(db.isReachable());

        try (java.sql.Connection conn = db.getConnection()) {
            String sedeId = "SED-999"; // Sede ficticia
            String id = IdGenerator.generateSedeDependentId(conn, "usuarios", EntidadPrefix.USUARIO, sedeId, 4);
            
            assertNotNull(id);
            assertTrue(id.startsWith("USR-999-"), "El ID debería empezar con el prefijo y el número de sede");
            assertTrue(id.endsWith("0001"), "Para una sede nueva sin datos, debería empezar en 0001");
        }
    }
}
