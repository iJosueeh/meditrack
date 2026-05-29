package com.utp.meditrackapp.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryHealthCalculatorTest {

    @Test
    @DisplayName("Debe descontar penalizaciones del calculo de salud")
    void shouldSubtractPenaltiesFromHealth() {
        assertEquals(94, InventoryHealthCalculator.calcularSaludInventario(2, 0, 0));
        assertEquals(90, InventoryHealthCalculator.calcularSaludInventario(0, 2, 0));
    }
}