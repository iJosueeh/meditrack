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

        // --- MEJORAS VISUALES DE ALINEACIÓN Y DISEÑO ---

        // 1. Alineación a la izquierda para texto (Default es izquierda, pero aseguramos)
        colName.setStyle("-fx-alignment: CENTER-LEFT;");
        colCategory.setStyle("-fx-alignment: CENTER-LEFT;");
        
        // 2. Centrado para números
        colCurrentStock.setStyle("-fx-alignment: CENTER;");
        colMinStock.setStyle("-fx-alignment: CENTER;");

        // 3. Custom Cell Factory para la columna ESTADO (Badges)
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
                // Ajustar la altura de la tabla para mostrar solo las filas necesarias
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
    }
}
