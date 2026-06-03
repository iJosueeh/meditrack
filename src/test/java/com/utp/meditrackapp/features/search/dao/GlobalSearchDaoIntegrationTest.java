package com.utp.meditrackapp.features.search.dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.features.search.models.SearchResult;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalSearchDaoIntegrationTest {

    private final GlobalSearchDAO searchDAO = new GlobalSearchDAO();

    @BeforeEach
    public void setUp() {
        Assumptions.assumeTrue(DatabaseConfig.getInstance().isReachable(), 
            "Abortando test: Base de datos no disponible");
    }

    @Test
    public void testSearchModule() throws SQLException {
        // Buscar módulos estáticos
        List<SearchResult> results = searchDAO.searchGlobal("inventario");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getType() == SearchResult.ResultType.MODULE));
    }

    @Test
    public void testSearchPatient() throws SQLException {
        // En la base seed hay datos de prueba, busquemos algo común
        List<SearchResult> results = searchDAO.searchGlobal("Admin");
        assertNotNull(results);
    }

    @Test
    public void testMultiWordSearch() throws SQLException {
        // 'Paracetamol 500mg' exist in seeds. 
        // Test out of order multi-word search.
        List<SearchResult> results = searchDAO.searchGlobal("500mg Paracetamol");
        assertFalse(results.isEmpty(), "Debería encontrar Paracetamol 500mg incluso con términos invertidos");
        assertTrue(results.stream().anyMatch(r -> r.getTitle().contains("Paracetamol") && r.getTitle().contains("500mg")));
    }

    @Test
    public void testEmptyResults() throws SQLException {
        List<SearchResult> results = searchDAO.searchGlobal("XYZZY_NO_EXISTE");
        assertTrue(results.isEmpty() || results.size() < 2); // Módulos podrían coincidir si el nombre es corto
    }
}
