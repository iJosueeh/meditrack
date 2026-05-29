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
        
        // Limpieza de datos de prueba previos
        try (java.sql.Connection conn = DatabaseConfig.getInstance().getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement("DELETE FROM sedes WHERE id = 'SED-TEST'")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        SedeDetalleDTO testSede = new SedeDetalleDTO(testId, "Sede Test Unit", "Calle Falsa 123", 1);
        testSede.setTelefono("999888777");
        
        // Limpieza previa no incluida por brevedad, asumimos ambiente controlado
        
        // Save
        boolean saved = sedeDAO.save(testSede);
        assertTrue(saved);

        // Update
        testSede.setNombre("Sede Test Editada");
        testSede.setTelefono("911222333");
        boolean updated = sedeDAO.update(testSede);
        assertTrue(updated);

        // Verify
        List<SedeDetalleDTO> list = sedeDAO.getAllWithDetails();
        SedeDetalleDTO found = list.stream().filter(s -> s.getId().equals(testId)).findFirst().orElse(null);
        
        assertNotNull(found);
        assertEquals("Sede Test Editada", found.getNombre());
        assertEquals("911222333", found.getTelefono());
    }

    @Test
    public void testGetTotalEmployeesGlobal() throws SQLException {
        int total = sedeDAO.getTotalEmployeesGlobal();
        assertTrue(total > 0, "Debería haber al menos el usuario admin");
    }
}
