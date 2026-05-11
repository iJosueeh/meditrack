package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.IOException;

public class DashboardController {

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
            welcomeLabel.setText("¡Bienvenido de nuevo, Usuario!");
        }
        setupTable();
    }

    private void setupTable() {
        if (topDrugsTable == null) return;

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
    }

    @FXML
    protected void onGoToPatients() {
    }

    @FXML
    protected void onGoToInventory() {
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
