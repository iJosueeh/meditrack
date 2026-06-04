package com.utp.meditrackapp.features.catalogs.ui;

import com.utp.meditrackapp.core.dao.MotivoMovimientoDAO;
import com.utp.meditrackapp.core.dao.TipoMovimientoDAO;
import com.utp.meditrackapp.core.models.entity.MotivoMovimiento;
import com.utp.meditrackapp.core.models.entity.TipoMovimiento;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CatalogoMovimientosController {

    // Tab Tipos
    @FXML private TableView<TipoMovimiento> tableTipos;
    @FXML private TableColumn<TipoMovimiento, String> colIdTipo, colNombreTipo;
    @FXML private TableColumn<TipoMovimiento, Void> colAccionesTipo;
    @FXML private TextField txtSearchTipo;

    // Tab Motivos
    @FXML private TableView<MotivoMovimiento> tableMotivos;
    @FXML private TableColumn<MotivoMovimiento, String> colIdMotivo, colNombreMotivo;
    @FXML private TableColumn<MotivoMovimiento, Void> colAccionesMotivo;
    @FXML private TextField txtSearchMotivo;

    // Modal
    @FXML private StackPane modalCatalogo;
    @FXML private Label modalTitle, modalSubtitle, lblFieldName;
    @FXML private TextField txtNombre;

    private final TipoMovimientoDAO tipoDAO = new TipoMovimientoDAO();
    private final MotivoMovimientoDAO motivoDAO = new MotivoMovimientoDAO();
    
    private final ObservableList<TipoMovimiento> masterTipos = FXCollections.observableArrayList();
    private final ObservableList<MotivoMovimiento> masterMotivos = FXCollections.observableArrayList();
    
    private Object selectedItem; // Can be TipoMovimiento or MotivoMovimiento
    private boolean isEditingTipo = true;

    @FXML
    public void initialize() {
        setupTableTipos();
        setupTableMotivos();
        loadTipos();
        loadMotivos();
        setupSearch();
    }

    private void setupTableTipos() {
        colIdTipo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreTipo.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAccionesTipo.setCellFactory(column -> createActionCell(true));
    }

    private void setupTableMotivos() {
        colIdMotivo.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombreMotivo.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAccionesMotivo.setCellFactory(column -> createActionCell(false));
    }

    private <T> TableCell<T, Void> createActionCell(boolean isTipo) {
        return new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deleteBtn = new Button();
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setOnAction(event -> {
                    T item = getTableRow().getItem();
                    if (item != null) openEditModal(item, isTipo);
                });

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("button", "flat", "danger", "sm");
                deleteBtn.setOnAction(event -> {
                    T item = getTableRow().getItem();
                    if (item != null) confirmDelete(item, isTipo);
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox box = new HBox(10, editBtn, deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        };
    }

    private void setupSearch() {
        FilteredList<TipoMovimiento> filteredTipos = new FilteredList<>(masterTipos, p -> true);
        txtSearchTipo.textProperty().addListener((obs, old, val) -> {
            filteredTipos.setPredicate(t -> val == null || val.isBlank() || 
                t.getNombre().toLowerCase().contains(val.toLowerCase()) || t.getId().toLowerCase().contains(val.toLowerCase()));
        });
        tableTipos.setItems(filteredTipos);

        FilteredList<MotivoMovimiento> filteredMotivos = new FilteredList<>(masterMotivos, p -> true);
        txtSearchMotivo.textProperty().addListener((obs, old, val) -> {
            filteredMotivos.setPredicate(m -> val == null || val.isBlank() || 
                m.getNombre().toLowerCase().contains(val.toLowerCase()) || m.getId().toLowerCase().contains(val.toLowerCase()));
        });
        tableMotivos.setItems(filteredMotivos);
    }

    @FXML public void loadTipos() {
        try { masterTipos.setAll(tipoDAO.listarTodas()); } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML public void loadMotivos() {
        try { masterMotivos.setAll(motivoDAO.listarTodas()); } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML protected void onOpenRegisterTipo() {
        prepareModal("Registrar Tipo de Movimiento", "Ingrese el nombre del nuevo tipo.", "NOMBRE DEL TIPO", true, null);
    }

    @FXML protected void onOpenRegisterMotivo() {
        prepareModal("Registrar Motivo de Movimiento", "Ingrese el nombre del nuevo motivo.", "NOMBRE DEL MOTIVO", false, null);
    }

    private void openEditModal(Object item, boolean isTipo) {
        String title = isTipo ? "Editar Tipo de Movimiento" : "Editar Motivo de Movimiento";
        String label = isTipo ? "NOMBRE DEL TIPO" : "NOMBRE DEL MOTIVO";
        String name = isTipo ? ((TipoMovimiento)item).getNombre() : ((MotivoMovimiento)item).getNombre();
        prepareModal(title, "Modifique el nombre del registro.", label, isTipo, item);
        txtNombre.setText(name);
    }

    private void prepareModal(String title, String subtitle, String fieldLabel, boolean isTipo, Object item) {
        modalTitle.setText(title);
        modalSubtitle.setText(subtitle);
        lblFieldName.setText(fieldLabel);
        isEditingTipo = isTipo;
        selectedItem = item;
        txtNombre.clear();
        modalCatalogo.setVisible(true);
    }

    @FXML protected void onSave() {
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.isBlank()) { showAlert("Validación", "El nombre es obligatorio."); return; }

        try {
            if (isEditingTipo) {
                if (selectedItem == null) tipoDAO.crear(new TipoMovimiento(null, nombre));
                else {
                    TipoMovimiento t = (TipoMovimiento) selectedItem;
                    t.setNombre(nombre);
                    tipoDAO.actualizar(t);
                }
                loadTipos();
            } else {
                if (selectedItem == null) motivoDAO.crear(new MotivoMovimiento(null, nombre));
                else {
                    MotivoMovimiento m = (MotivoMovimiento) selectedItem;
                    m.setNombre(nombre);
                    motivoDAO.actualizar(m);
                }
                loadMotivos();
            }
            onCloseModal();
            showAlert("Éxito", "Registro guardado correctamente.");
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    private void confirmDelete(Object item, boolean isTipo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Está seguro de eliminar este registro?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Confirmar Eliminación");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    if (isTipo) tipoDAO.eliminar(((TipoMovimiento)item).getId());
                    else motivoDAO.eliminar(((MotivoMovimiento)item).getId());
                    loadTipos(); loadMotivos();
                    showAlert("Éxito", "Registro eliminado.");
                } catch (SQLException e) { showAlert("Error", "No se puede eliminar porque está en uso."); }
            }
        });
    }

    @FXML protected void onCloseModal() { modalCatalogo.setVisible(false); }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}
