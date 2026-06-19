package com.utp.meditrackapp.domain.services.reports;

import com.utp.meditrackapp.application.dto.MedicamentoResumenDTO;
import com.utp.meditrackapp.domain.ports.out.DashboardRepository;
import com.utp.meditrackapp.domain.ports.out.ReportService;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Caso de uso: Generar reporte consolidado del Dashboard.
 * Centraliza la lógica que estaba en ReportsController.onGenerateConsolidated().
 */
public class GenerarReporteConsolidadoUseCase {
    private final DashboardRepository dashboardRepository;
    private final ReportService reportService;

    public GenerarReporteConsolidadoUseCase(DashboardRepository dashboardRepository,
                                             ReportService reportService) {
        this.dashboardRepository = dashboardRepository;
        this.reportService = reportService;
    }

    /**
     * Genera el reporte consolidado y lo guarda en el archivo especificado.
     *
     * @param sedeId      ID de la sede del usuario
     * @param usuarioName Nombre completo del usuario
     * @param sedeName    Nombre de la sede
     * @param outputFile  Archivo de salida PDF
     */
    public void generar(String sedeId, String usuarioName, String sedeName, File outputFile) throws Exception {
        // 1. Obtener datos
        List<Map<String, Object>> productosRaw = dashboardRepository.getProductosBajoStock(5);
        List<MedicamentoResumenDTO> productos = productosRaw.stream()
                .map(m -> new MedicamentoResumenDTO(
                        (String) m.get("codigo"),
                        (String) m.get("nombre"),
                        (String) m.get("categoria"),
                        ((Number) m.get("stock_total")).intValue(),
                        10,
                        ((Number) m.get("stock_total")).intValue() < 10 ? "CRÍTICO" : "BAJO",
                        String.format("S/ %,.2f", ((Number) m.get("stock_total")).doubleValue() * ((Number) m.get("precio")).doubleValue())
                ))
                .toList();

        double invValue = dashboardRepository.getValorInventario();
        int criticalStock = dashboardRepository.getStockCriticoCount(10);
        int movementsVol = dashboardRepository.getVolumenMovimientos(30);
        int efficiency = dashboardRepository.getSaludInventario(sedeId);

        // 2. Gráficos
        Map<String, Double> trendData = new HashMap<>();
        dashboardRepository.getTendenciaInventario(6).forEach(m -> {
            String periodo = (String) m.get("period");
            try {
                YearMonth ym = YearMonth.parse(periodo);
                String monthName = ym.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "PE"));
                trendData.put(monthName + " " + ym.getYear(), ((Number) m.get("value")).doubleValue());
            } catch (Exception e) {
                trendData.put(periodo, ((Number) m.get("value")).doubleValue());
            }
        });

        Map<String, Double> catData = new HashMap<>();
        dashboardRepository.getDistribucionPorCategoria().forEach(m -> {
            catData.put((String) m.get("category"), ((Number) m.get("total")).doubleValue());
        });

        String chartTrend = reportService.generarGraficoBarras(trendData, "Existencias por Periodo", "Mes", "Unidades");
        String chartCat = reportService.generarGraficoCircular(catData, "Distribución por Categoría");

        // 3. Calcular crecimiento
        String crecimiento;
        if (trendData.size() >= 2) {
            List<Double> values = new ArrayList<>(trendData.values());
            double prev = values.get(values.size() - 2);
            double curr = values.get(values.size() - 1);
            if (prev > 0) {
                double pct = ((curr - prev) / prev) * 100;
                crecimiento = String.format("%+.1f%% vs. mes anterior", pct);
            } else {
                crecimiento = "N/A";
            }
        } else {
            crecimiento = "N/A";
        }

        // 4. Construir variables de plantilla
        Map<String, Object> vars = new HashMap<>();
        vars.put("REPORT_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        vars.put("GENERATED_BY", usuarioName);
        vars.put("SEDE", sedeName);
        vars.put("VALOR_INVENTARIO", String.format("S/ %,.2f", invValue));
        vars.put("CRECIMIENTO_VALOR", crecimiento);
        vars.put("ALERTAS_STOCK", criticalStock + " Críticos");
        vars.put("VOLUMEN_MOVIMIENTOS", String.format("%,d", movementsVol));
        vars.put("EFICIENCIA_OPERATIVA", efficiency + "%");
        vars.put("CHART_TREND", chartTrend);
        vars.put("CHART_CAT", chartCat);
        vars.put("items", productos);

        reportService.generarPdf("dashboard", vars, outputFile);
    }
}
