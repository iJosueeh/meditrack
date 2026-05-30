package com.utp.meditrackapp.features.attentions.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.*;
import com.utp.meditrackapp.features.attentions.service.AtencionService;
import com.utp.meditrackapp.features.inventory.service.InventarioService;
import com.utp.meditrackapp.features.patients.service.PacienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AtencionController {

    private final AtencionService atencionService = new AtencionService();
    private final InventarioService inventarioService = new InventarioService();
    private final PacienteService pacienteService = new PacienteService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    // Patient & Prescription
    @FXML private TextField txtPacienteDni, txtReceta, txtMedico;
    @FXML private Label lblPatientName, lblPatientPhone;
    @FXML private VBox vboxPatientInfo;
    @FXML private ListView<String> listHistory;

    // Basket Entry
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    
    // Basket Table
    @FXML private TableView<AtencionDetalle> tableBasket;
    @FXML private TableColumn<AtencionDetalle, String> colBasketProduct, colBasketLote, colBasketAction;
    @FXML private TableColumn<AtencionDetalle, Integer> colBasketQty;
    @FXML private Label lblItemsCount, lblTotalDispensed;

    private final ObservableList<AtencionDetalle> basketItems = FXCollections.observableArrayList();
    private Paciente currentPaciente;

    @FXML
    public void initialize() {
        setupTables();
        setupProductCombo();
        loadInitialData();
    }

    private void setupTables() {
        // Basket Table
        colBasketProduct.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colBasketLote.setCellValueFactory(new PropertyValueFactory<>("loteNumero"));
        colBasketQty.setCellValueFactory(new PropertyValueFactory<>("cantidadEntregada"));

        colBasketAction.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button();
            {
                btnDelete.setGraphic(new FontIcon("fas-times"));
                btnDelete.getStyleClass().addAll("button", "flat", "danger", "sm");
                btnDelete.setOnAction(e -> {
                    AtencionDetalle item = getTableRow().getItem();
                    if (item != null) basketItems.remove(item);
                    updateBasketTotals();
                });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox box = new HBox(btnDelete);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        tableBasket.setItems(basketItems);
    }

    private void setupProductCombo() {
        cmbProducto.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) { return p != null ? p.getNombre() : ""; }
            @Override public Producto fromString(String s) { return null; }
        });
    }

    private void loadInitialData() {
        try {
            List<Producto> productos = inventarioService.listarProductosActivos();
            cmbProducto.setItems(FXCollections.observableArrayList(productos));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onSearchPatient() {
        String dni = txtPacienteDni.getText();
        if (dni == null || dni.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese un DNI para buscar.");
            return;
        }

        List<Paciente> results = pacienteService.buscarPacientes(dni);
        if (!results.isEmpty()) {
            currentPaciente = results.get(0);
            lblPatientName.setText(currentPaciente.getNombres() + " " + currentPaciente.getApellidos());
            lblPatientPhone.setText("Teléfono: " + (currentPaciente.getTelefono() != null ? currentPaciente.getTelefono() : "N/R"));
            vboxPatientInfo.setVisible(true);
            vboxPatientInfo.setManaged(true);
            loadPatientHistory(currentPaciente.getId());
        } else {
            currentPaciente = null;
            vboxPatientInfo.setVisible(false);
            vboxPatientInfo.setManaged(false);
            showAlert(Alert.AlertType.ERROR, "No Encontrado", "No se encontró un paciente con ese DNI.");
        }
    }

    private void loadPatientHistory(String pacienteId) {
        List<Atencion> history = atencionService.buscarHistorialPorPaciente(pacienteId);
        ObservableList<String> historyStrings = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        for (Atencion a : history) {
            historyStrings.add(a.getFechaAtencion().format(formatter) + " - Receta: " + a.getNumeroReceta());
        }
        
        if (historyStrings.isEmpty()) historyStrings.add("Sin atenciones previas.");
        listHistory.setItems(historyStrings);
    }

    @FXML
    protected void onProductoSelected() {
        // No auto-logic here yet, just selection
    }

    @FXML
    protected void onAddToBasket() {
        Producto p = cmbProducto.getValue();
        String qtyStr = txtCantidad.getText();

        if (p == null || qtyStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Seleccione un producto y cantidad.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();

            String sedeId = sessionManager.getCurrentUser().getSedeId();
            // Implement logic to find FEFO batches and add to basket
            List<AtencionDetalle> suggestions = atencionService.sugerirDispensacion(sedeId, p.getId(), qty);
            
            for (AtencionDetalle det : suggestions) {
                // To show in UI, we should fetch batch number
                // Fetching batch info for display
                Optional<Lote> loteOpt = inventarioService.listarLotesConProducto(sedeId).stream()
                    .filter(l -> l.getId().equals(det.getLoteId()))
                    .findFirst();
                
                det.setProductoNombre(p.getNombre());
                det.setLoteNumero(loteOpt.isPresent() ? loteOpt.get().getNumeroLote() : "LOTE-?");
                
                basketItems.add(det);
            }
            
            updateBasketTotals();
            txtCantidad.clear();
            cmbProducto.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese una cantidad válida.");
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Sin Stock", e.getMessage());
        }
    }

    private void updateBasketTotals() {
        int items = basketItems.size();
        int totalQty = basketItems.stream().mapToInt(AtencionDetalle::getCantidadEntregada).sum();
        lblItemsCount.setText(items + " ítems");
        lblTotalDispensed.setText(String.valueOf(totalQty));
    }

    @FXML
    protected void onConfirmarEntrega() {
        if (currentPaciente == null) {
            showAlert(Alert.AlertType.WARNING, "Falta Información", "Debe seleccionar un paciente.");
            return;
        }
        if (basketItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Falta Información", "La canasta está vacía.");
            return;
        }
        if (txtReceta.getText().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Falta Información", "Ingrese el número de receta.");
            return;
        }

        Atencion a = new Atencion();
        a.setPacienteId(currentPaciente.getId());
        a.setNumeroReceta(txtReceta.getText().trim());
        // Medico could be added to table but using reciept notes for now or extra field
        
        String result = atencionService.registrarAtencion(a, new ArrayList<>(basketItems));
        
        if (result.equals("OK")) {
            showAlert(Alert.AlertType.INFORMATION, "Atención Exitosa", "La atención se registró y el stock fue actualizado.");
            onLimpiarTodo();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result);
        }
    }

    @FXML
    protected void onLimpiarTodo() {
        currentPaciente = null;
        vboxPatientInfo.setVisible(false);
        vboxPatientInfo.setManaged(false);
        txtPacienteDni.clear();
        txtReceta.clear();
        txtMedico.clear();
        txtCantidad.clear();
        cmbProducto.getSelectionModel().clearSelection();
        basketItems.clear();
        updateBasketTotals();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }
}
