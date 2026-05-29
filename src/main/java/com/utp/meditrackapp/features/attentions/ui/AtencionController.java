package com.utp.meditrackapp.features.attentions.ui;

import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;
import com.utp.meditrackapp.core.models.entity.Producto;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.features.attentions.service.AtencionService;
import com.utp.meditrackapp.features.inventory.service.InventarioService;
import com.utp.meditrackapp.core.config.SessionManager;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AtencionController {

    private final AtencionService atencionService = new AtencionService();
    private final InventarioService inventarioService = new InventarioService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML private TextField txtPacienteDni, txtCantidad, txtDosis;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private Label lblLotesTitulo, lblStockTotal, lblPatientName, lblPatientPhone;
    @FXML private VBox vboxPatientInfo;
    @FXML private TableView<Lote> tableLotes;
    @FXML private TableColumn<Lote, String> colLoteNum, colLoteUbicacion, colLoteAccion;
    @FXML private TableColumn<Lote, LocalDate> colLoteVenc;
    @FXML private TableColumn<Lote, Integer> colLoteStock;

    private final com.utp.meditrackapp.features.patients.service.PacienteService pacienteService = new com.utp.meditrackapp.features.patients.service.PacienteService();

    @FXML
    public void initialize() {
        setupTables();
        loadProductos();
    }

    private void setupTables() {
        colLoteNum.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colLoteVenc.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colLoteUbicacion.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("Sede Central")); // Placeholder
        colLoteStock.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        
        // Columna de acción (botón)
        colLoteAccion.setCellFactory(param -> new TableCell<>() {
            final Button btn = new Button("Seleccionar");
            {
                btn.getStyleClass().addAll("button", "sm", "flat");
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    btn.setOnAction(e -> {
                        Lote lote = getTableView().getItems().get(getIndex());
                        System.out.println("Lote seleccionado: " + lote.getNumeroLote());
                    });
                    setGraphic(btn);
                }
            }
        });
    }

    private void loadProductos() {
        try {
            List<Producto> productos = inventarioService.listarProductosActivos();
            cmbProducto.setItems(FXCollections.observableArrayList(productos));
            cmbProducto.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p != null ? p.getNombre() : ""; }
                @Override public Producto fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    protected void onSearchPatient() {
        String dni = txtPacienteDni.getText();
        if (dni == null || dni.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Búsqueda de Paciente", "Por favor ingrese un DNI válido.");
            return;
        }
        
        // Integración con PacienteService
        List<com.utp.meditrackapp.core.models.entity.Paciente> results = pacienteService.buscarPacientes(dni);
        if (!results.isEmpty()) {
            com.utp.meditrackapp.core.models.entity.Paciente p = results.get(0);
            lblPatientName.setText(p.getNombres() + " " + p.getApellidos());
            lblPatientPhone.setText(p.getTelefono() != null ? p.getTelefono() : "No registrado");
            vboxPatientInfo.setVisible(true);
            vboxPatientInfo.setManaged(true);
        } else {
            vboxPatientInfo.setVisible(false);
            vboxPatientInfo.setManaged(false);
            showAlert(Alert.AlertType.ERROR, "Paciente No Encontrado", "No existe un paciente registrado con el DNI: " + dni);
        }
    }

    @FXML
    protected void onProductoSelected() {
        Producto p = cmbProducto.getValue();
        if (p != null) {
            lblLotesTitulo.setText("Lotes Disponibles: " + p.getNombre());
            try {
                String sedeId = sessionManager.getCurrentUser().getSedeId();
                List<Lote> lotes = inventarioService.listarLotesFefo(sedeId, p.getId());
                tableLotes.setItems(FXCollections.observableArrayList(lotes));
                
                // Calcular stock total
                int total = lotes.stream().mapToInt(Lote::getCantidad).sum();
                lblStockTotal.setText("Stock Total: " + total);
                
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    protected void onConfirmarEntrega() {
        // Lógica de confirmación pendiente
        System.out.println("Entrega confirmada para paciente: " + txtPacienteDni.getText());
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
