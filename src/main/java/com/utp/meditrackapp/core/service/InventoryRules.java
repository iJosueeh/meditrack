package com.utp.meditrackapp.core.service;

import java.time.LocalDate;

public final class InventoryRules {
    private InventoryRules() {
    }

    public static void validarFechaVencimiento(LocalDate fechaVencimiento) {
        if (fechaVencimiento == null) {
            throw new IllegalArgumentException("La fecha de vencimiento es obligatoria.");
        }

        if (fechaVencimiento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten lotes con fechas de vencimiento pasadas.");
        }
    }

    public static boolean venceDentroDe(LocalDate fechaVencimiento, int dias) {
        if (fechaVencimiento == null) {
            return false;
        }

        LocalDate limite = LocalDate.now().plusDays(dias);
        return !fechaVencimiento.isAfter(limite);
    }
}