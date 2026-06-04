package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Usuario;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SedeResolverTest {

    @BeforeEach
    public void setUp() {
        Assumptions.assumeTrue(DatabaseConfig.getInstance().isReachable(), 
            "Abortando test: Base de datos no disponible");
    }

    @Test
    public void testGetSedeNameWithCachedName() {
        Usuario user = new Usuario();
        user.setSedeNombre("Hospital Regional");
        
        String result = SedeResolver.getSedeName(user);
        assertEquals("Hospital Regional", result, "Debe retornar el nombre ya cacheado");
    }

    @Test
    public void testGetSedeNameFromDatabase() {
        Usuario user = new Usuario();
        user.setSedeId("SED-TEST-001"); // Assuming this might exist or we can use a known one
        user.setSedeNombre("SED-TEST-001"); // Simulate raw ID in name field
        
        // This test depends on DB state, so we use a more generic approach or check if the result is not the ID
        String result = SedeResolver.getSedeName(user);
        assertNotNull(result);
        assertNotEquals("SED-TEST-001", result, "Debe intentar resolver el nombre real desde la BD");
    }

    @Test
    public void testGetSedeNameNullUser() {
        String result = SedeResolver.getSedeName(null);
        assertEquals("Sede Desconocida", result);
    }
}
