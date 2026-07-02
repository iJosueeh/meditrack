package com.utp.meditrackapp.features.attentions.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.validation.SedeAccessValidator;
import com.utp.meditrackapp.domain.entities.Atencion;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.infrastructure.adapters.AtencionAdapter;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import com.utp.meditrackapp.infrastructure.adapters.ReportAdapter;

public class AtencionController {

    private final AtencionAdapter atencionAdapter = new AtencionAdapter();
    private final SessionManager sessionManager = SessionManager.getInstance();

    // Patient & Prescription
    @FXML private ComboBox<Paciente> cmbPaciente;
    @FXML private TextField txtReceta;
    @FXML private ComboBox<String> cmbMedico;
    @FXML private DatePicker dpFromDate, dpToDate;
    @FXML private Label lblPatientName, lblPatientPhone;
    @FXML private VBox vboxPatientInfo;
    @FXML private ListView<String> listHistory;

    // Basket Entry
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    
    // Basket Table
    @FXML private TableView<AtencionDetalle> tableBasket;
    @FXML private TableColumn<AtencionDetalle, String> colBasketProduct, colBasketLote, colBasketExp, colBasketAction;
    @FXML private TableColumn<AtencionDetalle, Integer> colBasketQty;
    @FXML private Label lblItemsCount, lblTotalDispensed;
    @FXML private Label lblFefoSuggestion;

    private final ObservableList<AtencionDetalle> basketItems = FXCollections.observableArrayList();
    private final ObservableList<Paciente> allPacientes = FXCollections.observableArrayList();
    private final ObservableList<String> allMedicos = FXCollections.observableArrayList();
    private Paciente currentPaciente;

    @FXML
    public void initialize() {
        setupTables();
        setupPacienteCombo();
        setupMedicoCombo();
        setupProductCombo();
        loadInitialData();
        autoGenerarReceta();
    }

    private void autoGenerarReceta() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            String receta = atencionAdapter.generarNumeroReceta(user.getSedeId());
            txtReceta.setText(receta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onGenerateReport() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;

            java.time.LocalDate desde = dpFromDate.getValue();
            java.time.LocalDate hasta = dpToDate.getValue();

            com.utp.meditrackapp.infrastructure.adapters.            ReportAdapter reportAdapter = new ReportAdapter();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Dispensaciones");
            fileChooser.setInitialFileName("reporte_dispensaciones_" + java.time.LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(cmbPaciente.getScene().getWindow());

            if (file != null) {
                String sedeName = com.utp.meditrackapp.core.util.SedeResolver.getSedeName(user);
                boolean generated = reportAdapter.generarReporteDispensaciones(
                    user.getSedeId(),
                    user.getNombres() + " " + user.getApellidos(),
                    sedeName,
                    desde, hasta,
                    file
                );
                
                if (generated) {
                    showAlert(Alert.AlertType.INFORMATION, "Reporte Generado", "El reporte modernizado (HTML/CSS) se ha guardado exitosamente.");
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "Reporte Vacío", "No hay dispensaciones registradas en el periodo seleccionado.");
                }
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
        colBasketExp.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
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
            List<Producto> productos = atencionAdapter.listarProductosActivos();
            cmbProducto.setItems(FXCollections.observableArrayList(productos));

            List<Paciente> pacientes = atencionAdapter.buscarPacientes("");
            allPacientes.setAll(pacientes);

            List<String> medicos = atencionAdapter.listarMedicosDistinct();
            for (String m : medicos) {
                if (!allMedicos.contains(m)) allMedicos.add(m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPacienteCombo() {
        cmbPaciente.setEditable(true);
        cmbPaciente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Paciente p) {
                if (p == null) return "";
                return p.getNumeroDocumento() + " — " + p.getNombreCompleto();
            }
            @Override
            public Paciente fromString(String s) { return null; }
        });

        cmbPaciente.setItems(allPacientes);

        cmbPaciente.getEditor().textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null) newValue = "";
            String filter = newValue.toLowerCase();

            // Don't filter if the text matches the currently selected patient (set by converter after selection)
            if (cmbPaciente.getValue() != null) {
                String expected = cmbPaciente.getConverter().toString(cmbPaciente.getValue());
                if (filter.equals(expected != null ? expected.toLowerCase() : "")) {
                    return;
                }
            }

            List<Paciente> filtered = allPacientes.stream()
                .filter(p -> filter.isEmpty()
                    || (p.getNumeroDocumento() != null && p.getNumeroDocumento().toLowerCase().contains(filter))
                    || (p.getNombres() != null && p.getNombres().toLowerCase().contains(filter))
                    || (p.getApellidos() != null && p.getApellidos().toLowerCase().contains(filter)))
                .collect(Collectors.toList());

            cmbPaciente.hide();
            cmbPaciente.setItems(FXCollections.observableArrayList(filtered));
            if (!filtered.isEmpty()) {
                cmbPaciente.show();
            }
        });

        listHistory.itemsProperty().addListener((obs, old, items) -> {
            listHistory.setMouseTransparent(items == null || items.isEmpty());
        });
    }

    private void setupMedicoCombo() {
        cmbMedico.setEditable(true);
        cmbMedico.setItems(allMedicos);
    }

    @FXML
    protected void onPacienteSelected() {
        Paciente selected = cmbPaciente.getValue();
        if (selected == null) return;

        currentPaciente = selected;
        lblPatientName.setText(currentPaciente.getNombres() + " " + currentPaciente.getApellidos());
        lblPatientPhone.setText("Teléfono: " + (currentPaciente.getTelefono() != null ? currentPaciente.getTelefono() : "N/R"));
        vboxPatientInfo.setVisible(true);
        vboxPatientInfo.setManaged(true);
        loadPatientHistory(currentPaciente.getId());
    }

    @FXML
    protected void onSearchByReceta() {
        String receta = txtReceta.getText();
        if (receta == null || receta.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese un número de receta para buscar.");
            return;
        }

        Usuario user = sessionManager.getCurrentUser();
        if (user == null) return;

        List<Atencion> results = atencionAdapter.buscarHistorialPorReceta(user.getSedeId(), receta);
        ObservableList<String> historyStrings = FXCollections.observableArrayList();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Atencion a : results) {
            historyStrings.add(a.getFechaAtencion().format(formatter) + " - Receta: " + a.getNumeroReceta());
        }

        if (historyStrings.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Sin Resultados", "No se encontraron atenciones con la receta " + receta);
        } else {
            listHistory.setItems(historyStrings);
            vboxPatientInfo.setVisible(true);
            vboxPatientInfo.setManaged(true);
            lblPatientName.setText("Búsqueda por receta: " + receta);
            lblPatientPhone.setText(results.size() + " resultado(s) encontrado(s)");
        }
    }

    private void loadPatientHistory(String pacienteId) {
        List<Atencion> history = atencionAdapter.buscarHistorialPorPaciente(pacienteId);
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
        Producto p = cmbProducto.getValue();
        if (p == null) {
            lblFefoSuggestion.setVisible(false);
            lblFefoSuggestion.setManaged(false);
            return;
        }

        Usuario user = sessionManager.getCurrentUser();
        if (user == null) return;

        List<Lote> lotesFefo = atencionAdapter.listarLotesFefo(user.getSedeId(), p.getId());
        if (lotesFefo.isEmpty()) {
            lblFefoSuggestion.setText("Sin stock disponible para este producto.");
            lblFefoSuggestion.setVisible(true);
            lblFefoSuggestion.setManaged(true);
        } else {
            Lote primero = lotesFefo.get(0);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String vence = primero.getFechaVencimiento() != null ? primero.getFechaVencimiento().format(fmt) : "N/D";
            lblFefoSuggestion.setText("FEFO sugerido: Lote " + primero.getNumeroLote()
                + " — Vence: " + vence
                + " — Stock: " + primero.getCantidad()
                + (lotesFefo.size() > 1 ? " (+ " + (lotesFefo.size() - 1) + " lote(s) más)" : ""));
            lblFefoSuggestion.setVisible(true);
            lblFefoSuggestion.setManaged(true);
        }
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

            List<Lote> lotesDisponibles = atencionAdapter.listarLotesConProducto(sedeId);
            List<AtencionDetalle> suggestions = atencionAdapter.sugerirDispensacion(sedeId, p.getId(), qty);
            
            for (AtencionDetalle det : suggestions) {
                Optional<Lote> loteOpt = lotesDisponibles.stream()
                    .filter(l -> l.getId().equals(det.getLoteId()))
                    .findFirst();
                
                det.setProductoNombre(p.getNombre());
                if (loteOpt.isPresent()) {
                    det.setLoteNumero(loteOpt.get().getNumeroLote());
                    if (loteOpt.get().getFechaVencimiento() != null) {
                        det.setFechaVencimiento(loteOpt.get().getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    }
                } else {
                    det.setLoteNumero("LOTE-?");
                }
                
                basketItems.add(det);
            }
            
            updateBasketTotals();
            txtCantidad.clear();
            cmbProducto.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese una cantidad válida.");
        } catch (Exception e) {
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
        // Validar que la sede no esté bloqueada
        try {
            SedeAccessValidator.validarSedeActiva();
        } catch (SedeAccessValidator.SedeBloqueadaException e) {
            showAlert(Alert.AlertType.WARNING, "Sede Bloqueada", e.getMessage());
            return;
        }
        
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

        String numReceta = txtReceta.getText().trim();
        String sedeId = sessionManager.getCurrentUser().getSedeId();
        if (atencionAdapter.existeReceta(sedeId, numReceta)) {
            showAlert(Alert.AlertType.WARNING, "Duplicado",
                "Ya existe una atención con el número de receta " + numReceta + ". Modifíquelo e intente de nuevo.");
            return;
        }

        Atencion a = new Atencion();
        a.setPacienteId(currentPaciente.getId());
        a.setNumeroReceta(numReceta);
        a.setMedico(cmbMedico.getValue() != null ? cmbMedico.getValue().trim() : "");
        a.setSedeId(sedeId);
        a.setUsuarioId(sessionManager.getCurrentUser().getId());

        // Re-validar stock antes de confirmar (FEFO)
        try {
            List<Lote> lotesActuales = atencionAdapter.listarLotesConProducto(sedeId);
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
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo validar el stock actual.");
            return;
        }

        String result = atencionAdapter.registrarAtencion(a, new ArrayList<>(basketItems));
        
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
        cmbPaciente.getSelectionModel().clearSelection();
        cmbPaciente.getEditor().clear();
        cmbMedico.getSelectionModel().clearSelection();
        cmbMedico.getEditor().clear();
        txtCantidad.clear();
        cmbProducto.getSelectionModel().clearSelection();
        lblFefoSuggestion.setVisible(false);
        lblFefoSuggestion.setManaged(false);
        basketItems.clear();
        updateBasketTotals();
        autoGenerarReceta();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        alert.initOwner(cmbPaciente.getScene().getWindow());
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/utp/meditrackapp/styles/global.css").toExternalForm());
        alert.showAndWait();
    }
}
