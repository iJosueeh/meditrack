package com.utp.meditrackapp.features.inventory.service;

public final class InventoryHealthCalculator {
    private InventoryHealthCalculator() {
    }

    public static int calcularSaludInventario(int productosCriticos, int lotesPorVencer30, int lotesPorVencer60) {
        return com.utp.meditrackapp.core.service.InventoryHealthCalculator.calcularSaludInventario(productosCriticos, lotesPorVencer30, lotesPorVencer60);
    }
}
