package com.utp.meditrackapp.tools;

import com.utp.meditrackapp.features.dashboard.Dao.DashboardDao;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import com.utp.meditrackapp.features.dashboard.service.HtmlReportService;
import com.utp.meditrackapp.features.dashboard.service.SeedDataLoader;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportGeneratorRunner {
    public static void main(String[] args) {
        try {
            SeedDataLoader seed = new SeedDataLoader();
            seed.ensureSeedData(6, 6);

            DashboardDao dao = new DashboardDao();
            HtmlReportService svc = new HtmlReportService();
            
            // 1. Dashboard Data
            List<MedicamentoResumen> items = dao.getTopBajoStock();
            Map<String, Double> trendData = dao.getInventoryTrendMonths(6).stream()
                .collect(Collectors.toMap(m -> (String)m.get("period"), m -> ((Number)m.get("value")).doubleValue()));
            Map<String, Double> catData = dao.getCategoryDistribution().stream()
                .collect(Collectors.toMap(m -> (String)m.get("category"), m -> ((Number)m.get("total")).doubleValue()));

            Map<String,Object> params = new HashMap<>();
            params.put("REPORT_DATE", java.time.LocalDate.now().toString());
            params.put("GENERATED_BY", "Runner Standalone");
            params.put("SEDE", "Sede de Pruebas");
            params.put("VALOR_INVENTARIO", "S/ 10,000.00");
            params.put("CRECIMIENTO_VALOR", "+0.0%");
            params.put("ALERTAS_STOCK", "0 Críticos");
            params.put("VOLUMEN_MOVIMIENTOS", "100");
            params.put("EFICIENCIA_OPERATIVA", "100%");
            params.put("CHART_TREND", svc.generateBarChartBase64(trendData, "Trend", "X", "Y"));
            params.put("CHART_CAT", svc.generatePieChartBase64(catData, "Categories"));
            params.put("items", items);

            File out = new File("target/standalone_dashboard.pdf");
            svc.generatePdf("dashboard", params, out);
            System.out.println("Reporte Dashboard HTML generado en: " + out.getAbsolutePath());

            // 2. Movements Data
            List<com.utp.meditrackapp.core.models.entity.Movimiento> movements = dao.getInventoryTrendMonths(1).stream().map(m -> {
                com.utp.meditrackapp.core.models.entity.Movimiento mov = new com.utp.meditrackapp.core.models.entity.Movimiento();
                mov.setFechaRegistro(java.time.LocalDateTime.now());
                mov.setTipoNombre("ENTRADA");
                mov.setProductoNombre("Producto Runner");
                mov.setNumeroLote("LOT-RUNNER");
                mov.setCantidad(50);
                mov.setMotivoNombre("COMPRA");
                return mov;
            }).collect(Collectors.toList());
            
            Map<String, Object> movParams = new HashMap<>();
            movParams.put("REPORT_DATE", "31/05/2026 12:00");
            movParams.put("GENERATED_BY", "Runner Standalone");
            movParams.put("SEDE", "Sede de Pruebas");
            movParams.put("DATE_RANGE", "Hoy");
            movParams.put("TOTAL_MOVEMENTS", "1");
            movParams.put("TOTAL_QUANTITY", "50");
            movParams.put("items", movements);
            
            File outMov = new File("target/standalone_movimientos.pdf");
            svc.generatePdf("movimientos", movParams, outMov);
            System.out.println("Reporte Movimientos HTML generado en: " + outMov.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
