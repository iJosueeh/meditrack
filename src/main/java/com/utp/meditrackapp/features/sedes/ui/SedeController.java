package com.utp.meditrackapp.features.sedes.ui;

import com.utp.meditrackapp.core.models.entity.Sede;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.features.sedes.dao.SedeDAO;
import com.utp.meditrackapp.features.sedes.models.SedeDetalleDTO;
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

public class SedeController {

    @FXML private TableView<SedeDetalleDTO> tableSedes;
    @FXML private TableColumn<SedeDetalleDTO, String> colId;
    @FXML private TableColumn<SedeDetalleDTO, String> colNombre;
    @FXML private TableColumn<SedeDetalleDTO, String> colAdmin;
    @FXML private TableColumn<SedeDetalleDTO, Integer> colEmp;
    @FXML private TableColumn<SedeDetalleDTO, String> colStockStatus;
    @FXML private TableColumn<SedeDetalleDTO, Integer> colEstado;
    @FXML private TableColumn<SedeDetalleDTO, Void> colAcciones;

    @FXML private Label lblTotalSedes;
    @FXML private Label lblTotalEmpleados;
    @FXML private Label lblSedesCriticas;
    @FXML private TextField txtSearchSede;
    @FXML private Pagination pagination;

    @FXML private StackPane modalSede;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre, txtTelefono;
    @FXML private TextArea txtDireccion;
    @FXML private CheckBox chkActiva;
    @FXML private ComboBox<com.utp.meditrackapp.core.models.entity.Usuario> cmbManager;
    @FXML private ComboBox<String> cmbTipoSede;
    @FXML private TableView<com.utp.meditrackapp.core.models.entity.Usuario> tableStaff;
    @FXML private TableColumn<com.utp.meditrackapp.core.models.entity.Usuario, String> colStaffName, colStaffRol;

    private final SedeDAO sedeDAO = new SedeDAO();
    private final com.utp.meditrackapp.features.auth.Dao.UsuarioDao usuarioDao = new com.utp.meditrackapp.features.auth.Dao.UsuarioDao();
    private final ObservableList<SedeDetalleDTO> masterData = FXCollections.observableArrayList();
    private static final int ROWS_PER_PAGE = 8;

    @FXML
    public void initialize() {
        setupTable();
        setupModalControls();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAdmin.setCellValueFactory(new PropertyValueFactory<>("administrador"));
        colEmp.setCellValueFactory(new PropertyValueFactory<>("totalEmpleados"));
        
        // Staff Table inside modal
        colStaffName.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colStaffRol.setCellValueFactory(new PropertyValueFactory<>("rolNombre"));

        // Custom rendering for Stock Status
        colStockStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    SedeDetalleDTO dto = getTableRow().getItem();
                    String status = dto.getEstadoInventario();
                    Label label = new Label(status.toUpperCase());
                    label.getStyleClass().add("status-badge-base");
                    
                    if ("Óptimo".equals(status)) label.getStyleClass().add("status-badge-active");
                    else if ("Bajo Stock".equals(status)) label.getStyleClass().add("status-badge-bajo");
                    else label.getStyleClass().add("status-badge-critico");
                    
                    setGraphic(label);
                }
            }
        });

        // Custom rendering for Operational Estado
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    SedeDetalleDTO s = getTableRow().getItem();
                    Label label = new Label(s.getIsActiva() == 1 ? "OPERATIVA" : "INACTIVA");
                    label.getStyleClass().add("status-badge-base");
                    label.getStyleClass().add(s.getIsActiva() == 1 ? "status-badge-active" : "status-badge-critico");
                    setGraphic(label);
                }
            }
        });

        // Column for Actions
        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button();
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setTooltip(new Tooltip("Editar Sede"));
                editBtn.setOnAction(event -> {
                    SedeDetalleDTO sede = getTableRow().getItem();
                    if (sede != null) openEditModal(sede);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox box = new HBox(editBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void setupModalControls() {
        cmbTipoSede.setItems(FXCollections.observableArrayList("Posta Médica", "Hospital", "Almacén Central", "Centro de Salud"));
        
        cmbManager.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(com.utp.meditrackapp.core.models.entity.Usuario u) { return u != null ? u.getNombreCompleto() : ""; }
            @Override public com.utp.meditrackapp.core.models.entity.Usuario fromString(String s) { return null; }
        });

        // Cargar y filtrar solo usuarios con rol de Administrador o Jefe (Mapeo Semántico)
        List<com.utp.meditrackapp.core.models.entity.Usuario> admins = usuarioDao.listarTodos().stream()
            .filter(u -> u.getRolNombre() != null && 
                        (u.getRolNombre().toUpperCase().contains("ADMIN") || 
                         u.getRolNombre().toUpperCase().contains("JEFE")))
            .toList();
            
        cmbManager.setItems(FXCollections.observableArrayList(admins));
    }

    private void loadData() {
        try {
            List<SedeDetalleDTO> list = sedeDAO.getAllWithDetails();
            masterData.setAll(list);
            
            // Update Summary metrics
            lblTotalSedes.setText(String.valueOf(masterData.size()));
            lblTotalEmpleados.setText(String.valueOf(sedeDAO.getTotalEmployeesGlobal()));
            long criticas = masterData.stream().filter(s -> "Crítico".equals(s.getEstadoInventario())).count();
            lblSedesCriticas.setText(String.valueOf(criticas));

            setupFilteringAndPagination();
            
        } catch (SQLException e) {
            showAlert("Error de Datos", e.getMessage());
        }
    }

    private void setupFilteringAndPagination() {
        FilteredList<SedeDetalleDTO> filteredData = new FilteredList<>(masterData, p -> true);
        
        txtSearchSede.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(sede -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String low = newValue.toLowerCase();
                return (sede.getNombre() != null && sede.getNombre().toLowerCase().contains(low)) || 
                       (sede.getAdministrador() != null && sede.getAdministrador().toLowerCase().contains(low)) ||
                       (sede.getId() != null && sede.getId().toLowerCase().contains(low));
            });
            updatePagination(filteredData);
        });

        updatePagination(filteredData);
    }

    private void updatePagination(FilteredList<SedeDetalleDTO> data) {
        int count = (int) Math.ceil((double) data.size() / ROWS_PER_PAGE);
        pagination.setPageCount(Math.max(1, count));
        pagination.setPageFactory(pageIndex -> {
            int from = pageIndex * ROWS_PER_PAGE;
            int to = Math.min(from + ROWS_PER_PAGE, data.size());
            tableSedes.setItems(FXCollections.observableArrayList(data.subList(from, to)));
            return new Label(); // Dummy to satisfy factory
        });
    }

    @FXML
    protected void onOpenRegisterModal() {
        selectedSede = null;
        modalTitle.setText("Registrar Nueva Sede");
        txtNombre.clear();
        txtDireccion.clear();
        txtTelefono.clear();
        cmbManager.getSelectionModel().clearSelection();
        cmbTipoSede.getSelectionModel().selectFirst();
        tableStaff.setItems(FXCollections.emptyObservableList());
        chkActiva.setSelected(true);
        modalSede.setVisible(true);
    }

    private SedeDetalleDTO selectedSede;
    private void openEditModal(SedeDetalleDTO sede) {
        selectedSede = sede;
        modalTitle.setText("Modificar Sede");
        txtNombre.setText(sede.getNombre());
        txtDireccion.setText(sede.getDireccion());
        txtTelefono.setText(sede.getTelefono());
        
        // Seleccionar manager actual si existe
        if (sede.getAdministradorId() != null) {
            cmbManager.getItems().stream()
                .filter(u -> u.getId().equals(sede.getAdministradorId()))
                .findFirst()
                .ifPresent(u -> cmbManager.setValue(u));
        }

        // Cargar Personal asignado
        try {
            List<com.utp.meditrackapp.core.models.entity.Usuario> staff = sedeDAO.getStaffBySede(sede.getId());
            tableStaff.setItems(FXCollections.observableArrayList(staff));
        } catch (SQLException e) { e.printStackTrace(); }

        chkActiva.setSelected(sede.getIsActiva() == 1);
        modalSede.setVisible(true);
    }

    @FXML
    protected void onSaveSede() {
        String nombre = txtNombre.getText();
        String dir = txtDireccion.getText();
        String tel = txtTelefono.getText(); // Capturar teléfono
        int activa = chkActiva.isSelected() ? 1 : 0;

        if (nombre == null || nombre.trim().isEmpty()) {
            showAlert("Validación", "El nombre de la sede es obligatorio.");
            return;
        }

        try {
            String sedeId;

            if (selectedSede == null) {
                SedeDetalleDTO dtoToSave = new SedeDetalleDTO(null, nombre, dir, activa);
                dtoToSave.setTelefono(tel);
                sedeDAO.save(dtoToSave);
                sedeId = dtoToSave.getId();
            } else {
                sedeId = selectedSede.getId();
                selectedSede.setNombre(nombre);
                selectedSede.setDireccion(dir);
                selectedSede.setTelefono(tel); // Actualizar en el DTO
                selectedSede.setIsActiva(activa);
                sedeDAO.update(selectedSede);
            }

            // Lógica de Asignación de Jefe (via Usuarios table)
            com.utp.meditrackapp.core.models.entity.Usuario selectedManager = cmbManager.getValue();
            if (selectedManager != null) {
                sedeDAO.assignUserToSede(selectedManager.getId(), sedeId, selectedManager.getRolId());
            }

            loadData();
            onCloseModal();
            showAlert("Éxito", "Sede y responsable actualizados correctamente.");
        } catch (SQLException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML protected void onCloseModal() { modalSede.setVisible(false); }

    private void showAlert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(c); a.showAndWait();
    }
}
