package com.utp.meditrackapp.features.sedes.ui;

import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.infrastructure.adapters.SedeAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;

public class SedeController {

    @FXML private TableView<Sede> tableSedes;
    @FXML private TableColumn<Sede, String> colId;
    @FXML private TableColumn<Sede, String> colNombre;
    @FXML private TableColumn<Sede, String> colAdmin;
    @FXML private TableColumn<Sede, Integer> colEmp;
    @FXML private TableColumn<Sede, String> colStockStatus;
    @FXML private TableColumn<Sede, Integer> colEstado;
    @FXML private TableColumn<Sede, String> colBloqueo;
    @FXML private TableColumn<Sede, String> colUbigeo;
    @FXML private TableColumn<Sede, Void> colAcciones;

    @FXML private Label lblTotalSedes;
    @FXML private Label lblTotalEmpleados;
    @FXML private Label lblSedesCriticas;
    @FXML private TextField txtSearchSede;
    @FXML private Pagination pagination;

    @FXML private StackPane modalSede;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre, txtTelefono, txtUbigeo;
    @FXML private TextArea txtDireccion;
    @FXML private CheckBox chkActiva;
    @FXML private ComboBox<Usuario> cmbManager;
    @FXML private ComboBox<String> cmbTipoSede;
    @FXML private TableView<Usuario> tableStaff;
    @FXML private TableColumn<Usuario, String> colStaffName, colStaffRol;

    private final SedeAdapter sedeAdapter = new SedeAdapter();
    private final ObservableList<Sede> masterData = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 8;
    private Sede selectedSede;

    @FXML
    public void initialize() {
        setupTable();
        setupModalControls();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAdmin.setCellValueFactory(new PropertyValueFactory<>("administradorNombre"));
        colEmp.setCellValueFactory(new PropertyValueFactory<>("totalEmpleados"));
        colUbigeo.setCellValueFactory(new PropertyValueFactory<>("ubigeo"));
        
        colStaffName.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colStaffRol.setCellValueFactory(new PropertyValueFactory<>("rolNombre"));

        colStockStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Sede sede = getTableRow().getItem();
                    String tipo = sede.getTipoSede() != null ? sede.getTipoSede() : "N/A";
                    Label label = new Label(tipo.toUpperCase());
                    label.getStyleClass().add("status-badge-base");
                    label.getStyleClass().add("status-badge-active");
                    setGraphic(label);
                }
            }
        });

        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Sede s = getTableRow().getItem();
                    Label label = new Label(s.getIsActiva() == 1 ? "OPERATIVA" : "INACTIVA");
                    label.getStyleClass().add("status-badge-base");
                    label.getStyleClass().add(s.getIsActiva() == 1 ? "status-badge-active" : "status-badge-critico");
                    setGraphic(label);
                }
            }
        });

        colBloqueo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Sede s = getTableRow().getItem();
                    if (s.isBloqueada()) {
                        Label label = new Label("BLOQUEADA");
                        label.getStyleClass().add("status-badge-base");
                        label.getStyleClass().add("status-badge-critico");
                        label.setTooltip(new Tooltip(s.getMotivoBloqueo() != null ? s.getMotivoBloqueo() : "Sin motivo"));
                        setGraphic(label);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button toggleBtn = new Button();
            private final Button blockBtn = new Button();
            private final Button deleteBtn = new Button();
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setTooltip(new Tooltip("Editar Sede"));
                editBtn.setMinWidth(26);
                editBtn.setMaxWidth(26);
                editBtn.setOnAction(event -> {
                    Sede sede = getTableRow().getItem();
                    if (sede != null) openEditModal(sede);
                });

                toggleBtn.getStyleClass().addAll("button", "flat", "sm");
                toggleBtn.setTooltip(new Tooltip("Activar/Desactivar"));
                toggleBtn.setMinWidth(26);
                toggleBtn.setMaxWidth(26);
                toggleBtn.setOnAction(event -> {
                    Sede sede = getTableRow().getItem();
                    if (sede != null) confirmToggle(sede);
                });

                blockBtn.getStyleClass().addAll("button", "flat", "sm");
                blockBtn.setTooltip(new Tooltip("Bloquear/Desbloquear"));
                blockBtn.setMinWidth(26);
                blockBtn.setMaxWidth(26);
                blockBtn.setOnAction(event -> {
                    Sede sede = getTableRow().getItem();
                    if (sede != null) confirmBlock(sede);
                });

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("button", "flat", "sm", "danger");
                deleteBtn.setTooltip(new Tooltip("Eliminar"));
                deleteBtn.setMinWidth(26);
                deleteBtn.setMaxWidth(26);
                deleteBtn.setOnAction(event -> {
                    Sede sede = getTableRow().getItem();
                    if (sede != null) handleDeleteSede(sede);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Sede sede = getTableRow().getItem();
                    toggleBtn.setGraphic(new FontIcon(
                        sede.getIsActiva() == 1 ? "fas-toggle-on" : "fas-toggle-off"));
                    
                    if (sede.isBloqueada()) {
                        blockBtn.setGraphic(new FontIcon("fas-lock"));
                        blockBtn.getStyleClass().removeAll("accent");
                        blockBtn.getStyleClass().add("danger");
                    } else {
                        blockBtn.setGraphic(new FontIcon("fas-unlock"));
                        blockBtn.getStyleClass().removeAll("danger");
                        blockBtn.getStyleClass().add("accent");
                    }
                    
                    HBox box = new HBox(3, editBtn, toggleBtn, blockBtn, deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void setupModalControls() {
        cmbTipoSede.setItems(FXCollections.observableArrayList("Posta Médica", "Hospital", "Almacén Central", "Centro de Salud"));
        
        cmbManager.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Usuario u) { return u != null ? u.getNombreCompleto() : ""; }
            @Override public Usuario fromString(String s) { return null; }
        });

        List<Usuario> admins = sedeAdapter.obtenerAdministradoresDisponibles();
            
        cmbManager.setItems(FXCollections.observableArrayList(admins));
    }

    private void loadData() {
        try {
            List<Sede> list = sedeAdapter.listarSedes();
            masterData.setAll(list);
            
            lblTotalSedes.setText(String.valueOf(masterData.size()));
            lblTotalEmpleados.setText(String.valueOf(sedeAdapter.contarEmpleadosGlobales()));
            long criticas = masterData.stream().filter(s -> s.getIsActiva() == 0).count();
            lblSedesCriticas.setText(String.valueOf(criticas));

            setupFilteringAndPagination();
            
        } catch (Exception e) {
            showAlert("Error de Datos", e.getMessage());
        }
    }

    private void setupFilteringAndPagination() {
        FilteredList<Sede> filteredData = new FilteredList<>(masterData, p -> true);
        
        txtSearchSede.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(sede -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String low = newValue.toLowerCase();
                return (sede.getNombre() != null && sede.getNombre().toLowerCase().contains(low)) || 
                       (sede.getTipoSede() != null && sede.getTipoSede().toLowerCase().contains(low)) ||
                       (sede.getId() != null && sede.getId().toLowerCase().contains(low));
            });
            updatePagination(filteredData);
        });

        updatePagination(filteredData);
    }

    private void updatePagination(FilteredList<Sede> data) {
        int count = (int) Math.ceil((double) data.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(1, count));
        pagination.setPageFactory(pageIndex -> {
            int from = pageIndex * ROWS_PER_PAGE;
            int to = Math.min(from + ROWS_PER_PAGE, data.size());
            tableSedes.setItems(FXCollections.observableArrayList(data.subList(from, to)));
            return new Label();
        });
    }

    @FXML
    protected void onOpenRegisterModal() {
        selectedSede = null;
        modalTitle.setText("Registrar Nueva Sede");
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        txtUbigeo.clear();
        cmbManager.getSelectionModel().clearSelection();
        cmbTipoSede.getSelectionModel().selectFirst();
        tableStaff.setItems(FXCollections.emptyObservableList());
        chkActiva.setSelected(true);
        modalSede.setVisible(true);
    }

    private void openEditModal(Sede sede) {
        selectedSede = sede;
        modalTitle.setText("Modificar Sede");
        txtNombre.setText(sede.getNombre());
        txtDireccion.setText(sede.getDireccion());
        txtTelefono.setText(sede.getTelefono());
        txtUbigeo.setText(sede.getUbigeo());
        
        if (sede.getAdministradorId() != null) {
            cmbManager.getItems().stream()
                .filter(u -> u.getId().equals(sede.getAdministradorId()))
                .findFirst()
                .ifPresent(u -> cmbManager.setValue(u));
        }

        try {
            List<Usuario> staff = sedeAdapter.obtenerStaffPorSede(sede.getId());
            tableStaff.setItems(FXCollections.observableArrayList(staff));
        } catch (Exception e) { e.printStackTrace(); }

        chkActiva.setSelected(sede.getIsActiva() == 1);
        modalSede.setVisible(true);
    }

    @FXML
    protected void onSaveSede() {
        String nombre = txtNombre.getText();
        String dir = txtDireccion.getText();
        String tel = txtTelefono.getText();
        String ubigeo = txtUbigeo.getText();
        int activa = chkActiva.isSelected() ? 1 : 0;
        Usuario selectedManager = cmbManager.getValue();

        if (nombre == null || nombre.trim().isEmpty()) {
            showAlert("Validación", "El nombre de la sede es obligatorio.");
            return;
        }
        if (dir == null || dir.trim().isEmpty()) {
            showAlert("Validación", "La dirección de la sede es obligatoria.");
            return;
        }
        if (tel == null || tel.trim().length() != 9) {
            showAlert("Validación", "El teléfono debe tener exactamente 9 dígitos.");
            return;
        }
        if (selectedManager == null) {
            showAlert("Validación", "Es obligatorio asignar un Jefe de Sede responsable.");
            return;
        }

        String tipoSede = cmbTipoSede.getValue();

        try {
            String sedeId;
            if (selectedSede == null) {
                Sede sedeToSave = new Sede(null, nombre, dir, activa);
                sedeToSave.setTelefono(tel);
                sedeToSave.setUbigeo(ubigeo);
                sedeToSave.setTipoSede(tipoSede);
                sedeToSave.setCapacidadAlmacen(0);
                sedeAdapter.guardarSede(sedeToSave);
                sedeId = sedeToSave.getId();
            } else {
                sedeId = selectedSede.getId();
                selectedSede.setNombre(nombre);
                selectedSede.setDireccion(dir);
                selectedSede.setTelefono(tel);
                selectedSede.setUbigeo(ubigeo);
                selectedSede.setIsActiva(activa);
                selectedSede.setTipoSede(tipoSede);
                selectedSede.setCapacidadAlmacen(0);
                sedeAdapter.guardarSede(selectedSede);
            }

            sedeAdapter.asignarUsuarioASede(selectedManager.getId(), sedeId, selectedManager.getRolId());

            loadData();
            onCloseModal();
            showAlert("Éxito", "Sede y jefe asignado guardados correctamente.");
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void confirmToggle(Sede sede) {
        String accion = sede.getIsActiva() == 1 ? "desactivar" : "activar";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cambio de Estado");
        alert.setHeaderText("¿Está seguro de " + accion + " la sede?");
        alert.setContentText("La sede \"" + sede.getNombre() + "\" será " + (sede.getIsActiva() == 1 ? "desactivada" : "activada") + ".");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                sedeAdapter.toggleEstado(sede.getId());
                loadData();
                showAlert("Éxito", "Sede " + (sede.getIsActiva() == 1 ? "desactivada" : "activada") + ".");
            } catch (Exception e) {
                showAlert("Error", "No se pudo cambiar el estado: " + e.getMessage());
            }
        }
    }

    private void confirmBlock(Sede sede) {
        if (sede.isBloqueada()) {
            // Desbloquear
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Desbloqueo");
            alert.setHeaderText("¿Está seguro de desbloquear la sede?");
            alert.setContentText("La sede \"" + sede.getNombre() + "\" será desbloqueada y los usuarios podrán operar nuevamente.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    sedeAdapter.desbloquear(sede.getId());
                    loadData();
                    showAlert("Éxito", "Sede desbloqueada correctamente.");
                } catch (Exception e) {
                    showAlert("Error", "No se pudo desbloquear la sede: " + e.getMessage());
                }
            }
        } else {
            // Bloquear - solicitar motivo
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Bloquear Sede");
            dialog.setHeaderText("Ingrese el motivo del bloqueo");
            dialog.setContentText("Motivo:");
            
            Optional<String> motivoResult = dialog.showAndWait();
            if (motivoResult.isPresent() && !motivoResult.get().trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmar Bloqueo");
                alert.setHeaderText("¿Está seguro de bloquear la sede?");
                alert.setContentText("La sede \"" + sede.getNombre() + "\" será bloqueada. Los usuarios no podrán realizar operaciones.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    try {
                        sedeAdapter.bloquear(sede.getId(), motivoResult.get().trim());
                        loadData();
                        showAlert("Éxito", "Sede bloqueada correctamente.");
                    } catch (Exception e) {
                        showAlert("Error", "No se pudo bloquear la sede: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void handleDeleteSede(Sede sede) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Está seguro de eliminar la sede \"" + sede.getNombre() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String deleteResult = sedeAdapter.eliminarSede(sede.getId());
        if ("NO_HISTORY".equals(deleteResult)) {
            showAlert("Tiene historial",
                "La sede \"" + sede.getNombre() + "\" tiene usuarios, lotes, movimientos o atenciones registrados.\nNo se puede eliminar sin borrar primero el historial asociado.");
        } else if ("OK".equals(deleteResult)) {
            showAlert("Eliminado", "Sede eliminada correctamente.");
            loadData();
        } else {
            showAlert("Error", deleteResult);
        }
    }

    @FXML protected void onCloseModal() { modalSede.setVisible(false); }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}
