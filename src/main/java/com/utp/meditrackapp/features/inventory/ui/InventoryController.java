package com.utp.meditrackapp.features.inventory.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.dto.StockCriticoItem;
import com.utp.meditrackapp.core.models.entity.*;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.features.inventory.service.InventarioService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

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
    @FXML private TableColumn<Lote, String> colBatchId, colBatchProduct, colBatchNum;
    @FXML private TableColumn<Lote, LocalDate> colBatchExp;
    @FXML private TableColumn<Lote, Integer> colBatchQty;
    @FXML private Label lblActiveBatches, lblCriticalBatches;

    // Modal fields
    @FXML private StackPane modalMovement;
    @FXML private VBox vboxEntradaDetails;
    @FXML private ComboBox<Producto> cmbModalProduct;
    @FXML private ComboBox<String> cmbModalType;
    @FXML private ComboBox<MotivoMovimiento> cmbModalMotivo;
    @FXML private ComboBox<Lote> cmbModalBatch;
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

        colBatchId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colBatchProduct.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colBatchNum.setCellValueFactory(new PropertyValueFactory<>("numeroLote"));
        colBatchExp.setCellValueFactory(new PropertyValueFactory<>("fechaVencimiento"));
        colBatchQty.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
    }

    private void loadInitialData() {
        try {
            List<Producto> productos = inventarioService.listarProductosActivos();
            cmbModalProduct.setItems(FXCollections.observableArrayList(productos));
            cmbModalType.setItems(FXCollections.observableArrayList(
                TipoMovimientoEnum.ENTRADA.name(), 
                TipoMovimientoEnum.SALIDA.name()
            ));
            
            cmbModalProduct.setConverter(new StringConverter<>() {
                @Override public String toString(Producto p) { return p != null ? p.getNombre() : ""; }
                @Override public Producto fromString(String s) { return null; }
            });
            cmbModalMotivo.setConverter(new StringConverter<>() {
                @Override public String toString(MotivoMovimiento m) { return m != null ? m.getNombre() : ""; }
                @Override public MotivoMovimiento fromString(String s) { return null; }
            });
        } catch (Exception e) { e.printStackTrace(); }
        refreshMovements();
        refreshBatches();
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
        cmbModalBatch.setVisible(!isEntrada);
        cmbModalBatch.setManaged(!isEntrada);
        
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
        System.out.println("Guardando movimiento...");
        // TODO: Implement actual save logic using inventarioService.registrarEntrada/Merma
        onCloseModal();
        refreshMovements();
        refreshBatches();
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