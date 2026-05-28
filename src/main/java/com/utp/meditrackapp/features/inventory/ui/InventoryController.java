package com.utp.meditrackapp.features.inventory.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.models.entity.*;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.features.inventory.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InventoryController {

    private final InventarioService inventarioService = new InventarioService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML private TabPane inventoryTabPane;
    @FXML private TableView<Movimiento> tableMovements;
    @FXML private TableView<Lote> tableBatches;
    @FXML private VBox alertsContainer;
    
    // Columns
    @FXML private TableColumn<Movimiento, String> colMovDate, colMovType, colMovProduct, colMovBatch, colMovReason;
    @FXML private TableColumn<Movimiento, Integer> colMovQty;
    @FXML private TableColumn<Lote, String> colBatchDigemid, colBatchProduct, colBatchNum;
    @FXML private TableColumn<Lote, LocalDate> colBatchExp, colBatchFab;
    @FXML private Label lblActiveBatches, lblCriticalBatches;

    // Quick Registration
    @FXML private ComboBox<String> cmbQuickType;
    @FXML private ComboBox<Lote> cmbQuickBatch;
    @FXML private TextField txtQuickQty, txtQuickObs;

    // Modal fields
    @FXML private StackPane modalMovement;
    @FXML private VBox vboxEntradaDetails;
    @FXML private ComboBox<Producto> cmbModalProduct;
    @FXML private ComboBox<String> cmbModalType;
    @FXML private ComboBox<MotivoMovimiento> cmbModalMotivo;
    @FXML private ComboBox<Lote> cmbModalBatch;
    @FXML private VBox cmbModalBatchContainer;
    @FXML private TextField txtNewLote, txtModalQty;
    @FXML private TextArea txtModalObs;
    @FXML private DatePicker dpNewVenc, dpNewFab;

    @FXML
    public void initialize() {
        setupTables();
        loadInitialData();
    }

    private void setupTables() {
        colMovDate.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));
        colMovType.setCellValueFactory(new PropertyValueFactory<>("tipoNombre"));
        colMovProduct.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colMovBatch.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colMovQty.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMovReason.setCellValueFactory(new PropertyValueFactory<>("motivoNombre"));

        setupMovementCellFactories();

        colBatchDigemid.setCellValueFactory(new PropertyValueFactory<>("codigoDigemid"));
        colBatchProduct.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colBatchNum.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colBatchFab.setCellValueFactory(new PropertyValueFactory<>("fechaFabricacion"));
        colBatchExp.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));

        setupExpirationCellFactory();
    }

    private void setupMovementCellFactories() {
        // Factory para Tipo de Movimiento (Entrada/Salida)
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

        // Factory para Motivo de Movimiento
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
                    
                    HBox box = new HBox(8);
                    box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    Label label = new Label(item.toString());
                    FontIcon icon = new FontIcon();
                    
                    if (daysToExpiry <= 30) {
                        icon.setIconLiteral("fas-exclamation-circle");
                        icon.getStyleClass().add("icon-danger");
                        label.getStyleClass().add("text-danger");
                        box.getChildren().addAll(icon, label);
                        setGraphic(box);
                    } else if (daysToExpiry <= 90) {
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

    private void loadInitialData() {
        try {
            List<Producto> productos = inventarioService.listarProductosActivos();
            cmbModalProduct.setItems(FXCollections.observableArrayList(productos));
            
            ObservableList<String> types = FXCollections.observableArrayList(
                TipoMovimientoEnum.ENTRADA.name(), 
                TipoMovimientoEnum.SALIDA.name()
            );
            cmbModalType.setItems(types);
            cmbQuickType.setItems(types);
            
            cmbModalProduct.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p != null ? p.getNombre() : ""; }
                @Override public Producto fromString(String s) { return null; }
            });
            cmbModalMotivo.setConverter(new StringConverter<>() {
                @Override public String toString(MotivoMovimiento m) { return m != null ? m.getNombre() : ""; }
                @Override public MotivoMovimiento fromString(String s) { return null; }
            });

            cmbQuickBatch.setConverter(new StringConverter<>() {
                @Override public String toString(Lote l) { return l != null ? l.getProductoNombre() + " [" + l.getNumeroLote() + "]" : ""; }
                @Override public Lote fromString(String s) { return null; }
            });

            // Populate Quick Batch combo
            refreshQuickBatchCombo();

        } catch (Exception e) { e.printStackTrace(); }
        refreshMovements();
        refreshBatches();
    }

    private void refreshQuickBatchCombo() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user != null) {
                List<Lote> batches = inventarioService.listarLotesConProducto(user.getSedeId());
                cmbQuickBatch.setItems(FXCollections.observableArrayList(batches));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    protected void onOpenMovementModal() {
        modalMovement.setVisible(true);
    }

    @FXML
    protected void onCloseModal() {
        modalMovement.setVisible(false);
    }

    @FXML
    protected void onModalProductChanged() {
        if (TipoMovimientoEnum.SALIDA.name().equals(cmbModalType.getValue())) {
            loadBatchesForProduct(cmbModalProduct.getValue());
        }
    }

    @FXML
    protected void onModalTypeChanged() {
        boolean isEntrada = TipoMovimientoEnum.ENTRADA.name().equals(cmbModalType.getValue());
        vboxEntradaDetails.setVisible(isEntrada);
        vboxEntradaDetails.setManaged(isEntrada);
        cmbModalBatchContainer.setVisible(!isEntrada);
        cmbModalBatchContainer.setManaged(!isEntrada);
        
        List<MotivoMovimiento> motivos = isEntrada ? 
            List.of(
                new MotivoMovimiento(MotivoMovimientoEnum.COMPRA.getId(), "Compra"), 
                new MotivoMovimiento(MotivoMovimientoEnum.TRANSFERENCIA.getId(), "Transferencia")
            ) :
            List.of(
                new MotivoMovimiento(MotivoMovimientoEnum.ATENCION.getId(), "Atención"), 
                new MotivoMovimiento(MotivoMovimientoEnum.MERMA.getId(), "Merma")
            );
        cmbModalMotivo.setItems(FXCollections.observableArrayList(motivos));
        
        if (!isEntrada) loadBatchesForProduct(cmbModalProduct.getValue());
    }

    private void loadBatchesForProduct(Producto p) {
        if (p == null) return;
        try {
            String sedeId = sessionManager.getCurrentUser().getSedeId();
            List<Lote> lotes = inventarioService.listarLotesFefo(sedeId, p.getId());
            cmbModalBatch.setItems(FXCollections.observableArrayList(lotes));
            cmbModalBatch.setConverter(new StringConverter<>() {
                @Override public String toString(Lote l) { return l != null ? l.getNumeroLote() : ""; }
                @Override public Lote fromString(String s) { return null; }
            });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    protected void onSaveMovement() {
        try {
            Producto producto = cmbModalProduct.getValue();
            String tipoStr = cmbModalType.getValue();
            MotivoMovimiento motivo = cmbModalMotivo.getValue();
            String qtyStr = txtModalQty.getText();

            if (producto == null || tipoStr == null || motivo == null || qtyStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor complete todos los campos obligatorios.");
                return;
            }

            int cantidad = Integer.parseInt(qtyStr);
            TipoMovimientoEnum tipo = TipoMovimientoEnum.valueOf(tipoStr);
            Usuario user = sessionManager.getCurrentUser();
            String obs = txtModalObs.getText();

            boolean success;
            if (tipo == TipoMovimientoEnum.ENTRADA) {
                Lote nuevoLote = new Lote();
                nuevoLote.setProductoId(producto.getId());
                nuevoLote.setSedeId(user.getSedeId());
                nuevoLote.setNumeroLote(txtNewLote.getText());
                nuevoLote.setFechaFabricacion(dpNewFab.getValue());
                nuevoLote.setFechaVencimiento(dpNewVenc.getValue());
                nuevoLote.setCantidad(cantidad);
                
                success = inventarioService.registrarMovimiento(nuevoLote, user.getId(), tipo, 
                        MotivoMovimientoEnum.valueOf(motivo.getNombre().toUpperCase()), cantidad, obs);
            } else {
                Lote loteExistente = cmbModalBatch.getValue();
                if (loteExistente == null) {
                    showAlert(Alert.AlertType.WARNING, "Lote no seleccionado", "Debe seleccionar un lote para realizar una salida.");
                    return;
                }
                success = inventarioService.registrarMovimiento(loteExistente, user.getId(), tipo, 
                        MotivoMovimientoEnum.valueOf(motivo.getNombre().toUpperCase().replace("ATENCIÓN", "ATENCION")), cantidad, obs);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Operación Exitosa", "El movimiento se ha registrado correctamente.");
                onCloseModal();
                refreshMovements();
                refreshBatches();
                refreshQuickBatchCombo();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo registrar el movimiento. Verifique el stock disponible.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Formato", "La cantidad debe ser un número entero válido.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error Inesperado", e.getMessage());
        }
    }

    @FXML
    protected void onQuickUpdate() {
        try {
            String tipoStr = cmbQuickType.getValue();
            Lote lote = cmbQuickBatch.getValue();
            String qtyStr = txtQuickQty.getText();

            if (tipoStr == null || lote == null || qtyStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Campos Incompletos", "Por favor complete los campos de la actualización rápida.");
                return;
            }

            int cantidad = Integer.parseInt(qtyStr);
            TipoMovimientoEnum tipo = TipoMovimientoEnum.valueOf(tipoStr);
            MotivoMovimientoEnum motivo = (tipo == TipoMovimientoEnum.ENTRADA) ? MotivoMovimientoEnum.COMPRA : MotivoMovimientoEnum.MERMA;
            Usuario user = sessionManager.getCurrentUser();

            boolean success = inventarioService.registrarMovimiento(lote, user.getId(), tipo, motivo, cantidad, txtQuickObs.getText());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Stock Actualizado", "Se ha actualizado el stock del lote seleccionado.");
                txtQuickQty.clear();
                txtQuickObs.clear();
                refreshMovements();
                refreshBatches();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el stock.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Error de Formato", "La cantidad debe ser un número entero válido.");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
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
            List<Movimiento> movements = inventarioService.listarMovimientos(user.getSedeId(), null, ""); 
            tableMovements.setItems(FXCollections.observableArrayList(movements));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void refreshBatches() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            List<Lote> batches = inventarioService.listarLotesConProducto(user.getSedeId());
            tableBatches.setItems(FXCollections.observableArrayList(batches));
            updateBatchSummary(user.getSedeId(), batches);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void updateBatchSummary(String sedeId, List<Lote> batches) {
        if (lblActiveBatches == null || lblCriticalBatches == null) return;
        
        try {
            // Ya no filtramos manualmente en Java, usamos el servicio que encapsula la lógica
            List<StockCriticoItem> criticos = inventarioService.obtenerStockCritico(sedeId);
            
            long active = batches.stream().filter(l -> l.getCantidad() > 0).count();
            long critical = criticos.stream().filter(StockCriticoItem::isVencePronto).count();
            
            lblActiveBatches.setText(String.valueOf(active));
            lblCriticalBatches.setText(String.valueOf(critical));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}