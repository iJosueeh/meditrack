package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IdGeneratorTest {

    @Test
    @DisplayName("Debería generar un ID con el prefijo correcto")
    void shouldGenerateIdWithCorrectPrefix() {
        String userId = IdGenerator.generateId(EntidadPrefix.USUARIO);
        assertTrue(userId.startsWith("USR-"), "El ID de usuario debería empezar con USR-");

        String personalId = IdGenerator.generateId(EntidadPrefix.PERSONAL);
        assertTrue(personalId.startsWith("PER-"), "El ID de personal debería empezar con PER-");
    }

    @Test
    @DisplayName("Debería generar IDs únicos")
    void shouldGenerateUniqueIds() {
        String id1 = IdGenerator.generateId(EntidadPrefix.USUARIO);
        String id2 = IdGenerator.generateId(EntidadPrefix.USUARIO);

        assertNotEquals(id1, id2, "Los IDs generados deberían ser únicos");
    }

    @Test
    @DisplayName("El ID generado debería tener la longitud esperada")
    void shouldHaveCorrectLength() {
        String id = IdGenerator.generateId(EntidadPrefix.USUARIO);
        // "USR" (3) + "-" (1) + shortId (8) = 12 caracteres
        assertEquals(12, id.length(), "El ID debería tener exactamente 12 caracteres");
    }
}
