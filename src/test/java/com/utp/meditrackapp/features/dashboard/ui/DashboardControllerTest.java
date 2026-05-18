package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DashboardControllerTest {

    @Test
    void testMedicamentoResumenCreation() {
        MedicamentoResumen med = new MedicamentoResumen(
            "MED001", "Paracetamol", "Analgésicos", 50, 10, "Estable"
        );
        
        assertEquals("MED001", med.getCode());
        assertEquals("Paracetamol", med.getName());
        assertEquals("Analgésicos", med.getCategory());
        assertEquals(50, med.getCurrentStock());
        assertEquals(10, med.getMinStock());
        assertEquals("Estable", med.getStatus());
    }
}
