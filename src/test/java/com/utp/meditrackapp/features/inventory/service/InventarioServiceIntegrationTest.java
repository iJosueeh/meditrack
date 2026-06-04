package com.utp.meditrackapp.features.inventory.service;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.dao.LoteDAO;
import com.utp.meditrackapp.core.dao.MotivoMovimientoDAO;
import com.utp.meditrackapp.core.dao.TipoMovimientoDAO;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.models.entity.MotivoMovimiento;
import com.utp.meditrackapp.core.models.entity.TipoMovimiento;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class InventarioServiceIntegrationTest {

    private InventarioService inventarioService;
    private TipoMovimientoDAO tipoMovimientoDAO;
    private MotivoMovimientoDAO motivoMovimientoDAO;
    private LoteDAO loteDAO;

    @BeforeEach
    public void setUp() {
        Assumptions.assumeTrue(DatabaseConfig.getInstance().isReachable(), 
            "Abortando test: Base de datos no disponible");
        
        inventarioService = new InventarioService();
        tipoMovimientoDAO = new TipoMovimientoDAO();
        motivoMovimientoDAO = new MotivoMovimientoDAO();
        loteDAO = new LoteDAO();
    }

    @Test
    public void testDynamicMovementSemanticMatching() throws SQLException {
        // 1. Create a dynamic "Entrada" type
        TipoMovimiento dynamicType = new TipoMovimiento();
        dynamicType.setNombre("Donación de ENTRADA especial");
        dynamicType = tipoMovimientoDAO.crear(dynamicType);
        String typeId = dynamicType.getId();

        // 2. Create a dynamic reason
        MotivoMovimiento dynamicReason = new MotivoMovimiento();
        dynamicReason.setNombre("Donativo Externo");
        dynamicReason = motivoMovimientoDAO.crear(dynamicReason);
        String reasonId = dynamicReason.getId();

        // 3. Prepare a batch for the test (using LT-01 from seed data)
        Optional<Lote> loteOpt = loteDAO.buscarPorId("LT-01");
        assertTrue(loteOpt.isPresent(), "El lote LT-01 debe existir en la BD de prueba");
        Lote lote = loteOpt.get();
        int initialStock = lote.getCantidad();
        int amountToAdd = 10;

        // 4. Register movement using dynamic IDs
        assertDoesNotThrow(() -> {
            inventarioService.registrarMovimiento(
                lote, 
                "USR-001", 
                typeId, 
                reasonId, 
                amountToAdd, 
                "Test de movimiento dinámico semántico"
            );
        });

        // 5. Verify stock increased because name contains "entrada"
        Optional<Lote> updatedLoteOpt = loteDAO.buscarPorId("LT-01");
        assertEquals(initialStock + amountToAdd, updatedLoteOpt.get().getCantidad(), 
            "El stock debió aumentar semánticamente al contener 'entrada' en el nombre del tipo");

        // Cleanup
        cleanUpMovements(typeId, null);
        motivoMovimientoDAO.eliminar(reasonId);
        tipoMovimientoDAO.eliminar(typeId);
        
        // Restore stock
        updateStockManually(lote.getId(), -amountToAdd);
    }

    @Test
    public void testDynamicMovementExitSemanticMatching() throws SQLException {
        // 1. Create a dynamic "Salida" type
        TipoMovimiento dynamicType = new TipoMovimiento();
        dynamicType.setNombre("SALIDA por vencimiento");
        dynamicType = tipoMovimientoDAO.crear(dynamicType);
        String typeId = dynamicType.getId();

        // 2. Prepare a batch for the test
        Optional<Lote> loteOpt = loteDAO.buscarPorId("LT-01");
        Lote lote = loteOpt.get();
        int initialStock = lote.getCantidad();
        int amountToRemove = 5;

        // 3. Register movement
        inventarioService.registrarMovimiento(
            lote, 
            "USR-001", 
            typeId, 
            "MOV-M-04", // Existing Merma reason
            amountToRemove, 
            "Test de salida dinámica"
        );

        // 4. Verify stock decreased
        Optional<Lote> updatedLoteOpt = loteDAO.buscarPorId("LT-01");
        assertEquals(initialStock - amountToRemove, updatedLoteOpt.get().getCantidad(), 
            "El stock debió disminuir semánticamente al NO contener 'entrada' en el nombre del tipo");

        // Cleanup
        cleanUpMovements(typeId, null);
        tipoMovimientoDAO.eliminar(typeId);
        
        // Restore stock
        updateStockManually(lote.getId(), amountToRemove);
    }

    private void cleanUpMovements(String typeId, String reasonId) throws SQLException {
        String sql = "DELETE FROM movimientos WHERE tipo_id = ? OR motivo_id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, typeId);
            ps.setString(2, reasonId != null ? reasonId : "NON_EXISTENT");
            ps.executeUpdate();
        }
    }

    private void updateStockManually(String loteId, int delta) throws SQLException {
        String sql = "UPDATE lotes SET cantidad = cantidad + ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, loteId);
            ps.executeUpdate();
        }
    }
}
