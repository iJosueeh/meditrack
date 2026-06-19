package com.utp.meditrackapp.features.catalogs.ui;

import com.utp.meditrackapp.infrastructure.adapters.CatalogAdapter;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
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

    private final CatalogAdapter catalogAdapter = new CatalogAdapter();
    
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
        try { masterTipos.setAll(catalogAdapter.listarTiposMovimiento()); } catch (Exception e) { showAlert("Error", e.getMessage()); }
    }

    @FXML public void loadMotivos() {
        try { masterMotivos.setAll(catalogAdapter.listarMotivosMovimiento()); } catch (Exception e) { showAlert("Error", e.getMessage()); }
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

        if (isEditingTipo) {
            if (selectedItem == null) {
                TipoMovimiento t = new TipoMovimiento();
                t.setNombre(nombre);
                String r = catalogAdapter.crearTipoMovimiento(t);
                if (!"OK".equals(r)) { showAlert("Error", r); return; }
            } else {
                TipoMovimiento t = (TipoMovimiento) selectedItem;
                t.setNombre(nombre);
                catalogAdapter.actualizarTipoMovimiento(t);
            }
            loadTipos();
        } else {
            if (selectedItem == null) {
                MotivoMovimiento m = new MotivoMovimiento();
                m.setNombre(nombre);
                String r = catalogAdapter.crearMotivoMovimiento(m);
                if (!"OK".equals(r)) { showAlert("Error", r); return; }
            } else {
                MotivoMovimiento m = (MotivoMovimiento) selectedItem;
                m.setNombre(nombre);
                catalogAdapter.actualizarMotivoMovimiento(m);
            }
            loadMotivos();
        }
        onCloseModal();
        showAlert("Éxito", "Registro guardado correctamente.");
    }

    private void confirmDelete(Object item, boolean isTipo) {
        String nombre = isTipo ? ((TipoMovimiento)item).getNombre() : ((MotivoMovimiento)item).getNombre();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Eliminación");
        dialog.setHeaderText("¿Está seguro de eliminar \"" + nombre + "\"?");
        dialog.setContentText("Esta acción no se puede deshacer. Escriba 'ELIMINAR' para confirmar:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && "ELIMINAR".equals(result.get().trim().toUpperCase())) {
            String r = isTipo
                ? catalogAdapter.eliminarTipoMovimiento(((TipoMovimiento)item).getId())
                : catalogAdapter.eliminarMotivoMovimiento(((MotivoMovimiento)item).getId());
            if ("OK".equals(r)) {
                loadTipos(); loadMotivos();
                showAlert("Éxito", "Registro eliminado.");
            } else {
                showAlert("Error", r);
            }
        }
    }

    @FXML protected void onCloseModal() { modalCatalogo.setVisible(false); }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}
