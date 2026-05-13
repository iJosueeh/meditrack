package com.utp.meditrackapp.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryRulesTest {

    @Test
    @DisplayName("Debe permitir fechas de vencimiento futuras")
    void shouldAllowFutureExpiryDates() {
        assertDoesNotThrow(() -> InventoryRules.validarFechaVencimiento(LocalDate.now().plusDays(1)));
    }

    @Test
    @DisplayName("Debe rechazar fechas de vencimiento pasadas")
    void shouldRejectPastExpiryDates() {
        assertThrows(IllegalArgumentException.class, () -> InventoryRules.validarFechaVencimiento(LocalDate.now().minusDays(1)));
    }

    @Test
    @DisplayName("Debe detectar vencimientos dentro del rango esperado")
    void shouldDetectExpiryWithinRange() {
        assertTrue(InventoryRules.venceDentroDe(LocalDate.now().plusDays(30), 30));
        assertFalse(InventoryRules.venceDentroDe(LocalDate.now().plusDays(61), 60));
    }
}