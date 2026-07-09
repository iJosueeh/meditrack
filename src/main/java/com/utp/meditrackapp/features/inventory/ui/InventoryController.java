package com.utp.meditrackapp.features.inventory.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.validation.SedeAccessValidator;
import com.utp.meditrackapp.application.dto.StockCriticoDTO;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.infrastructure.adapters.InventoryAdapter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.util.List;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import com.utp.meditrackapp.infrastructure.reports.HtmlPdfReportService;

public class InventoryController {

    private final InventoryAdapter inventoryAdapter = new InventoryAdapter();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final HtmlPdfReportService reportService = new HtmlPdfReportService();

    @FXML private TabPane inventoryTabPane;
    @FXML private TableView<Movimiento> tableMovements;
    @FXML private TableView<Lote> tableBatches;
    @FXML private VBox alertsContainer;
    @FXML private Button btnNewMovement;
    
    // Filters
    @FXML private TextField txtSearchMovement;
    @FXML private TextField txtSearchBatch;
    @FXML private ComboBox<TipoMovimiento> cmbMovementType;
    @FXML private DatePicker dpFromDate, dpToDate;

    // Columns
    @FXML private TableColumn<Movimiento, String> colMovDate, colMovType, colMovProduct, colMovBatch, colMovReason, colMovObs;
    @FXML private TableColumn<Movimiento, Integer> colMovQty;
    @FXML private TableColumn<Movimiento, Void> colMovActions;
    @FXML private TableColumn<Lote, String> colBatchDigemid, colBatchProduct, colBatchNum;
    @FXML private TableColumn<Lote, LocalDate> colBatchExp, colBatchFab;
    @FXML private Label lblActiveBatches, lblCriticalBatches;
    @FXML private Label lblTotalMovs, lblEntryQty, lblExitQty;

    // Quick Registration
    @FXML private ComboBox<TipoMovimiento> cmbQuickType;
    @FXML private ComboBox<Lote> cmbQuickBatch;
    @FXML private ComboBox<MotivoMovimiento> cmbQuickMotivo;
    @FXML private TextField txtQuickQty, txtQuickObs;

    // Expiration threshold
    @FXML private ComboBox<Integer> cmbExpirationThreshold;

    // Modal fields
    @FXML private StackPane modalMovement;
    @FXML private VBox vboxEntradaDetails;
    @FXML private ComboBox<Producto> cmbModalProduct;
    @FXML private ComboBox<TipoMovimiento> cmbModalType;
    @FXML private ComboBox<MotivoMovimiento> cmbModalMotivo;
    @FXML private ComboBox<Lote> cmbModalBatch;
    @FXML private VBox cmbModalBatchContainer;
    @FXML private TextField txtNewLote, txtModalQty;
    @FXML private TextArea txtModalObs;
    @FXML private DatePicker dpNewVenc, dpNewFab;

    // Pagination
    @FXML private Pagination paginationMovements;
    @FXML private Pagination paginationBatches;
    private static final int ROWS_PER_PAGE = 10;
    private ObservableList<Movimiento> allMovements = FXCollections.observableArrayList();
    private ObservableList<Lote> allBatches = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTables();
        loadDataInBackground();
        applyRolePermissions();
    }

    private void loadDataInBackground() {
        setUIBusy(true);
        
        Task<List<?>> loadTask = new Task<>() {
            @Override
            protected List<?> call() throws Exception {
                Usuario user = sessionManager.getCurrentUser();
                if (user == null) return List.of();
                
                List<Producto> productos = inventoryAdapter.listarProductosActivos();
                List<TipoMovimiento> tipos = inventoryAdapter.listarTiposMovimiento();
                List<MotivoMovimiento> motivos = inventoryAdapter.listarMotivosMovimiento();
                List<Lote> lotes = inventoryAdapter.listarLotesConProducto(user.getSedeId());
                
                return List.of(productos, tipos, motivos, lotes);
            }
        };
        
        loadTask.setOnSucceeded(e -> {
            try {
                List<?> results = loadTask.get();
                if (results.size() >= 4) {
                    applyLoadedData(
                        (List<Producto>) results.get(0),
                        (List<TipoMovimiento>) results.get(1),
                        (List<MotivoMovimiento>) results.get(2),
                        (List<Lote>) results.get(3)
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                setUIBusy(false);
            }
        });
        
        loadTask.setOnFailed(e -> {
            setUIBusy(false);
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los datos iniciales.");
        });
        
        new Thread(loadTask).start();
    }

    private void applyLoadedData(List<Producto> productos, List<TipoMovimiento> tipos, 
                                 List<MotivoMovimiento> motivos, List<Lote> lotes) {
        cmbModalProduct.setItems(FXCollections.observableArrayList(productos));
        
        boolean canEntrada = sessionManager.tienePermiso("M5_ENTRADAS");
        boolean canSalida = sessionManager.tienePermiso("M6_SALIDAS");
        List<TipoMovimiento> tiposPermitidos = tipos.stream()
            .filter(t -> {
                if (t.getId().equals(TipoMovimientoEnum.ENTRADA.getId())) return canEntrada;
                if (t.getId().equals(TipoMovimientoEnum.SALIDA.getId())) return canSalida;
                return true;
            })
            .collect(Collectors.toList());
        ObservableList<TipoMovimiento> obsTipos = FXCollections.observableArrayList(tiposPermitidos);
        
        cmbModalType.setItems(obsTipos);
        cmbQuickType.setItems(obsTipos);
        
        ObservableList<TipoMovimiento> filterTipos = FXCollections.observableArrayList();
        TipoMovimiento todos = new TipoMovimiento();
        todos.setId("");
        todos.setNombre("TODOS");
        filterTipos.add(todos);
        filterTipos.addAll(tipos);
        cmbMovementType.setItems(filterTipos);
        cmbMovementType.getSelectionModel().selectFirst();

        StringConverter<TipoMovimiento> tipoConverter = new StringConverter<>() {
            @Override public String toString(TipoMovimiento t) { return t != null ? t.getNombre().toUpperCase() : ""; }
            @Override public TipoMovimiento fromString(String s) { return null; }
        };

        cmbModalType.setConverter(tipoConverter);
        cmbQuickType.setConverter(tipoConverter);
        cmbMovementType.setConverter(tipoConverter);
        
        cmbQuickMotivo.setItems(FXCollections.observableArrayList(motivos));
        cmbQuickMotivo.setConverter(new StringConverter<>() {
            @Override public String toString(MotivoMovimiento m) { return m != null ? m.getNombre() : ""; }
            @Override public MotivoMovimiento fromString(String s) { return null; }
        });
        
        cmbModalProduct.setConverter(new StringConverter<>() {
            @Override public String toString(Producto p) { return p != null ? p.getNombre() : ""; }
            @Override public Producto fromString(String s) { return null; }
        });
        cmbModalMotivo.setConverter(new StringConverter<>() {
            @Override public String toString(MotivoMovimiento m) { return m != null ? m.getNombre() : ""; }
            @Override public MotivoMovimiento fromString(String s) { return null; }
        });

        cmbQuickBatch.setItems(FXCollections.observableArrayList(lotes));
        cmbQuickBatch.setConverter(new StringConverter<>() {
            @Override public String toString(Lote l) { return l != null ? l.getProductoNombre() + " [" + l.getNumeroLote() + "]" : ""; }
            @Override public Lote fromString(String s) { return null; }
        });

        cmbExpirationThreshold.setItems(FXCollections.observableArrayList(30, 60, 90));
        cmbExpirationThreshold.getSelectionModel().selectFirst();

        handleInitialSearch();
        refreshMovements();
        refreshBatches();
    }

    private void setUIBusy(boolean busy) {
        if (inventoryTabPane != null) {
            inventoryTabPane.setDisable(busy);
        }
        if (btnNewMovement != null) {
            btnNewMovement.setDisable(busy);
        }
    }

    private void applyRolePermissions() {
        var user = sessionManager.getCurrentUser();
        if (user != null) {
            boolean canRegister = sessionManager.tienePermiso("M5_ENTRADAS") || sessionManager.tienePermiso("M6_SALIDAS");
            if (btnNewMovement != null) {
                btnNewMovement.setVisible(canRegister);
                btnNewMovement.setManaged(canRegister);
            }
        }
    }

    private void handleInitialSearch() {
        String initialSearch = NavigationService.getInventoryInitialSearch();
        if (initialSearch != null && !initialSearch.isEmpty()) {
            if (txtSearchMovement != null) txtSearchMovement.setText(initialSearch);
            if (txtSearchBatch != null) txtSearchBatch.setText(initialSearch);

            // Switch to Batch Monitor tab if searching for a specific product/batch
            if (inventoryTabPane != null && inventoryTabPane.getTabs().size() > 1) {
                inventoryTabPane.getSelectionModel().select(1);
            }

            refreshMovements();
            refreshBatches();
        }
    }

    private void setupTables() {
        java.time.format.DateTimeFormatter dateFmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        colMovDate.setCellValueFactory(data -> {
            java.time.LocalDateTime dt = data.getValue().getFechaRegistro();
            return new javafx.beans.property.SimpleStringProperty(dt != null ? dt.format(dateFmt) : "");
        });
        colMovType.setCellValueFactory(new PropertyValueFactory<>("tipoNombre"));
        colMovProduct.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colMovBatch.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colMovQty.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMovReason.setCellValueFactory(new PropertyValueFactory<>("motivoNombre"));
        colMovObs.setCellValueFactory(new PropertyValueFactory<>("observacion"));

        setupMovementCellFactories();
        setupMovActionsCellFactory();
        setupTooltipCellFactory(colMovProduct);
        setupTooltipCellFactory(colMovObs);

        colBatchDigemid.setCellValueFactory(new PropertyValueFactory<>("codigoDigemid"));
        colBatchProduct.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colBatchNum.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colBatchFab.setCellValueFactory(new PropertyValueFactory<>("fechaFabricacion"));
        colBatchExp.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));

        setupExpirationCellFactory();
    }

    private void setupMovementCellFactories() {
        colMovType.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    FontIcon icon = new FontIcon();
                    Label label = new Label(item.toUpperCase());
                    label.getStyleClass().add("text-bold");

                    if (item.equalsIgnoreCase("ENTRADA")) {
                        icon.setIconLiteral("fas-arrow-circle-down");
                        icon.getStyleClass().add("icon-success");
                        label.getStyleClass().add("text-on-success");
                    } else {
                        icon.setIconLiteral("fas-arrow-circle-up");
                        icon.getStyleClass().add("icon-danger");
                        label.getStyleClass().add("text-on-danger");
                    }
                    box.getChildren().addAll(icon, label);
                    setGraphic(box);
                }
            }
        });

        colMovReason.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    FontIcon icon = new FontIcon();
                    icon.getStyleClass().add("icon-muted");
                    
                    String upperItem = item.toUpperCase();
                    if (upperItem.contains("COMPRA")) icon.setIconLiteral("fas-shopping-cart");
                    else if (upperItem.contains("MERMA")) icon.setIconLiteral("fas-trash-alt");
                    else if (upperItem.contains("ATENCIÓN") || upperItem.contains("ATENCION")) icon.setIconLiteral("fas-hand-holding-medical");
                    else if (upperItem.contains("TRANSFERENCIA")) icon.setIconLiteral("fas-exchange-alt");
                    else icon.setIconLiteral("fas-info-circle");

                    box.getChildren().addAll(icon, new Label(item));
                    setGraphic(box);
                }
            }
        });
    }

    private void setupMovActionsCellFactory() {
        colMovActions.setCellFactory(column -> new TableCell<>() {
            private final Button btnAnular = new Button();
            private final Button btnEditar = new Button();
            private final HBox box = new HBox(3, btnAnular, btnEditar);

            {
                btnAnular.setGraphic(new FontIcon("fas-ban"));
                btnAnular.getStyleClass().addAll("button", "sm", "danger");
                btnAnular.setTooltip(new Tooltip("Anular"));
                btnAnular.setMinWidth(30);
                btnAnular.setMaxWidth(30);

                btnEditar.setGraphic(new FontIcon("fas-edit"));
                btnEditar.getStyleClass().addAll("button", "sm", "flat");
                btnEditar.setTooltip(new Tooltip("Editar"));
                btnEditar.setMinWidth(30);
                btnEditar.setMaxWidth(30);

                box.setAlignment(javafx.geometry.Pos.CENTER);
                box.setMinWidth(66);
                box.setMaxWidth(66);

                btnAnular.setOnAction(e -> {
                    Movimiento mov = getTableView().getItems().get(getIndex());
                    onAnularMovimiento(mov);
                });
                btnEditar.setOnAction(e -> {
                    Movimiento mov = getTableView().getItems().get(getIndex());
                    onEditarObservacion(mov);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getTableView().getItems().size() <= getIndex()) {
                    setGraphic(null);
                } else {
                    Movimiento mov = getTableView().getItems().get(getIndex());
                    boolean canModify;
                    if (mov.isEntrada()) {
                        canModify = sessionManager.tienePermiso("M5_ENTRADAS");
                    } else {
                        canModify = sessionManager.tienePermiso("M6_SALIDAS");
                    }
                    btnAnular.setVisible(canModify);
                    btnAnular.setManaged(canModify);
                    btnEditar.setVisible(canModify);
                    btnEditar.setManaged(canModify);
                    setGraphic(box);
                }
            }
        });
    }

    private void setupTooltipCellFactory(TableColumn<Movimiento, String> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    setTooltip(new Tooltip(item));
                }
            }
        });
    }

    private void onAnularMovimiento(Movimiento mov) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Anular Movimiento");
        confirm.setHeaderText("¿Está seguro de anular este movimiento?");
        confirm.setContentText("El stock del lote se revertirá automáticamente.\n\n" +
            "Producto: " + mov.getProductoNombre() + "\n" +
            "Lote: " + mov.getNumeroLote() + "\n" +
            "Tipo: " + mov.getTipoNombre() + "\n" +
            "Cantidad: " + mov.getCantidad() + "\n" +
            "Observación: " + (mov.getObservacion() != null ? mov.getObservacion() : ""));
        confirm.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        confirm.initOwner(inventoryTabPane.getScene().getWindow());
        DialogPane dp = confirm.getDialogPane();
        dp.getStylesheets().add(getClass().getResource("/com/utp/meditrackapp/styles/global.css").toExternalForm());
        dp.getStyleClass().add("modal-content");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    inventoryAdapter.anularMovimiento(mov);
                    showAlert(Alert.AlertType.INFORMATION, "Movimiento Anulado",
                        "El movimiento se ha anulado y el stock ha sido revertido.");
                    refreshMovements();
                    refreshBatches();
                    populateAlerts();
                } catch (IllegalStateException e) {
                    showAlert(Alert.AlertType.ERROR, "Error al Anular", e.getMessage());
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error Inesperado",
                        "No se pudo anular el movimiento: " + e.getMessage());
                }
            }
        });
    }

    private void onEditarObservacion(Movimiento mov) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Editar Movimiento");
        dialog.setHeaderText("Modifique los campos del movimiento:");
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.initOwner(inventoryTabPane.getScene().getWindow());

        DialogPane dp = dialog.getDialogPane();
        dp.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dp.getStyleClass().addAll("modal-content");
        dp.getStylesheets().add(getClass().getResource("/com/utp/meditrackapp/styles/global.css").toExternalForm());
        dp.setStyle("-fx-padding: 25;");

        Label lblInfo = new Label("Producto: " + mov.getProductoNombre() + " | Lote: " + mov.getNumeroLote());
        lblInfo.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        ComboBox<TipoMovimiento> cmbTipo = new ComboBox<>();
        cmbTipo.setPromptText("Tipo");
        List<TipoMovimiento> tiposEditPermitidos = inventoryAdapter.listarTiposMovimiento().stream()
            .filter(t -> {
                if (t.getId().equals(TipoMovimientoEnum.ENTRADA.getId())) return sessionManager.tienePermiso("M5_ENTRADAS");
                if (t.getId().equals(TipoMovimientoEnum.SALIDA.getId())) return sessionManager.tienePermiso("M6_SALIDAS");
                return true;
            })
            .collect(Collectors.toList());
        cmbTipo.getItems().addAll(tiposEditPermitidos);
        cmbTipo.setConverter(new StringConverter<>() {
            @Override public String toString(TipoMovimiento t) { return t != null ? t.getNombre() : ""; }
            @Override public TipoMovimiento fromString(String s) { return null; }
        });
        for (TipoMovimiento t : cmbTipo.getItems()) {
            if (t.getId().equals(mov.getTipoId())) { cmbTipo.getSelectionModel().select(t); break; }
        }

        ComboBox<MotivoMovimiento> cmbMotivo = new ComboBox<>();
        cmbMotivo.setPromptText("Motivo");
        cmbMotivo.getItems().addAll(inventoryAdapter.listarMotivosMovimiento());
        cmbMotivo.setConverter(new StringConverter<>() {
            @Override public String toString(MotivoMovimiento m) { return m != null ? m.getNombre() : ""; }
            @Override public MotivoMovimiento fromString(String s) { return null; }
        });
        for (MotivoMovimiento m : cmbMotivo.getItems()) {
            if (m.getId().equals(mov.getMotivoId())) { cmbMotivo.getSelectionModel().select(m); break; }
        }

        TextField txtCantidad = new TextField(String.valueOf(mov.getCantidad()));
        txtCantidad.setPromptText("Cantidad");

        TextArea txtObs = new TextArea(mov.getObservacion() != null ? mov.getObservacion() : "");
        txtObs.setPromptText("Observaciones");
        txtObs.setWrapText(true);
        txtObs.setPrefHeight(60);

        VBox content = new VBox(10,
            lblInfo,
            new Label("Tipo de Movimiento:"), cmbTipo,
            new Label("Motivo:"), cmbMotivo,
            new Label("Cantidad:"), txtCantidad,
            new Label("Observación:"), txtObs
        );
        content.setStyle("-fx-padding: 15;");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().setMinWidth(420);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                if (cmbTipo.getSelectionModel().getSelectedItem() == null || cmbMotivo.getSelectionModel().getSelectedItem() == null) {
                    return null;
                }
                return new String[]{
                    cmbTipo.getSelectionModel().getSelectedItem().getId(),
                    cmbMotivo.getSelectionModel().getSelectedItem().getId(),
                    txtCantidad.getText().trim(),
                    txtObs.getText()
                };
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                String tipoId = result[0];
                String motivoId = result[1];
                int cantidad = Integer.parseInt(result[2]);
                String obs = result[3];

                inventoryAdapter.editarMovimiento(mov, tipoId, motivoId, cantidad, obs);
                showAlert(Alert.AlertType.INFORMATION, "Movimiento Editado",
                    "El movimiento se ha actualizado correctamente. El stock ha sido ajustado.");
                refreshMovements();
                refreshBatches();
                populateAlerts();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido.");
            } catch (IllegalStateException ex) {
                showAlert(Alert.AlertType.ERROR, "Error al Editar", ex.getMessage());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo editar: " + ex.getMessage());
            }
        });
    }

    private void setupExpirationCellFactory() {
        colBatchExp.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    long daysToExpiry = item.toEpochDay() - LocalDate.now().toEpochDay();
                    
                    // RF-10: Read threshold from combo (default 30/90)
                    int thresholdDanger = 30;
                    int thresholdWarning = 90;
                    if (cmbExpirationThreshold != null && cmbExpirationThreshold.getValue() != null) {
                        thresholdDanger = cmbExpirationThreshold.getValue();
                        thresholdWarning = thresholdDanger * 3;
                    }
                    
                    HBox box = new HBox(8);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label label = new Label(item.toString());
                    FontIcon icon = new FontIcon();
                    
                    if (daysToExpiry <= thresholdDanger) {
                        icon.setIconLiteral("fas-exclamation-circle");
                        icon.getStyleClass().add("icon-danger");
                        label.getStyleClass().add("text-danger");
                        box.getChildren().addAll(icon, label);
                        setGraphic(box);
                    } else if (daysToExpiry <= thresholdWarning) {
                        icon.setIconLiteral("fas-exclamation-triangle");
                        icon.getStyleClass().add("icon-warning");
                        label.setStyle("-fx-text-fill: -color-warning-fg;");
                        box.getChildren().addAll(icon, label);
                        setGraphic(box);
                    } else {
                        setGraphic(null);
                        setText(item.toString());
                    }
                }
            }
        });
    }

    private void refreshQuickBatchCombo() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user != null) {
                List<Lote> batches = inventoryAdapter.listarLotesConProducto(user.getSedeId());
                cmbQuickBatch.setItems(FXCollections.observableArrayList(batches));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML protected void onOpenMovementModal() { modalMovement.setVisible(true); }
    @FXML protected void onCloseModal() { modalMovement.setVisible(false); }

    @FXML
    protected void onModalProductChanged() {
        TipoMovimiento selectedType = cmbModalType.getValue();
        if (selectedType != null && selectedType.getNombre().toLowerCase().contains("salida")) {
            loadBatchesForProduct(cmbModalProduct.getValue());
        }
    }

    @FXML
    protected void onModalTypeChanged() {
        TipoMovimiento selectedType = cmbModalType.getValue();
        if (selectedType == null) return;

        boolean isEntrada = selectedType.getNombre().toLowerCase().contains("entrada");
        vboxEntradaDetails.setVisible(isEntrada);
        vboxEntradaDetails.setManaged(isEntrada);
        cmbModalBatchContainer.setVisible(!isEntrada);
        cmbModalBatchContainer.setManaged(!isEntrada);
        
        try {
            List<MotivoMovimiento> motivos = inventoryAdapter.listarMotivosMovimiento();
            cmbModalMotivo.setItems(FXCollections.observableArrayList(motivos));
        } catch (Exception e) { e.printStackTrace(); }
        
        if (!isEntrada) loadBatchesForProduct(cmbModalProduct.getValue());
    }

    private void loadBatchesForProduct(Producto p) {
        if (p == null) return;
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            String sedeId = user.getSedeId();
            List<Lote> lotes = inventoryAdapter.listarLotesFefo(sedeId, p.getId());
            cmbModalBatch.setItems(FXCollections.observableArrayList(lotes));
            cmbModalBatch.setConverter(new StringConverter<>() {
                @Override public String toString(Lote l) { return l != null ? l.getNumeroLote() : ""; }
                @Override public Lote fromString(String s) { return null; }
            });
            cmbModalBatch.setCellFactory(cb -> new javafx.scene.control.ListCell<>() {
                @Override
                protected void updateItem(Lote item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.getNumeroLote() + " (vence: " + item.getFechaVencimiento() + ")");
                        if (getIndex() == 0) {
                            setStyle("-fx-font-weight: bold; -fx-text-fill: #2ea043;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    protected void onSaveMovement() {
        try {
            // Validar que la sede no esté bloqueada
            SedeAccessValidator.validarSedeActiva();
            
            Producto producto = cmbModalProduct.getValue();
            TipoMovimiento selectedType = cmbModalType.getValue();
            MotivoMovimiento motivo = cmbModalMotivo.getValue();
            String qtyStr = txtModalQty.getText();

            if (producto == null || selectedType == null || motivo == null || qtyStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor complete todos los campos obligatorios.");
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(qtyStr);
                if (cantidad <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser un número entero mayor a cero.");
                return;
            }

            Usuario user = sessionManager.getCurrentUser();
            String obs = txtModalObs.getText();
            boolean isEntrada = selectedType.getNombre().toLowerCase().contains("entrada");

            if (isEntrada) {
                Lote nuevoLote = new Lote();
                nuevoLote.setProductoId(producto.getId());
                nuevoLote.setSedeId(user.getSedeId());
                nuevoLote.setNumeroLote(txtNewLote.getText());
                nuevoLote.setFechaFabricacion(dpNewFab.getValue());
                nuevoLote.setFechaVencimiento(dpNewVenc.getValue());
                nuevoLote.setCantidad(cantidad);
                
                if (nuevoLote.getNumeroLote() == null || nuevoLote.getNumeroLote().isBlank() || nuevoLote.getFechaVencimiento() == null) {
                    showAlert(Alert.AlertType.WARNING, "Datos de Lote", "Debe ingresar el N° de Lote y la Fecha de Vencimiento.");
                    return;
                }

                inventoryAdapter.registrarMovimiento(nuevoLote, user.getId(), selectedType.getId(), motivo.getId(), cantidad, obs);
            } else {
                Lote loteExistente = cmbModalBatch.getValue();
                if (loteExistente == null) {
                    showAlert(Alert.AlertType.WARNING, "Lote no seleccionado", "Debe seleccionar un lote para realizar una salida.");
                    return;
                }
                inventoryAdapter.registrarMovimiento(loteExistente, user.getId(), selectedType.getId(), motivo.getId(), cantidad, obs);
            }

            showAlert(Alert.AlertType.INFORMATION, "Operación Exitosa", "El movimiento se ha registrado correctamente.");
            onCloseModal();
            refreshMovements();
            refreshBatches();
            refreshQuickBatchCombo();
            clearModalFields();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Inesperado", "Ocurrió un error al procesar el movimiento.");
        }
    }

    private void clearModalFields() {
        txtModalQty.clear();
        txtModalObs.clear();
        txtNewLote.clear();
        dpNewFab.setValue(null);
        dpNewVenc.setValue(null);
        cmbModalProduct.getSelectionModel().clearSelection();
        cmbModalType.getSelectionModel().clearSelection();
        cmbModalMotivo.getSelectionModel().clearSelection();
        cmbModalBatch.getSelectionModel().clearSelection();
    }

    @FXML
    protected void onQuickUpdate() {
        try {
            // Validar que la sede no esté bloqueada
            SedeAccessValidator.validarSedeActiva();
            
            TipoMovimiento selectedType = cmbQuickType.getValue();
            Lote lote = cmbQuickBatch.getValue();
            String qtyStr = txtQuickQty.getText();

            if (selectedType == null || lote == null || qtyStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor complete los campos de la actualización rápida.");
                return;
            }

            int cantidad;
            try {
                cantidad = Integer.parseInt(qtyStr);
                if (cantidad <= 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Validación", "La cantidad debe ser un número entero mayor a cero.");
                return;
            }

            TipoMovimientoEnum tipoEnum = TipoMovimientoEnum.fromId(selectedType.getId());
            boolean isEntrada = tipoEnum == TipoMovimientoEnum.ENTRADA;
            MotivoMovimiento selectedMotivo = cmbQuickMotivo.getValue();
            String motivoId = selectedMotivo != null ? selectedMotivo.getId()
                : (isEntrada ? MotivoMovimientoEnum.COMPRA.getId() : MotivoMovimientoEnum.MERMA.getId());
            Usuario user = sessionManager.getCurrentUser();

            inventoryAdapter.registrarMovimiento(lote, user.getId(), selectedType.getId(), motivoId, cantidad, txtQuickObs.getText());

            showAlert(Alert.AlertType.INFORMATION, "Stock Actualizado", "Se ha actualizado el stock del lote seleccionado.");
            txtQuickQty.clear();
            txtQuickObs.clear();
            refreshMovements();
            refreshBatches();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo realizar la actualización rápida.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        alert.initOwner(inventoryTabPane.getScene().getWindow());
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/com/utp/meditrackapp/styles/global.css").toExternalForm());
        alert.showAndWait();
    }

    @FXML
    protected void onFilterMovements() {
        refreshMovements();
    }

    private void refreshMovements() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            
            String searchText = txtSearchMovement != null ? txtSearchMovement.getText() : "";
            TipoMovimiento selectedType = cmbMovementType != null ? cmbMovementType.getValue() : null;
            String tipoId = (selectedType != null && !selectedType.getId().isEmpty()) ? selectedType.getId() : null;
            LocalDate desde = dpFromDate != null ? dpFromDate.getValue() : null;
            LocalDate hasta = dpToDate != null ? dpToDate.getValue() : null;

            List<Movimiento> movements = inventoryAdapter.listarMovimientosConFiltros(user.getSedeId(), tipoId, searchText, desde, hasta); 
            allMovements.setAll(movements);
            
            int totalItems = allMovements.size();
            int pageCount = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
            if (pageCount < 1) pageCount = 1;
            
            paginationMovements.setPageCount(pageCount);
            paginationMovements.currentPageIndexProperty().set(0);
            paginationMovements.setPageFactory(this::createMovementsPage);

            // Update KPIs
            int total = movements.size();
            int entry = movements.stream().filter(m -> "ENTRADA".equalsIgnoreCase(m.getTipoNombre())).mapToInt(Movimiento::getCantidad).sum();
            int exit = movements.stream().filter(m -> "SALIDA".equalsIgnoreCase(m.getTipoNombre())).mapToInt(Movimiento::getCantidad).sum();

            if (lblTotalMovs != null) lblTotalMovs.setText(String.valueOf(total));
            if (lblEntryQty != null) lblEntryQty.setText(String.valueOf(entry));
            if (lblExitQty != null) lblExitQty.setText(String.valueOf(exit));

        } catch (Exception e) { e.printStackTrace(); }
    }

    private javafx.scene.Node createMovementsPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allMovements.size());
        
        ObservableList<Movimiento> pageItems = FXCollections.observableArrayList();
        if (fromIndex < toIndex) {
            pageItems.addAll(allMovements.subList(fromIndex, toIndex));
        }
        tableMovements.setItems(pageItems);
        return new VBox();
    }

    @FXML
    protected void onFilterBatches() {
        refreshBatches();
    }

    private void refreshBatches() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            
            List<Lote> batches = inventoryAdapter.listarLotesConProducto(user.getSedeId());
            
            // Apply Filter
            String query = (txtSearchBatch != null) ? txtSearchBatch.getText().toLowerCase().trim() : "";
            if (!query.isEmpty()) {
                String[] terms = query.split("\\s+");
                batches = batches.stream().filter(l -> {
                    String data = (l.getProductoNombre() + " " + l.getNumeroLote() + " " + (l.getCodigoDigemid() != null ? l.getCodigoDigemid() : "")).toLowerCase();
                    for (String term : terms) {
                        if (!data.contains(term)) return false;
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            allBatches.setAll(batches);
            
            int totalItems = allBatches.size();
            int pageCount = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
            if (pageCount < 1) pageCount = 1;
            
            paginationBatches.setPageCount(pageCount);
            paginationBatches.currentPageIndexProperty().set(0);
            paginationBatches.setPageFactory(this::createBatchesPage);
            
            updateBatchSummary(user.getSedeId(), batches);
            populateAlerts();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private javafx.scene.Node createBatchesPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, allBatches.size());
        
        ObservableList<Lote> pageItems = FXCollections.observableArrayList();
        if (fromIndex < toIndex) {
            pageItems.addAll(allBatches.subList(fromIndex, toIndex));
        }
        tableBatches.setItems(pageItems);
        return new VBox();
    }

    @FXML
    protected void onGenerateReport() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;

            LocalDate desde = dpFromDate.getValue();
            LocalDate hasta = dpToDate.getValue();
            String searchText = txtSearchMovement.getText();
            TipoMovimiento selectedType = cmbMovementType.getValue();
            String tipoId = (selectedType != null && !selectedType.getId().isEmpty()) ? selectedType.getId() : null;

            List<Movimiento> movements = inventoryAdapter.listarMovimientosConFiltros(user.getSedeId(), tipoId, searchText, desde, hasta);

            if (movements.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Reporte Vacío", "No hay movimientos que coincidan con los filtros seleccionados.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Movimientos");
            fileChooser.setInitialFileName("reporte_movimientos_" + LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(tableMovements.getScene().getWindow());

            if (file != null) {
                Map<String, Object> params = new HashMap<>();
                params.put("REPORT_DATE", java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                params.put("GENERATED_BY", user.getNombres() + " " + user.getApellidos());
                params.put("SEDE", com.utp.meditrackapp.core.util.SedeResolver.getSedeName(user));
                
                String period = (desde != null ? desde.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio") 
                              + " al " + 
                              (hasta != null ? hasta.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoy");
                params.put("DATE_RANGE", period);
                
                params.put("TOTAL_MOVEMENTS", String.valueOf(movements.size()));
                
                int totalQty = movements.stream().mapToInt(Movimiento::getCantidad).sum();
                params.put("TOTAL_QUANTITY", String.valueOf(totalQty));
                params.put("items", movements);

                reportService.generarPdf("movimientos", params, file);
                showAlert(Alert.AlertType.INFORMATION, "Reporte Generado", "El reporte modernizado (HTML/CSS) se ha guardado exitosamente en:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al Generar Reporte", "Ocurrió un error: " + e.getMessage());
        }
    }

    private void populateAlerts() {
        if (alertsContainer == null) return;
        alertsContainer.getChildren().clear();
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            List<StockCriticoDTO> criticos = inventoryAdapter.obtenerStockCritico(user.getSedeId());

            boolean hasAlerts = !criticos.isEmpty();
            alertsContainer.setVisible(hasAlerts);
            alertsContainer.setManaged(hasAlerts);

            for (StockCriticoDTO item : criticos) {
                HBox alertCard = new HBox(10);
                alertCard.setStyle("-fx-padding: 8 12; -fx-background-radius: 6; -fx-alignment: center-left;");

                FontIcon icon = new FontIcon();
                String bgColor;
                if (item.isStockBajo() && item.isVencePronto()) {
                    icon.setIconLiteral("fas-exclamation-triangle");
                    bgColor = "-fx-background-color: #fce4e4; -fx-border-color: #e74c3c;";
                } else if (item.isStockBajo()) {
                    icon.setIconLiteral("fas-box-open");
                    bgColor = "-fx-background-color: #fff3cd; -fx-border-color: #ffc107;";
                } else {
                    icon.setIconLiteral("fas-clock");
                    bgColor = "-fx-background-color: #d1ecf1; -fx-border-color: #17a2b8;";
                }
                icon.setIconSize(18);
                alertCard.setStyle(alertCard.getStyle() + bgColor);

                String riesgo = switch (item.getNivelRiesgo()) {
                    case "CRITICO" -> "CRITICO";
                    case "STOCK_BAJO" -> "Stock Bajo";
                    case "VENCIMIENTO_INMINENTE" -> "Vence pronto";
                    case "POR_VENCER" -> "Por vencer";
                    default -> "Alerta";
                };

                String prodName = item.getProductoNombre() != null ? item.getProductoNombre() : "(Producto desconocido)";
                Label text = new Label(riesgo + ": " + prodName
                    + " (Stock: " + item.getStockActual() + "/" + item.getStockMinimo() + ")");
                text.setStyle("-fx-font-size: 12px;");
                alertCard.getChildren().addAll(icon, text);
                alertsContainer.getChildren().add(alertCard);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateBatchSummary(String sedeId, List<Lote> batches) {
        if (lblActiveBatches == null || lblCriticalBatches == null) return;
        try {
            List<StockCriticoDTO> criticos = inventoryAdapter.obtenerStockCritico(sedeId);
            long active = batches.stream().filter(l -> l.getCantidad() > 0).count();
            long expiring = criticos.stream().filter(StockCriticoDTO::isVencePronto).count();
            long lowStock = criticos.stream().filter(StockCriticoDTO::isStockBajo).count();
            lblActiveBatches.setText(String.valueOf(active));
            lblCriticalBatches.setText("Por vencer: " + expiring + " | Stock bajo: " + lowStock);
        } catch (Exception e) { e.printStackTrace(); }
    }
}
