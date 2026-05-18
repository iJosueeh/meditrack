package com.utp.meditrackapp.features.inventory.service;

import java.time.LocalDate;

public final class InventoryRules {
    private InventoryRules() {
    }

    public static void validarFechaVencimiento(LocalDate fechaVencimiento) {
        com.utp.meditrackapp.core.service.InventoryRules.validarFechaVencimiento(fechaVencimiento);
    }

    public static boolean venceDentroDe(LocalDate fechaVencimiento, int dias) {
        return com.utp.meditrackapp.core.service.InventoryRules.venceDentroDe(fechaVencimiento, dias);
    }
}
