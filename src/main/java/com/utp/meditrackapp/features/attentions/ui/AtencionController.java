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

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.stage.FileChooser;
import com.utp.meditrackapp.features.dashboard.service.HtmlReportService;
import com.utp.meditrackapp.core.models.dto.DispensacionReportItem;

public class AtencionController {

    private final AtencionService atencionService = new AtencionService();
    private final InventarioService inventarioService = new InventarioService();
    private final PacienteService pacienteService = new PacienteService();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final HtmlReportService reportService = new HtmlReportService();

    // Patient & Prescription
    @FXML private TextField txtPacienteDni, txtReceta, txtMedico;
    @FXML private DatePicker dpFromDate, dpToDate;
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

    @FXML
    protected void onGenerateReport() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;

            java.time.LocalDate desde = dpFromDate.getValue();
            java.time.LocalDate hasta = dpToDate.getValue();

            List<DispensacionReportItem> items = atencionService.listarDispensacionesReporte(user.getSedeId(), desde, hasta);

            if (items.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Reporte Vacío", "No hay dispensaciones registradas en el periodo seleccionado.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Dispensaciones");
            fileChooser.setInitialFileName("reporte_dispensaciones_" + java.time.LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(txtPacienteDni.getScene().getWindow());

            if (file != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("REPORT_DATE", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                params.put("GENERATED_BY", user.getNombres() + " " + user.getApellidos());
                params.put("SEDE", com.utp.meditrackapp.core.util.SedeResolver.getSedeName(user));
                
                String period = (desde != null ? desde.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio") 
                              + " al " + 
                              (hasta != null ? hasta.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoy");
                params.put("DATE_RANGE", period);
                
                long totalAttentions = items.stream().map(DispensacionReportItem::getNumeroReceta).distinct().count();
                int totalMeds = items.stream().mapToInt(DispensacionReportItem::getCantidad).sum();
                
                params.put("TOTAL_ATTENTIONS", String.valueOf(totalAttentions));
                params.put("TOTAL_MEDICINES", String.valueOf(totalMeds));
                params.put("items", items);

                reportService.generatePdf("dispensaciones", params, file);
                showAlert(Alert.AlertType.INFORMATION, "Reporte Generado", "El reporte modernizado (HTML/CSS) se ha guardado exitosamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Ocurrió un error al generar el reporte: " + e.getMessage());
        }
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

            Usuario user = sessionManager.getCurrentUser();
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Sesión", "No hay una sesión activa.");
                return;
            }
            String sedeId = user.getSedeId();

            List<Lote> lotesDisponibles = inventarioService.listarLotesConProducto(sedeId);
            List<AtencionDetalle> suggestions = atencionService.sugerirDispensacion(sedeId, p.getId(), qty);
            
            for (AtencionDetalle det : suggestions) {
                Optional<Lote> loteOpt = lotesDisponibles.stream()
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
        a.setMedico(txtMedico.getText().trim());

        // Re-validar stock antes de confirmar (FEFO)
        try {
            String sedeId = sessionManager.getCurrentUser().getSedeId();
            List<Lote> lotesActuales = inventarioService.listarLotesConProducto(sedeId);
            for (AtencionDetalle det : basketItems) {
                Optional<Lote> loteActual = lotesActuales.stream()
                    .filter(l -> l.getId().equals(det.getLoteId()))
                    .findFirst();
                if (loteActual.isEmpty() || loteActual.get().getCantidad() < det.getCantidadEntregada()) {
                    showAlert(Alert.AlertType.WARNING, "Stock Cambiado",
                        "El lote " + det.getLoteNumero() + " ya no tiene stock suficiente. Por favor actualice la canasta.");
                    return;
                }
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo validar el stock actual.");
            return;
        }

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
