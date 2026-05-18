package com.utp.meditrackapp.core.service;

public final class InventoryHealthCalculator {
    private InventoryHealthCalculator() {
    }

    public static int calcularSaludInventario(int productosCriticos, int lotesPorVencer30, int lotesPorVencer60) {
        int penalizacion = (productosCriticos * 3) + (lotesPorVencer30 * 5) + (lotesPorVencer60 * 2);
        return Math.max(0, 100 - penalizacion);
    }
}