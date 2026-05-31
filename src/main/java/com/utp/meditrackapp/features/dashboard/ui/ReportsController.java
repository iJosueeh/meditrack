package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.core.models.entity.Movimiento;
import com.utp.meditrackapp.core.models.dto.DispensacionReportItem;
import com.utp.meditrackapp.core.util.SedeResolver;
import com.utp.meditrackapp.features.dashboard.Dao.DashboardDao;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import com.utp.meditrackapp.features.dashboard.service.HtmlReportService;
import com.utp.meditrackapp.features.inventory.service.InventarioService;
import com.utp.meditrackapp.features.attentions.service.AtencionService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {

    private final DashboardDao dashboardDao = new DashboardDao();
    private final InventarioService inventarioService = new InventarioService();
    private final AtencionService atencionService = new AtencionService();
    private final HtmlReportService reportService = new HtmlReportService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML private BorderPane rootPane;
    @FXML private DatePicker dpMovFrom, dpMovTo, dpDispFrom, dpDispTo;

    @FXML
    public void onGenerateConsolidated() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;

            // 1. Gather Data
            List<MedicamentoResumen> topProducts = dashboardDao.getTopBajoStock();
            
            // Unify total value calculation based on what the report actually shows
            double invValue = topProducts.stream()
                .map(p -> p.getFormattedTotalValue().replaceAll("[^\\d,.]", "").replace(",", ""))
                .mapToDouble(Double::parseDouble)
                .sum();
                
            int criticalStock = dashboardDao.getStockCriticoCount(10);
            int movementsVol = dashboardDao.getMovementsVolume(30);
            int efficiency = dashboardDao.getSaludInventario();

            // 2. Charts with improved labels
            Map<String, Double> trendData = new HashMap<>();
            dashboardDao.getInventoryTrendMonths(6).forEach(m -> {
                String periodo = (String)m.get("period"); // yyyy-MM
                try {
                    java.time.YearMonth ym = YearMonth.parse(periodo);
                    String monthName = ym.getMonth().getDisplayName(TextStyle.SHORT, new java.util.Locale("es", "PE"));
                    trendData.put(monthName + " " + ym.getYear(), ((Number)m.get("value")).doubleValue());
                } catch (Exception e) {
                    trendData.put(periodo, ((Number)m.get("value")).doubleValue());
                }
            });

            Map<String, Double> catData = dashboardDao.getCategoryDistribution().stream()
                .collect(Collectors.toMap(m -> (String)m.get("category"), m -> ((Number)m.get("total")).doubleValue()));

            String chartTrend = reportService.generateBarChartBase64(trendData, "Existencias por Periodo", "Mes", "Unidades");
            String chartCat = reportService.generatePieChartBase64(catData, "Distribución por Categoría");

            // 3. Variables
            Map<String, Object> vars = new HashMap<>();
            vars.put("REPORT_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            vars.put("GENERATED_BY", user.getNombres() + " " + user.getApellidos());
            vars.put("SEDE", SedeResolver.getSedeName(user));
            vars.put("VALOR_INVENTARIO", String.format("S/ %,.2f", invValue));
            vars.put("CRECIMIENTO_VALOR", "+2.4% vs. mes anterior");
            vars.put("ALERTAS_STOCK", criticalStock + " Críticos");
            vars.put("VOLUMEN_MOVIMIENTOS", String.format("%,d", movementsVol));
            vars.put("EFICIENCIA_OPERATIVA", efficiency + "%");
            vars.put("CHART_TREND", chartTrend);
            vars.put("CHART_CAT", chartCat);
            vars.put("items", topProducts);

            saveReport("reporte_consolidado", "dashboard", vars);

        } catch (Exception e) { showException(e); }
    }

    @FXML
    public void onGenerateMovements() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            LocalDate desde = dpMovFrom.getValue();
            LocalDate hasta = dpMovTo.getValue();

            List<Movimiento> movements = inventarioService.listarMovimientosConFiltros(user.getSedeId(), null, null, desde, hasta);
            if (movements.isEmpty()) { showAlert("Sin Datos", "No hay movimientos en el rango seleccionado."); return; }

            Map<String, Object> vars = new HashMap<>();
            vars.put("REPORT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("GENERATED_BY", user.getNombres() + " " + user.getApellidos());
            vars.put("SEDE", SedeResolver.getSedeName(user));
            
            String period = (desde != null ? desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio")
                          + " al " + 
                          (hasta != null ? hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoy");
            vars.put("DATE_RANGE", period);
            
            vars.put("TOTAL_MOVEMENTS", String.valueOf(movements.size()));
            vars.put("TOTAL_QUANTITY", String.valueOf(movements.stream().mapToInt(Movimiento::getCantidad).sum()));
            vars.put("items", movements);

            saveReport("reporte_movimientos", "movimientos", vars);
        } catch (Exception e) { showException(e); }
    }

    @FXML
    public void onGenerateDispensations() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            LocalDate desde = dpDispFrom.getValue();
            LocalDate hasta = dpDispTo.getValue();

            List<DispensacionReportItem> items = atencionService.listarDispensacionesReporte(user.getSedeId(), desde, hasta);
            if (items.isEmpty()) { showAlert("Sin Datos", "No hay dispensaciones en el rango seleccionado."); return; }

            Map<String, Object> vars = new HashMap<>();
            vars.put("REPORT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            vars.put("GENERATED_BY", user.getNombres() + " " + user.getApellidos());
            
            // Robust Sede Name Resolver
            String sedeDisplay = (user.getSedeNombre() != null && !user.getSedeNombre().isEmpty()) ? user.getSedeNombre() : "Sede Central";
            if (sedeDisplay.startsWith("SED-")) {
                // Fallback to fetch actual name if only ID is available
                try (Connection conn = DatabaseConfig.getInstance().getConnection();
                     PreparedStatement ps = conn.prepareStatement("SELECT nombre FROM sedes WHERE id = ?")) {
                    ps.setString(1, user.getSedeId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) sedeDisplay = rs.getString("nombre");
                    }
                } catch (java.sql.SQLException e) { e.printStackTrace(); }
            }
            vars.put("SEDE", sedeDisplay);

            String period = (desde != null ? desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio")
                          + " al " + 
                          (hasta != null ? hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoy");
            vars.put("DATE_RANGE", period);

            vars.put("TOTAL_ATTENTIONS", String.valueOf(items.stream().map(DispensacionReportItem::getNumeroReceta).distinct().count()));
            vars.put("TOTAL_MEDICINES", String.valueOf(items.stream().mapToInt(DispensacionReportItem::getCantidad).sum()));
            vars.put("items", items);

            saveReport("reporte_dispensaciones", "dispensaciones", vars);
        } catch (Exception e) { showException(e); }
    }

    private void saveReport(String prefix, String template, Map<String, Object> vars) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte PDF");
        fileChooser.setInitialFileName(prefix + "_" + LocalDate.now() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

        if (file != null) {
            reportService.generatePdf(template, vars, file);
            showAlert("Reporte Generado", "El documento se ha guardado en:\n" + file.getAbsolutePath());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }

    private void showException(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Reporte"); alert.setContentText(e.getMessage()); alert.showAndWait();
    }
}
