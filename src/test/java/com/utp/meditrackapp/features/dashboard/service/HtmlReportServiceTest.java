package com.utp.meditrackapp.features.dashboard.service;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class HtmlReportServiceTest {

    private final HtmlReportService reportService = new HtmlReportService();

    @Test
    public void testGenerateBarChartBase64() {
        assertDoesNotThrow(() -> {
            Map<String, Double> data = new HashMap<>();
            data.put("Enero", 100.0);
            data.put("Febrero", 150.0);
            
            String result = reportService.generateBarChartBase64(data, "Test", "X", "Y");
            assertNotNull(result);
            assertTrue(result.startsWith("data:image/png;base64,"));
        });
    }

    @Test
    public void testGeneratePieChartBase64() {
        assertDoesNotThrow(() -> {
            Map<String, Double> data = new HashMap<>();
            data.put("Cat A", 50.0);
            data.put("Cat B", 50.0);
            
            String result = reportService.generatePieChartBase64(data, "Test");
            assertNotNull(result);
            assertTrue(result.startsWith("data:image/png;base64,"));
        });
    }

    @Test
    public void testEmptyDataCharts() {
        assertDoesNotThrow(() -> {
            String resultBar = reportService.generateBarChartBase64(new HashMap<>(), "Empty", "X", "Y");
            String resultPie = reportService.generatePieChartBase64(null, "Empty");
            
            assertNotNull(resultBar);
            assertNotNull(resultPie);
            assertTrue(resultBar.contains("base64,"));
            assertTrue(resultPie.contains("base64,"));
        });
    }
}
