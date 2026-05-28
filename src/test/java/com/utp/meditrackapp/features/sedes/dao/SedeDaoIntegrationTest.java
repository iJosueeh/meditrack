package com.utp.meditrackapp.features.sedes.dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.models.entity.Sede;
import com.utp.meditrackapp.features.sedes.models.SedeDetalleDTO;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SedeDaoIntegrationTest {

    private final SedeDAO sedeDAO = new SedeDAO();

    @BeforeEach
    public void setUp() {
        Assumptions.assumeTrue(DatabaseConfig.getInstance().isReachable(), 
            "Abortando test: Base de datos no disponible");
    }

    @Test
    public void testGetAllWithDetails() throws SQLException {
        List<SedeDetalleDTO> list = sedeDAO.getAllWithDetails();
        assertNotNull(list);
        assertFalse(list.isEmpty(), "Debería haber al menos la sede central");
        
        SedeDetalleDTO central = list.stream()
                .filter(s -> s.getNombre().contains("Central"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(central);
        assertNotNull(central.getAdministrador());
        assertTrue(central.getTotalEmpleados() >= 0);
    }

    @Test
    public void testSaveAndUpdateSede() throws SQLException {
        String testId = "SED-TEST";
        Sede testSede = new Sede(testId, "Sede Test Unit", "Calle Falsa 123", 1);
        
        // Limpieza previa si existe
        // (Nota: En un ambiente real usaríamos transacciones o un DB de test)
        
        // Save
        boolean saved = sedeDAO.save(testSede);
        assertTrue(saved);

        // Update
        testSede.setNombre("Sede Test Editada");
        boolean updated = sedeDAO.update(testSede);
        assertTrue(updated);

        // Verify
        List<SedeDetalleDTO> list = sedeDAO.getAllWithDetails();
        Sede found = list.stream().filter(s -> s.getId().equals(testId)).findFirst().orElse(null);
        assertNotNull(found);
        assertEquals("Sede Test Editada", found.getNombre());
    }

    @Test
    public void testGetTotalEmployeesGlobal() throws SQLException {
        int total = sedeDAO.getTotalEmployeesGlobal();
        assertTrue(total > 0, "Debería haber al menos el usuario admin");
    }
}
