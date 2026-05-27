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
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AtencionController {

    private final AtencionService atencionService = new AtencionService();
    private final InventarioService inventarioService = new InventarioService();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML private TextField txtPacienteDni, txtCantidad;
    @FXML private ComboBox<Producto> cmbProducto;
    @FXML private Label lblLotesTitulo;
    @FXML private TableView<Lote> tableLotes;
    @FXML private TableColumn<Lote, String> colLoteNum, colLoteUbicacion, colLoteAccion;
    @FXML private TableColumn<Lote, LocalDate> colLoteVenc;
    @FXML private TableColumn<Lote, Integer> colLoteStock;

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
    protected void onProductoSelected() {
        Producto p = cmbProducto.getValue();
        if (p != null) {
            lblLotesTitulo.setText("Lotes disponibles: \"" + p.getNombre() + "\"");
            try {
                String sedeId = sessionManager.getCurrentUser().getSedeId();
                List<Lote> lotes = inventarioService.listarLotesFefo(sedeId, p.getId());
                tableLotes.setItems(FXCollections.observableArrayList(lotes));
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    @FXML
    protected void onConfirmarEntrega() {
        // Lógica de confirmación pendiente
        System.out.println("Entrega confirmada para paciente: " + txtPacienteDni.getText());
    }
}
