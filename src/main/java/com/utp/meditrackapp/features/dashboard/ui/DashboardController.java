package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.service.InventarioService;
import com.utp.meditrackapp.core.session.SessionContext;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    private final InventarioService inventarioService = new InventarioService();

    @FXML
    private BorderPane rootPane;
    @FXML
    private FontIcon themeIcon;
    
    @FXML
    private Label welcomeLabel;

    @FXML
    private TableView<MedicamentoResumen> topDrugsTable;
    @FXML
    private TableColumn<MedicamentoResumen, String> colCode;
    @FXML
    private TableColumn<MedicamentoResumen, String> colName;
    @FXML
    private TableColumn<MedicamentoResumen, String> colCategory;
    @FXML
    private TableColumn<MedicamentoResumen, Integer> colCurrentStock;
    @FXML
    private TableColumn<MedicamentoResumen, Integer> colMinStock;
    @FXML
    private TableColumn<MedicamentoResumen, String> colStatus;

    @FXML
    public void initialize() {
        if (welcomeLabel != null) {
            String usuarioId = SessionContext.getUsuarioId().orElse("Usuario");
            welcomeLabel.setText("¡Bienvenido de nuevo, " + usuarioId + "!");
        }
        setupTable();
        cargarTopMedicamentos();
        if (rootPane != null && rootPane.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) rootPane.getParent();
            rootPane.prefHeightProperty().bind(parent.heightProperty());
            rootPane.prefWidthProperty().bind(parent.widthProperty());
        }
    }

    private void setupTable() {
        if (topDrugsTable == null) return;

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCurrentStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        colMinStock.setCellValueFactory(new PropertyValueFactory<>("minStock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        topDrugsTable.setItems(FXCollections.observableArrayList());
    }

    private void cargarTopMedicamentos() {
        if (topDrugsTable == null) {
            return;
        }

        SessionContext.getSedeId().ifPresent(sedeId -> {
            try {
                List<StockCriticoItem> items = inventarioService.obtenerTopStockBajo(sedeId, 5);
                ObservableList<MedicamentoResumen> filas = FXCollections.observableArrayList();

                for (StockCriticoItem item : items) {
                    filas.add(new MedicamentoResumen(
                            item.getCodigoDigemid(),
                            item.getNombreProducto(),
                            item.getCategoria(),
                            item.getStockActual(),
                            item.getStockMinimo(),
                            item.getEstado()
                    ));
                }

                topDrugsTable.setItems(filas);
            } catch (SQLException error) {
                System.err.println("[INVENTARIO] No fue posible cargar el resumen de stock: " + error.getMessage());
            }
        });
    }

    @FXML
    protected void onLogout() throws IOException {
        SessionContext.clear();
        NavigationService.toLogin();
    }

    @FXML
    protected void onGenerateReport() {
        System.out.println("Generando reporte con JasperReports...");
    }

    @FXML
    protected void onGoToPatients() {
    }

    @FXML
    protected void onGoToInventory() {
    }

    @FXML
    protected void onToggleTheme() {
        // 1. Tomar captura del estado actual
        WritableImage snapshot = rootPane.snapshot(new SnapshotParameters(), null);
        ImageView tempView = new ImageView(snapshot);

        // 2. Superponer la captura sobre el contenido real
        StackPane parent = (StackPane) rootPane.getParent();
        parent.getChildren().add(tempView);

        // 3. Cambiar el tema en el fondo del root (StackPane), no solo en el BorderPane
        if (parent.getStyleClass().contains("dark-theme")) {
            parent.getStyleClass().remove("dark-theme");
            themeIcon.setIconLiteral("fas-moon");
        } else {
            parent.getStyleClass().add("dark-theme");
            themeIcon.setIconLiteral("fas-sun");
        }

        // 4. Animación de desvanecimiento de la captura vieja
        FadeTransition fade = new FadeTransition(Duration.millis(400), tempView);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> parent.getChildren().remove(tempView));
        fade.play();
    }

    public static class MedicamentoResumen {
        private final SimpleStringProperty code;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleIntegerProperty currentStock;
        private final SimpleIntegerProperty minStock;
        private final SimpleStringProperty status;

        public MedicamentoResumen(String code, String name, String category, int currentStock, int minStock, String status) {
            this.code = new SimpleStringProperty(code);
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.currentStock = new SimpleIntegerProperty(currentStock);
            this.minStock = new SimpleIntegerProperty(minStock);
            this.status = new SimpleStringProperty(status);
        }

        public String getCode() { return code.get(); }
        public String getName() { return name.get(); }
        public String getCategory() { return category.get(); }
        public int getCurrentStock() { return currentStock.get(); }
        public int getMinStock() { return minStock.get(); }
        public String getStatus() { return status.get(); }
    }
}
