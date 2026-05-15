package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.features.dashboard.Dao.DashboardDao;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import javafx.application.Platform;
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
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.time.LocalDate;
import javafx.stage.FileChooser;
import com.utp.meditrackapp.features.dashboard.service.ReportService;
import net.sf.jasperreports.engine.JRException;

public class DashboardController {

    @FXML private BorderPane rootPane;

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
        setupTable();
        loadDashboardData();
        
        if (rootPane != null && rootPane.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) rootPane.getParent();
            rootPane.prefHeightProperty().bind(parent.heightProperty());
            rootPane.prefWidthProperty().bind(parent.widthProperty());
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

        colName.setStyle("-fx-alignment: CENTER-LEFT;");
        colCategory.setStyle("-fx-alignment: CENTER-LEFT;");
        
        colCurrentStock.setStyle("-fx-alignment: CENTER;");
        colMinStock.setStyle("-fx-alignment: CENTER;");
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add("status-badge-base");
                    
                    if (item.equals("CRÍTICO")) {
                        badge.getStyleClass().add("status-badge-critico");
                    } else {
                        badge.getStyleClass().add("status-badge-bajo");
                    }
                    
                    HBox container = new HBox(badge);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                }
            }
        });
    }

    private void loadDashboardData() {
        try {
            int criticos = dashboardDao.getStockCriticoCount(10);
            int porVencer = dashboardDao.getLotesPorVencerCount(30);
            int salud = dashboardDao.getSaludInventario();

            updateStockCard(criticos);
            updateVencimientoCard(porVencer);
            
            if (saludValueLabel != null) {
                saludValueLabel.setText(salud + "%");
            }

            List<MedicamentoResumen> topBajoStock = dashboardDao.getTopBajoStock();
            if (topBajoStock != null) {
                topDrugsTable.setItems(FXCollections.observableArrayList(topBajoStock));
                Platform.runLater(() -> {
                    int rowsToShow = Math.min(5, Math.max(1, topBajoStock.size()));
                    double headerApprox = 36; // espacio para encabezado y bordes
                    double newHeight = rowsToShow * FIXED_ROW_HEIGHT + headerApprox;
                    topDrugsTable.setPrefHeight(newHeight);
                    topDrugsTable.setMaxHeight(newHeight);
                    topDrugsTable.setMinHeight(newHeight);
                });
            }
        } catch (Exception e) {
            System.err.println("[DASHBOARD ERROR] Error al cargar datos: " + e.getMessage());
        }
    }

    private void updateStockCard(int count) {
        if (stockCard == null) return;
        stockCard.getStyleClass().removeAll("card-success", "card-danger");
        stockValueLabel.getStyleClass().removeAll("card-value-success", "card-value-danger");
        stockIconContainer.getStyleClass().removeAll("icon-bg-success", "icon-bg-danger");

        if (count > 0) {
            stockCard.getStyleClass().add("card-danger");
            stockValueLabel.setText(String.valueOf(count));
            stockValueLabel.getStyleClass().add("card-value-danger");
            stockSubtitleLabel.setText("Lotes requieren reabastecimiento urgente");
            stockIconContainer.getStyleClass().add("icon-bg-danger");
            stockIcon.setIconLiteral("fas-exclamation-triangle");
            stockIcon.setIconColor(javafx.scene.paint.Color.web("#ef4444"));
        } else {
            stockCard.getStyleClass().add("card-success");
            stockValueLabel.setText("Saludable");
            stockValueLabel.getStyleClass().add("card-value-success");
            stockSubtitleLabel.setText("Todos los artículos cuentan con niveles óptimos");
            stockIconContainer.getStyleClass().add("icon-bg-success");
            stockIcon.setIconLiteral("fas-check-double");
            stockIcon.setIconColor(javafx.scene.paint.Color.web("#16a34a"));
        }
    }

    private void updateVencimientoCard(int count) {
        if (vencimientoCard == null) return;
        vencimientoCard.getStyleClass().removeAll("card-success", "card-warning");
        vencimientoValueLabel.getStyleClass().removeAll("card-value-success", "card-value-warning");
        vencimientoIconContainer.getStyleClass().removeAll("icon-bg-success", "icon-bg-warning");

        if (count > 0) {
            vencimientoCard.getStyleClass().add("card-warning");
            vencimientoValueLabel.setText(String.valueOf(count));
            vencimientoValueLabel.getStyleClass().add("card-value-warning");
            vencimientoSubtitleLabel.setText("Lotes vencen en los próximos 30 días");
            vencimientoIconContainer.getStyleClass().add("icon-bg-warning");
            vencimientoIcon.setIconLiteral("fas-hourglass-half");
            vencimientoIcon.setIconColor(javafx.scene.paint.Color.web("#eab308"));
        } else {
            vencimientoCard.getStyleClass().add("card-success");
            vencimientoValueLabel.setText("Óptimo");
            vencimientoValueLabel.getStyleClass().add("card-value-success");
            vencimientoSubtitleLabel.setText("No se registran vencimientos próximos");
            vencimientoIconContainer.getStyleClass().add("icon-bg-success");
            vencimientoIcon.setIconLiteral("fas-check-circle");
            vencimientoIcon.setIconColor(javafx.scene.paint.Color.web("#16a34a"));
        }
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
        } catch (JRException e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Error de JasperReports: " + e.getMessage(), ButtonType.OK);
            err.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert err = new Alert(Alert.AlertType.ERROR, "Error inesperado: " + e.getMessage(), ButtonType.OK);
            err.showAndWait();
        }
    }
}
