package com.utp.meditrackapp.features.catalogs.ui;

import com.utp.meditrackapp.infrastructure.adapters.CatalogAdapter;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Categoria;
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

public class CategoriaController {

    @FXML private TableView<Categoria> tableCategorias;
    @FXML private TableColumn<Categoria, String> colId;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colEstado;
    @FXML private TableColumn<Categoria, Void> colAcciones;

    @FXML private TextField txtSearch;
    @FXML private Label lblTotalCategorias;
    @FXML private StackPane modalCategoria;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre;

    private final CatalogAdapter catalogAdapter = new CatalogAdapter();
    private final SessionManager sessionManager = SessionManager.getInstance();
    private final ObservableList<Categoria> masterData = FXCollections.observableArrayList();
    private Categoria selectedCategoria;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        colEstado.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Categoria cat = getTableRow().getItem();
                    Label label = new Label(cat.getIsActivo() == 1 ? "ACTIVA" : "INACTIVA");
                    label.getStyleClass().add("status-badge-base");
                    label.getStyleClass().add(cat.getIsActivo() == 1 ? "status-badge-active" : "status-badge-critico");
                    setGraphic(label);
                }
            }
        });

        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button();
            private final boolean canWrite = sessionManager.tienePermiso("M2_SEDES");
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setTooltip(new Tooltip("Editar"));
                editBtn.setOnAction(event -> {
                    Categoria cat = getTableRow().getItem();
                    if (cat != null) openEditModal(cat);
                });

                toggleBtn.getStyleClass().addAll("button", "flat", "sm");
                toggleBtn.setTooltip(new Tooltip("Activar/Desactivar"));
                toggleBtn.setOnAction(event -> {
                    Categoria cat = getTableRow().getItem();
                    if (cat != null) confirmToggle(cat);
                });

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("button", "flat", "danger", "sm");
                deleteBtn.setTooltip(new Tooltip("Eliminar"));
                deleteBtn.setOnAction(event -> {
                    Categoria cat = getTableRow().getItem();
                    if (cat != null) confirmDelete(cat);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    if (!canWrite) {
                        setGraphic(null);
                    } else {
                        Categoria cat = getTableRow().getItem();
                        toggleBtn.setGraphic(new FontIcon(
                            cat.getIsActivo() == 1 ? "fas-toggle-on" : "fas-toggle-off"));
                        HBox box = new HBox(8, editBtn, toggleBtn, deleteBtn);
                        box.setStyle("-fx-alignment: center;");
                        setGraphic(box);
                    }
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Categoria> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(cat -> {
                if (newValue == null || newValue.isBlank()) return true;
                String lower = newValue.toLowerCase();
                return cat.getNombre().toLowerCase().contains(lower) || 
                       cat.getId().toLowerCase().contains(lower);
            });
        });
        tableCategorias.setItems(filteredData);
    }

    @FXML
    public void loadData() {
        try {
            List<Categoria> list = catalogAdapter.listarCategorias();
            masterData.setAll(list);
            if (lblTotalCategorias != null) {
                lblTotalCategorias.setText(String.valueOf(list.size()));
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudieron cargar las categorías: " + e.getMessage());
        }
    }

    @FXML
    protected void onOpenRegisterModal() {
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert("Sin permisos", "No tiene permisos para crear categorías.");
            return;
        }
        selectedCategoria = null;
        modalTitle.setText("Registrar Categoría");
        txtNombre.clear();
        modalCategoria.setVisible(true);
    }

    private void openEditModal(Categoria cat) {
        selectedCategoria = cat;
        modalTitle.setText("Editar Categoría");
        txtNombre.setText(cat.getNombre());
        modalCategoria.setVisible(true);
    }

    @FXML
    protected void onSave() {
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert("Sin permisos", "No tiene permisos para modificar categorías.");
            return;
        }
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.isBlank()) {
            showAlert("Validación", "El nombre es obligatorio.");
            return;
        }

        if (selectedCategoria == null) {
            Categoria nueva = new Categoria(null, nombre);
            String result = catalogAdapter.crearCategoria(nueva);
            if (!"OK".equals(result)) {
                showAlert("Error", result);
                return;
            }
            showAlert("Éxito", "Categoría creada correctamente.");
        } else {
            selectedCategoria.setNombre(nombre);
            catalogAdapter.actualizarCategoria(selectedCategoria);
            showAlert("Éxito", "Categoría actualizada correctamente.");
        }
        loadData();
        onCloseModal();
    }

    private void confirmToggle(Categoria cat) {
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert("Sin permisos", "No tiene permisos para modificar categorías.");
            return;
        }
        String accion = cat.getIsActivo() == 1 ? "desactivar" : "activar";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cambio de Estado");
        alert.setHeaderText("¿Está seguro de " + accion + " la categoría?");
        alert.setContentText("La categoría \"" + cat.getNombre() + "\" será " + (cat.getIsActivo() == 1 ? "desactivada" : "activada") + ".");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String r = catalogAdapter.toggleEstadoCategoria(cat.getId());
            if ("OK".equals(r)) {
                loadData();
                showAlert("Éxito", "Categoría " + (cat.getIsActivo() == 1 ? "desactivada" : "activada") + ".");
            } else {
                showAlert("Error", r);
            }
        }
    }

    private void confirmDelete(Categoria cat) {
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert("Sin permisos", "No tiene permisos para eliminar categorías.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Eliminación");
        dialog.setHeaderText("¿Está seguro de eliminar la categoría \"" + cat.getNombre() + "\"?");
        dialog.setContentText("Esta acción no se puede deshacer. Escriba 'ELIMINAR' para confirmar:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && "ELIMINAR".equals(result.get().trim().toUpperCase())) {
            String r = catalogAdapter.eliminarCategoria(cat.getId());
            if ("OK".equals(r)) {
                loadData();
                showAlert("Éxito", "Categoría eliminada.");
            } else {
                showAlert("Error", r);
            }
        }
    }

    @FXML protected void onCloseModal() { modalCategoria.setVisible(false); }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
