package com.utp.meditrackapp.features.catalogs.ui;

import com.utp.meditrackapp.core.dao.RolDAO;
import com.utp.meditrackapp.core.models.entity.Rol;
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

public class RolController {

    @FXML private TableView<Rol> tableRoles;
    @FXML private TableColumn<Rol, String> colId;
    @FXML private TableColumn<Rol, String> colNombre;
    @FXML private TableColumn<Rol, String> colEstado;
    @FXML private TableColumn<Rol, Void> colAcciones;

    @FXML private TextField txtSearch;
    @FXML private Label lblTotalRoles;
    @FXML private StackPane modalRol;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre;

    private final RolDAO rolDAO = new RolDAO();
    private final ObservableList<Rol> masterData = FXCollections.observableArrayList();
    private Rol selectedRol;

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
                    Rol rol = getTableRow().getItem();
                    Label label = new Label(rol.getIsActivo() == 1 ? "ACTIVO" : "INACTIVO");
                    label.getStyleClass().add("status-badge-base");
                    label.getStyleClass().add(rol.getIsActivo() == 1 ? "status-badge-active" : "status-badge-critico");
                    setGraphic(label);
                }
            }
        });

        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button();
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setTooltip(new Tooltip("Editar"));
                editBtn.setOnAction(event -> {
                    Rol rol = getTableRow().getItem();
                    if (rol != null) openEditModal(rol);
                });

                toggleBtn.getStyleClass().addAll("button", "flat", "sm");
                toggleBtn.setTooltip(new Tooltip("Activar/Desactivar"));
                toggleBtn.setOnAction(event -> {
                    Rol rol = getTableRow().getItem();
                    if (rol != null) confirmToggle(rol);
                });

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("button", "flat", "danger", "sm");
                deleteBtn.setTooltip(new Tooltip("Eliminar"));
                deleteBtn.setOnAction(event -> {
                    Rol rol = getTableRow().getItem();
                    if (rol != null) confirmDelete(rol);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Rol rol = getTableRow().getItem();
                    toggleBtn.setGraphic(new FontIcon(
                        rol.getIsActivo() == 1 ? "fas-user-minus" : "fas-user-check"));
                    HBox box = new HBox(8, editBtn, toggleBtn, deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Rol> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(rol -> {
                if (newValue == null || newValue.isBlank()) return true;
                String lower = newValue.toLowerCase();
                return rol.getNombre().toLowerCase().contains(lower) || 
                       rol.getId().toLowerCase().contains(lower);
            });
        });
        tableRoles.setItems(filteredData);
    }

    @FXML
    public void loadData() {
        try {
            List<Rol> list = rolDAO.listarTodas();
            masterData.setAll(list);
            if (lblTotalRoles != null) {
                lblTotalRoles.setText(String.valueOf(list.size()));
            }
        } catch (SQLException e) {
            showAlert("Error", "No se pudieron cargar los roles: " + e.getMessage());
        }
    }

    @FXML
    protected void onOpenRegisterModal() {
        selectedRol = null;
        modalTitle.setText("Registrar Rol");
        txtNombre.clear();
        modalRol.setVisible(true);
    }

    private void openEditModal(Rol rol) {
        selectedRol = rol;
        modalTitle.setText("Editar Rol");
        txtNombre.setText(rol.getNombre());
        modalRol.setVisible(true);
    }

    @FXML
    protected void onSave() {
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.isBlank()) {
            showAlert("Validación", "El nombre es obligatorio.");
            return;
        }

        try {
            if (selectedRol == null) {
                Rol nuevo = new Rol(null, nombre);
                rolDAO.crear(nuevo);
                showAlert("Éxito", "Rol creado correctamente.");
            } else {
                selectedRol.setNombre(nombre);
                rolDAO.actualizar(selectedRol);
                showAlert("Éxito", "Rol actualizado correctamente.");
            }
            loadData();
            onCloseModal();
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void confirmToggle(Rol rol) {
        String accion = rol.getIsActivo() == 1 ? "desactivar" : "activar";

        // Check if users are assigned to this role before deactivating
        if (rol.getIsActivo() == 1) {
            try {
                int userCount = rolDAO.countUsersByRole(rol.getId());
                if (userCount > 0) {
                    showAlert("No se puede desactivar",
                        "Hay " + userCount + " usuario(s) asignado(s) al rol \"" + rol.getNombre() + "\".\n\n" +
                        "Primero reassigne o elimine los usuarios antes de desactivar este rol.");
                    return;
                }
            } catch (SQLException e) {
                showAlert("Error", "No se pudo verificar usuarios: " + e.getMessage());
                return;
            }
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cambio de Estado");
        alert.setHeaderText("¿Está seguro de " + accion + " el rol?");
        alert.setContentText("El rol \"" + rol.getNombre() + "\" será " + (rol.getIsActivo() == 1 ? "desactivado" : "activado") + ".");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                rolDAO.toggleEstado(rol.getId());
                loadData();
                showAlert("Éxito", "Rol " + (rol.getIsActivo() == 1 ? "desactivado" : "activado") + ".");
            } catch (SQLException e) {
                showAlert("Error", "No se pudo cambiar el estado: " + e.getMessage());
            }
        }
    }

    private void confirmDelete(Rol rol) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar el rol?");
        alert.setContentText("Esta acción no se puede deshacer y puede fallar si el rol está en uso por usuarios.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                rolDAO.eliminar(rol.getId());
                loadData();
                showAlert("Éxito", "Rol eliminado.");
            } catch (SQLException e) {
                showAlert("Error", "No se pudo eliminar: " + e.getMessage());
            }
        }
    }

    @FXML protected void onCloseModal() { modalRol.setVisible(false); }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
