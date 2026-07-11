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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

public class AtencionController {

    private final AtencionAdapter atencionAdapter = new AtencionAdapter();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML private BorderPane rootPane;

    // Tab Atenciones
    @FXML private TextField txtSearchPaciente;
    @FXML private HBox hboxPacienteInfo;
    @FXML private Label lblPacienteInfo, lblAtencionesCount;
    @FXML private TableView<Atencion> tableHistorial;
    @FXML private TableColumn<Atencion, String> colHistFecha, colHistReceta, colHistMedico, colHistAcciones;

    // Tab Dispensación
    @FXML private TextField txtBusquedaPacienteDisp;
    @FXML private HBox hboxPacienteInfoDisp;
    @FXML private Label lblPatientNameDisp, lblPatientPhoneDisp, lblRecetaNum;
    @FXML private TextField txtMedico;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private TextField txtCantidad;
    @FXML private TableView<AtencionDetalle> tableBasket;
    @FXML private TableColumn<AtencionDetalle, String> colBasketProduct, colBasketLote, colBasketExp, colBasketAction;
    @FXML private TableColumn<AtencionDetalle, Integer> colBasketQty;
    @FXML private Label lblItemsCount, lblTotalDispensed;
    @FXML private ComboBox<Lote> cmbLote;
    @FXML private javafx.scene.layout.HBox hboxLoteSelect;

    // Modals
    @FXML private StackPane modalDetalle;
    @FXML private Label lblDetalleTitle, lblDetalleSub;
    @FXML private TableView<AtencionDetalle> tableDetalle;
    @FXML private TableColumn<AtencionDetalle, String> colDetProducto, colDetLote, colDetVencimiento;
    @FXML private TableColumn<AtencionDetalle, Integer> colDetCantidad;
    @FXML private StackPane modalEditar;
    @FXML private Label lblEditarSub;
    @FXML private TextField txtEditReceta, txtEditMedico;
    @FXML private TableView<EditableDetalleRow> tableEditDetalle;
    @FXML private TableColumn<EditableDetalleRow, String> colEditProducto;
    @FXML private TableColumn<EditableDetalleRow, String> colEditLote;
    @FXML private TableColumn<EditableDetalleRow, Number> colEditCantidad;

    private final ObservableList<AtencionDetalle> basketItems = FXCollections.observableArrayList();
    private final ObservableList<Atencion> historialItems = FXCollections.observableArrayList();
    private final ObservableList<AtencionDetalle> detalleItems = FXCollections.observableArrayList();
    private final ObservableList<EditableDetalleRow> editDetalleItems = FXCollections.observableArrayList();
    private Paciente currentPacienteDisp;
    private Atencion editingAtencion;
    private String recetaGenerada;
    private List<AtencionDetalle> originalDetalles = new ArrayList<>();

    private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTables();
        setupProductCombo();
        loadInitialData();
        autoGenerarReceta();
    }

    private void autoGenerarReceta() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            recetaGenerada = atencionAdapter.generarNumeroReceta(user.getSedeId());
            lblRecetaNum.setText("N° Receta: " + recetaGenerada);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTables() {
        // Basket table
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
                else { HBox box = new HBox(btnDelete); box.setAlignment(Pos.CENTER); setGraphic(box); }
            }
        });
        tableBasket.setItems(basketItems);

        // Historial table
        colHistFecha.setCellValueFactory(cellData -> {
            Atencion a = cellData.getValue();
            String fecha = a.getFechaAtencion() != null ? a.getFechaAtencion().format(FMT_FECHA) : "N/D";
            return new javafx.beans.property.SimpleStringProperty(fecha);
        });
        colHistReceta.setCellValueFactory(new PropertyValueFactory<>("numeroReceta"));
        colHistMedico.setCellValueFactory(new PropertyValueFactory<>("medico"));
        colHistAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button();
            private final Button btnEditar = new Button();
            private final Button btnEliminar = new Button();
            {
                btnVer.setGraphic(new FontIcon("fas-eye"));
                btnVer.getStyleClass().addAll("button", "flat", "accent", "sm");
                btnVer.setTooltip(new Tooltip("Ver detalle"));
                btnVer.setOnAction(e -> { Atencion a = getTableRow().getItem(); if (a != null) onVerDetalle(a); });

                btnEditar.setGraphic(new FontIcon("fas-edit"));
                btnEditar.getStyleClass().addAll("button", "flat", "accent", "sm");
                btnEditar.setTooltip(new Tooltip("Editar"));
                btnEditar.setOnAction(e -> { Atencion a = getTableRow().getItem(); if (a != null) onEditarAtencion(a); });

                btnEliminar.setGraphic(new FontIcon("fas-trash"));
                btnEliminar.getStyleClass().addAll("button", "flat", "danger", "sm");
                btnEliminar.setTooltip(new Tooltip("Eliminar"));
                btnEliminar.setOnAction(e -> { Atencion a = getTableRow().getItem(); if (a != null) onEliminarAtencion(a); });
            }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    boolean canWrite = sessionManager.tienePermiso("M8_ATENCIONES");
                    HBox box = new HBox(6, btnVer);
                    if (canWrite) box.getChildren().addAll(btnEditar, btnEliminar);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
        tableHistorial.setItems(historialItems);

        // Detalle modal table
        colDetProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colDetLote.setCellValueFactory(new PropertyValueFactory<>("loteNumero"));
        colDetVencimiento.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colDetCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidadEntregada"));
        tableDetalle.setItems(detalleItems);

        // Edit modal detail table
        colEditProducto.setCellValueFactory(cd -> cd.getValue().productoNombreProperty());
        colEditCantidad.setCellValueFactory(cd -> cd.getValue().cantidadEntregadaProperty());
        colEditLote.setCellValueFactory(cd -> cd.getValue().loteNumeroProperty());
        colEditLote.setCellFactory(param -> new TableCell<>() {
            private ComboBox<Lote> combo;
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EditableDetalleRow row = getTableView().getItems().get(getIndex());
                    combo = new ComboBox<>(row.getLotesDisponibles());
                    combo.setConverter(new StringConverter<>() {
                        @Override public String toString(Lote l) {
                            if (l == null) return "";
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String vence = l.getFechaVencimiento() != null ? l.getFechaVencimiento().format(fmt) : "N/D";
                            return l.getNumeroLote() + " — Vence: " + vence + " — Stock: " + l.getCantidad();
                        }
                        @Override public Lote fromString(String s) { return null; }
                    });
                    // Select current lot
                    for (Lote l : row.getLotesDisponibles()) {
                        if (l.getId().equals(row.getLoteId())) {
                            combo.getSelectionModel().select(l);
                            break;
                        }
                    }
                    combo.valueProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal != null) {
                            row.loteIdProperty().set(newVal.getId());
                            row.loteNumeroProperty().set(newVal.getNumeroLote());
                        }
                    });
                    combo.setMaxWidth(Double.MAX_VALUE);
                    setGraphic(combo);
                }
            }
        });
        colEditCantidad.setCellFactory(param -> new TableCell<>() {
            private TextField tf;
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    tf = new TextField(item != null ? String.valueOf(item.intValue()) : "0");
                    tf.setMaxWidth(80);
                    tf.textProperty().addListener((obs, oldVal, newVal) -> {
                        try {
                            int qty = Integer.parseInt(newVal);
                            if (qty > 0) {
                                EditableDetalleRow row = getTableView().getItems().get(getIndex());
                                row.cantidadEntregadaProperty().set(qty);
                            }
                        } catch (NumberFormatException ignored) {}
                    });
                    setGraphic(tf);
                }
            }
        });
        tableEditDetalle.setItems(editDetalleItems);
    }

    private void setupProductCombo() {
        cmbProducto.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) { return p != null ? p.getNombre() : ""; }
            @Override public Producto fromString(String s) { return null; }
        });
        cmbLote.setConverter(new StringConverter<>() {
            @Override public String toString(Lote l) {
                if (l == null) return "";
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String vence = l.getFechaVencimiento() != null ? l.getFechaVencimiento().format(fmt) : "N/D";
                return l.getNumeroLote() + " — Vence: " + vence + " — Stock: " + l.getCantidad();
            }
            @Override public Lote fromString(String s) { return null; }
        });
    }

    private void loadInitialData() {
        try {
            List<Producto> productos = atencionAdapter.listarProductosActivos();
            cmbProducto.setItems(FXCollections.observableArrayList(productos));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== TAB ATENCIONES ====================

    @FXML
    protected void onSearchPacienteKey() {
        if (txtSearchPaciente.getText() != null && !txtSearchPaciente.getText().trim().isEmpty()) {
            // Allow typing; consult with Enter handled by onBuscarAtenciones
        }
    }

    @FXML
    protected void onBuscarAtenciones() {
        String query = txtSearchPaciente.getText();
        if (query == null || query.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese un DNI o nombre para buscar.");
            return;
        }

        try {
            List<Paciente> pacientes = atencionAdapter.buscarPacientes(query.trim());
            if (pacientes.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Sin resultados", "No se encontraron pacientes con: " + query);
                return;
            }
            if (pacientes.size() >= 5) {
                showAlert(Alert.AlertType.INFORMATION, "Búsqueda limitada",
                    "Se mostraron las primeras 5 coincidencias para \"" + query + "\".\n"
                    + "Sea más específico (use DNI o nombre completo) para refinar.");
            }
            if (pacientes.size() == 1) {
                cargarAtencionesPaciente(pacientes.get(0));
            } else {
                Paciente elegido = mostrarSelectorPaciente(pacientes);
                if (elegido != null) {
                    cargarAtencionesPaciente(elegido);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo buscar: " + e.getMessage());
        }
    }

    private Paciente mostrarSelectorPaciente(List<Paciente> pacientes) {
        List<String> opciones = pacientes.stream()
            .map(p -> p.getNumeroDocumento() + " \u2014 " + p.getNombreCompleto())
            .collect(Collectors.toList());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(opciones.get(0), opciones);
        dialog.setTitle("Seleccionar Paciente");
        String hint = pacientes.size() >= 5
            ? "Se encontraron múltiples pacientes (máx. 5 coincidencias mostradas)."
            : "Se encontraron " + pacientes.size() + " pacientes:";
        dialog.setHeaderText(hint);
        dialog.setContentText("Seleccione uno:");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(rootPane.getScene().getWindow());
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String selected = result.get();
            return pacientes.stream()
                .filter(p -> selected.equals(p.getNumeroDocumento() + " \u2014 " + p.getNombreCompleto()))
                .findFirst().orElse(null);
        }
        return null;
    }

    private void cargarAtencionesPaciente(Paciente paciente) {
        List<Atencion> atenciones = atencionAdapter.buscarHistorialPorPaciente(paciente.getId());
        historialItems.setAll(atenciones);

        hboxPacienteInfo.setVisible(true);
        hboxPacienteInfo.setManaged(true);
        lblPacienteInfo.setText(paciente.getNumeroDocumento() + " \u2014 " + paciente.getNombreCompleto());
        lblAtencionesCount.setText(atenciones.size() + " atenciones encontradas");
    }

    @FXML
    protected void onLimpiarBusqueda() {
        txtSearchPaciente.clear();
        historialItems.clear();
        hboxPacienteInfo.setVisible(false);
        hboxPacienteInfo.setManaged(false);
    }

    private void onVerDetalle(Atencion atencion) {
        try {
            List<AtencionDetalle> detalles = atencionAdapter.buscarDetallesAtencion(atencion.getId());
            detalleItems.setAll(detalles);
            lblDetalleTitle.setText("Detalle — Receta: " + atencion.getNumeroReceta());
            String fecha = atencion.getFechaAtencion() != null ? atencion.getFechaAtencion().format(FMT_FECHA) : "N/D";
            lblDetalleSub.setText("Fecha: " + fecha + "  |  Médico: " + (atencion.getMedico() != null ? atencion.getMedico() : "N/D"));
            modalDetalle.setVisible(true);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo cargar el detalle: " + e.getMessage());
        }
    }

    @FXML
    protected void onCloseDetalle() {
        modalDetalle.setVisible(false);
        detalleItems.clear();
    }

    private void onEditarAtencion(Atencion atencion) {
        if (!sessionManager.tienePermiso("M8_ATENCIONES")) {
            showAlert(Alert.AlertType.WARNING, "Sin permisos", "No tiene permisos para editar atenciones.");
            return;
        }
        if (atencion.getFechaAtencion() != null && atencion.getFechaAtencion().isBefore(java.time.LocalDateTime.now().minusHours(24))) {
            showAlert(Alert.AlertType.WARNING, "No permitido", "Solo se pueden editar atenciones de las últimas 24 horas.");
            return;
        }
        editingAtencion = atencion;
        txtEditReceta.setText(atencion.getNumeroReceta());
        txtEditMedico.setText(atencion.getMedico() != null ? atencion.getMedico() : "");
        String fecha = atencion.getFechaAtencion() != null ? atencion.getFechaAtencion().format(FMT_FECHA) : "N/D";
        lblEditarSub.setText("Atención del " + fecha);

        // Load details for editing
        editDetalleItems.clear();
        originalDetalles.clear();
        try {
            List<AtencionDetalle> detalles = atencionAdapter.buscarDetallesAtencion(atencion.getId());
            originalDetalles.addAll(detalles);
            Usuario user = sessionManager.getCurrentUser();
            String sedeId = user != null ? user.getSedeId() : null;
            for (AtencionDetalle det : detalles) {
                EditableDetalleRow row = new EditableDetalleRow();
                row.detalleIdProperty().set(det.getId());
                row.productoIdProperty().set(det.getProductoId());
                row.productoNombreProperty().set(det.getProductoNombre());
                row.loteIdProperty().set(det.getLoteId());
                row.loteNumeroProperty().set(det.getLoteNumero());
                row.cantidadEntregadaProperty().set(det.getCantidadEntregada());
                // Load available lots for this product
                if (det.getProductoId() != null && sedeId != null) {
                    List<Lote> lotes = atencionAdapter.listarLotesFefo(sedeId, det.getProductoId());
                    row.setLotesDisponibles(FXCollections.observableArrayList(lotes));
                }
                editDetalleItems.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        modalEditar.setVisible(true);
    }

    @FXML
    protected void onCloseEditar() {
        modalEditar.setVisible(false);
        editDetalleItems.clear();
        originalDetalles.clear();
        editingAtencion = null;
    }

    @FXML
    protected void onSaveEditar() {
        if (editingAtencion == null) return;
        String receta = txtEditReceta.getText();
        if (receta == null || receta.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El número de receta es obligatorio.");
            return;
        }
        editingAtencion.setNumeroReceta(receta.trim());
        editingAtencion.setMedico(txtEditMedico.getText() != null ? txtEditMedico.getText().trim() : "");

        // Build new details list from edit table
        List<AtencionDetalle> nuevas = new ArrayList<>();
        for (EditableDetalleRow row : editDetalleItems) {
            if (row.getLoteId() == null || row.getLoteId().isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Validación",
                    "Seleccione un lote para: " + row.getProductoNombre());
                return;
            }
            if (row.getCantidadEntregada() <= 0) {
                showAlert(Alert.AlertType.WARNING, "Validación",
                    "Ingrese una cantidad válida para: " + row.getProductoNombre());
                return;
            }
            AtencionDetalle det = new AtencionDetalle();
            det.setId(row.getDetalleId());
            det.setLoteId(row.getLoteId());
            det.setCantidadEntregada(row.getCantidadEntregada());
            det.setProductoId(row.getProductoId());
            det.setProductoNombre(row.getProductoNombre());
            nuevas.add(det);
        }

        try {
            String result = atencionAdapter.editarAtencionCompleta(editingAtencion, originalDetalles, nuevas);
            if ("OK".equals(result)) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Atención actualizada correctamente.");
                modalEditar.setVisible(false);
                editDetalleItems.clear();
                originalDetalles.clear();
                editingAtencion = null;
                onBuscarAtenciones();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar: " + e.getMessage());
        }
    }

    private void onEliminarAtencion(Atencion atencion) {
        if (!sessionManager.tienePermiso("M8_ATENCIONES")) {
            showAlert(Alert.AlertType.WARNING, "Sin permisos", "No tiene permisos para eliminar atenciones.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Eliminación");
        dialog.setHeaderText("¿Está seguro de eliminar esta atención?");
        dialog.setContentText("Receta: " + atencion.getNumeroReceta() + "\nEscriba 'ELIMINAR' para confirmar:");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(rootPane.getScene().getWindow());
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && "ELIMINAR".equals(result.get().trim().toUpperCase())) {
            try {
                String r = atencionAdapter.eliminarAtencion(atencion.getId());
                if ("OK".equals(r)) {
                    showAlert(Alert.AlertType.INFORMATION, "Éxito", "Atención eliminada.");
                    onBuscarAtenciones();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    // ==================== TAB DISPENSACIÓN ====================

    @FXML
    protected void onBuscarPacienteDisp() {
        String query = txtBusquedaPacienteDisp.getText();
        if (query == null || query.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese un DNI o nombre para buscar.");
            return;
        }

        try {
            List<Paciente> pacientes = atencionAdapter.buscarPacientes(query.trim());
            if (pacientes.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Sin resultados", "No se encontraron pacientes con: " + query);
                return;
            }
            if (pacientes.size() >= 5) {
                showAlert(Alert.AlertType.INFORMATION, "Búsqueda limitada",
                    "Se mostraron las primeras 5 coincidencias para \"" + query + "\".\n"
                    + "Sea más específico (use DNI o nombre completo) para refinar.");
            }
            if (pacientes.size() == 1) {
                selectPacienteDisp(pacientes.get(0));
            } else {
                Paciente elegido = mostrarSelectorPaciente(pacientes);
                if (elegido != null) {
                    selectPacienteDisp(elegido);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo buscar: " + e.getMessage());
        }
    }

    @FXML
    protected void onLimpiarBusquedaDisp() {
        currentPacienteDisp = null;
        txtBusquedaPacienteDisp.clear();
        hboxPacienteInfoDisp.setVisible(false);
        hboxPacienteInfoDisp.setManaged(false);
        txtMedico.clear();
        txtCantidad.clear();
        cmbProducto.getSelectionModel().clearSelection();
        hboxLoteSelect.setVisible(false);
        hboxLoteSelect.setManaged(false);
        cmbLote.getItems().clear();
        basketItems.clear();
        updateBasketTotals();
        autoGenerarReceta();
    }

    @FXML
    protected void onSearchPacienteDispKey(javafx.scene.input.KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            onBuscarPacienteDisp();
        }
    }

    private void setupDispensacionTypeahead() {
    }

    private void hidePopup() {
    }

    private void selectPacienteDisp(Paciente paciente) {
        currentPacienteDisp = paciente;
        txtBusquedaPacienteDisp.setText(paciente.getNumeroDocumento() + " \u2014 " + paciente.getNombreCompleto());
        lblPatientNameDisp.setText(paciente.getNombreCompleto());
        lblPatientPhoneDisp.setText("Tel: " + (paciente.getTelefono() != null ? paciente.getTelefono() : "N/R"));
        lblRecetaNum.setText("N° Receta: " + recetaGenerada);
        hboxPacienteInfoDisp.setVisible(true);
        hboxPacienteInfoDisp.setManaged(true);
    }

    @FXML
    protected void onProductoSelected() {
        Producto p = cmbProducto.getValue();
        if (p == null) {
            hboxLoteSelect.setVisible(false);
            hboxLoteSelect.setManaged(false);
            return;
        }
        cargarLotesProducto(p);
    }

    private void cargarLotesProducto(Producto producto) {
        Usuario user = sessionManager.getCurrentUser();
        if (user == null) return;
        List<Lote> lotesFefo = atencionAdapter.listarLotesFefo(user.getSedeId(), producto.getId());
        cmbLote.getItems().clear();
        if (lotesFefo.isEmpty()) {
            hboxLoteSelect.setVisible(false);
            hboxLoteSelect.setManaged(false);
            showAlert(Alert.AlertType.INFORMATION, "Sin stock", "No hay lotes disponibles para este producto.");
        } else {
            cmbLote.getItems().addAll(lotesFefo);
            cmbLote.getSelectionModel().selectFirst();
            hboxLoteSelect.setVisible(true);
            hboxLoteSelect.setManaged(true);
        }
    }

    @FXML
    protected void onAddToBasket() {
        Producto p = cmbProducto.getValue();
        Lote loteSeleccionado = cmbLote.getValue();
        String qtyStr = txtCantidad.getText();
        if (p == null || loteSeleccionado == null || qtyStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Seleccione producto, lote y cantidad.");
            return;
        }
        try {
            int qty = Integer.parseInt(qtyStr);
            if (qty <= 0) throw new NumberFormatException();
            if (loteSeleccionado.getCantidad() < qty) {
                showAlert(Alert.AlertType.WARNING, "Stock insuficiente", 
                    "El lote " + loteSeleccionado.getNumeroLote() + " solo tiene " + loteSeleccionado.getCantidad() + " unidades.");
                return;
            }
            AtencionDetalle det = new AtencionDetalle();
            det.setLoteId(loteSeleccionado.getId());
            det.setProductoNombre(p.getNombre());
            det.setLoteNumero(loteSeleccionado.getNumeroLote());
            det.setCantidadEntregada(qty);
            if (loteSeleccionado.getFechaVencimiento() != null)
                det.setFechaVencimiento(loteSeleccionado.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            basketItems.add(det);
            updateBasketTotals();
            txtCantidad.clear();
            cmbProducto.getSelectionModel().clearSelection();
            hboxLoteSelect.setVisible(false);
            hboxLoteSelect.setManaged(false);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Ingrese una cantidad válida.");
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
        try { SedeAccessValidator.validarSedeActiva(); }
        catch (SedeAccessValidator.SedeBloqueadaException e) { showAlert(Alert.AlertType.WARNING, "Sede Bloqueada", e.getMessage()); return; }

        if (currentPacienteDisp == null) { showAlert(Alert.AlertType.WARNING, "Falta", "Debe seleccionar un paciente."); return; }
        if (basketItems.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Falta", "La canasta está vacía."); return; }

        String numReceta = recetaGenerada;
        String sedeId = sessionManager.getCurrentUser().getSedeId();
        if (atencionAdapter.existeReceta(sedeId, numReceta)) {
            showAlert(Alert.AlertType.WARNING, "Duplicado", "Ya existe una atención con receta " + numReceta);
            return;
        }

        Atencion a = new Atencion();
        a.setPacienteId(currentPacienteDisp.getId());
        a.setNumeroReceta(numReceta);
        a.setMedico(txtMedico.getText() != null ? txtMedico.getText().trim() : "");
        a.setSedeId(sedeId);
        a.setUsuarioId(sessionManager.getCurrentUser().getId());

        try {
            List<Lote> lotesActuales = atencionAdapter.listarLotesConProducto(sedeId);
            for (AtencionDetalle det : basketItems) {
                Optional<Lote> loteActual = lotesActuales.stream().filter(l -> l.getId().equals(det.getLoteId())).findFirst();
                if (loteActual.isEmpty() || loteActual.get().getCantidad() < det.getCantidadEntregada()) {
                    showAlert(Alert.AlertType.WARNING, "Stock Cambiado", "El lote " + det.getLoteNumero() + " ya no tiene stock suficiente.");
                    return;
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo validar el stock actual.");
            return;
        }

        String result = atencionAdapter.registrarAtencion(a, new ArrayList<>(basketItems));
        if ("OK".equals(result)) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "La atención se registró y el stock fue actualizado.");
            onLimpiarTodo();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result);
        }
    }

    @FXML
    protected void onLimpiarTodo() {
        currentPacienteDisp = null;
        txtBusquedaPacienteDisp.clear();
        hboxPacienteInfoDisp.setVisible(false);
        hboxPacienteInfoDisp.setManaged(false);
        txtSearchPaciente.clear();
        historialItems.clear();
        hboxPacienteInfo.setVisible(false);
        hboxPacienteInfo.setManaged(false);
        txtMedico.clear();
        txtCantidad.clear();
        cmbProducto.getSelectionModel().clearSelection();
        hboxLoteSelect.setVisible(false);
        hboxLoteSelect.setManaged(false);
        cmbLote.getItems().clear();
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
        alert.initOwner(rootPane.getScene().getWindow());
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/utp/meditrackapp/styles/global.css").toExternalForm());
        alert.showAndWait();
    }

    // Inner class for editable detail rows in the edit modal
    public static class EditableDetalleRow {
        private final StringProperty productoNombre = new SimpleStringProperty();
        private final StringProperty loteNumero = new SimpleStringProperty();
        private final IntegerProperty cantidadEntregada = new SimpleIntegerProperty();
        private final StringProperty loteId = new SimpleStringProperty();
        private final StringProperty productoId = new SimpleStringProperty();
        private final StringProperty detalleId = new SimpleStringProperty();
        private ObservableList<Lote> lotesDisponibles = FXCollections.observableArrayList();

        public StringProperty productoNombreProperty() { return productoNombre; }
        public StringProperty loteNumeroProperty() { return loteNumero; }
        public IntegerProperty cantidadEntregadaProperty() { return cantidadEntregada; }
        public StringProperty loteIdProperty() { return loteId; }
        public StringProperty productoIdProperty() { return productoId; }
        public StringProperty detalleIdProperty() { return detalleId; }

        public ObservableList<Lote> getLotesDisponibles() { return lotesDisponibles; }
        public void setLotesDisponibles(ObservableList<Lote> lotes) { this.lotesDisponibles = lotes; }

        public String getProductoNombre() { return productoNombre.get(); }
        public String getLoteNumero() { return loteNumero.get(); }
        public int getCantidadEntregada() { return cantidadEntregada.get(); }
        public String getLoteId() { return loteId.get(); }
        public String getProductoId() { return productoId.get(); }
        public String getDetalleId() { return detalleId.get(); }
    }
}
