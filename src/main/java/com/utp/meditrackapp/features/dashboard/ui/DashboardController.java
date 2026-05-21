package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.features.inventory.service.InventarioService;
import com.utp.meditrackapp.features.dashboard.Dao.DashboardDao;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import com.utp.meditrackapp.features.dashboard.service.ReportService;

import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class DashboardController {

    private final InventarioService inventarioService = new InventarioService();

    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;

    // Métricas
    @FXML private HBox stockCard;
    @FXML private Label stockValueLabel;
    @FXML private Label stockSubtitleLabel;
    @FXML private VBox stockIconContainer;
    @FXML private FontIcon stockIcon;

    @FXML private HBox vencimientoCard;
    @FXML private Label vencimientoValueLabel;
    @FXML private Label vencimientoSubtitleLabel;
    @FXML private VBox vencimientoIconContainer;
    @FXML private FontIcon vencimientoIcon;

    @FXML private Label saludValueLabel;

    // Tabla
    @FXML private TableView<MedicamentoResumen> topDrugsTable;
    @FXML private TableColumn<MedicamentoResumen, String> colCode;
    @FXML private TableColumn<MedicamentoResumen, String> colName;
    @FXML private TableColumn<MedicamentoResumen, String> colCategory;
    @FXML private TableColumn<MedicamentoResumen, Integer> colCurrentStock;
    @FXML private TableColumn<MedicamentoResumen, Integer> colMinStock;
    @FXML private TableColumn<MedicamentoResumen, String> colStatus;

    private final DashboardDao dashboardDao = new DashboardDao();
    private static final double FIXED_ROW_HEIGHT = 48.0;

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            welcomeLabel.setText("¡Bienvenido de nuevo, Usuario!");
        }
        setupTable();
        loadDashboardMetrics();
        if (rootPane != null && rootPane.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) rootPane.getParent();
            rootPane.prefHeightProperty().bind(parent.heightProperty());
            rootPane.prefWidthProperty().bind(parent.widthProperty());
        }
    }

    private void loadDashboardMetrics() {
        try {
            int stockCritico = dashboardDao.getStockCriticoCount(10);
            stockValueLabel.setText(String.valueOf(stockCritico));
            
            int porVencer = dashboardDao.getLotesPorVencerCount(30);
            vencimientoValueLabel.setText(String.valueOf(porVencer));
            
            int salud = dashboardDao.getSaludInventario();
            saludValueLabel.setText(salud + "%");

            topDrugsTable.setItems(FXCollections.observableArrayList(dashboardDao.getTopBajoStock()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTable() {
        if (topDrugsTable == null) return;

        // Ensure consistent row height across themes
        topDrugsTable.setFixedCellSize(FIXED_ROW_HEIGHT);

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCurrentStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Quitamos los datos para probar el placeholder según lo solicitado
        topDrugsTable.setItems(FXCollections.observableArrayList());
    }

    @FXML
    protected void onLogout() throws IOException {
        NavigationService.toLogin();
    }

    @FXML
    protected void onGenerateReport() {
        System.out.println("Generando reporte con JasperReports...");
        try {
            DashboardDao dao = new DashboardDao();
            Map<String,Object> params = new HashMap<>();
            
            // Alineación con parámetros de mediTrack_reporte.jrxml
            params.put("REPORT_DATE", LocalDate.now().toString());
            params.put("GENERATED_BY", System.getProperty("user.name", "Usuario"));
            params.put("SEDE", "Sede Hospital Central");
            
            double inventoryValue = dao.getInventoryValue();
            params.put("VALOR_INVENTARIO", String.format("S/ %.2f", inventoryValue));
            params.put("VAR_INVENTARIO", "+0.0% vs mes anterior"); // Placeholder
            
            int criticos = dao.getStockCriticoCount(10);
            params.put("ALERTAS_CRITICAS", criticos + " Críticas");
            
            int movements = dao.getMovementsVolume(30);
            params.put("VOL_MOVIMIENTOS", String.valueOf(movements));
            
            int eficiencia = dao.getSaludInventario();
            params.put("EFICIENCIA", eficiencia + "%");

            List<MedicamentoResumen> items = dao.getTopBajoStock();
            List<Map<String,Object>> trend = dao.getInventoryTrendMonths(6);
            List<Map<String,Object>> categories = dao.getCategoryDistribution();

            ReportService svc = new ReportService();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar reporte PDF");
            chooser.setInitialFileName("Reporte_MediTrack_" + LocalDate.now() + ".pdf");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
            File out = chooser.showSaveDialog(rootPane.getScene().getWindow());
            if (out != null) {
                svc.generateDashboardPdf(params, items, trend, categories, out);
                Alert ok = new Alert(Alert.AlertType.INFORMATION, "Reporte generado: " + out.getAbsolutePath(), ButtonType.OK);
                ok.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            Alert err = new Alert(Alert.AlertType.ERROR, "Error generando reporte: " + msg, ButtonType.OK);
            err.showAndWait();
        }
    }
}
