package com.utp.meditrackapp.features.patients.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.infrastructure.adapters.PacienteAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;

public class PacienteController {

    @FXML private TextField searchField;
    @FXML private TableView<Paciente> patientsTable;
    @FXML private Button btnNuevoPaciente;
    @FXML private TableColumn<Paciente, String> colTipoDoc;
    @FXML private TableColumn<Paciente, String> colNumDoc;
    @FXML private TableColumn<Paciente, String> colNombres;
    @FXML private TableColumn<Paciente, String> colApellidos;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, Integer> colEstado;
    @FXML private TableColumn<Paciente, Void> colActions;

    // Summary Labels
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTodayAttentions;
    @FXML private Label lblNewPatientsMonth;

    // Form Overlay Fields
    @FXML private StackPane formOverlay;
    @FXML private Label formTitle;
    @FXML private ComboBox<String> typeDocCombo;
    @FXML private TextField numDocField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private CheckBox chkActivo;

    private final PacienteAdapter pacienteAdapter = new PacienteAdapter();
    private Paciente currentPaciente; // Para edición

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadPatients();
        applyRoleRestrictions();
        handleInitialSearch();
    }

    private void handleInitialSearch() {
        String initialQuery = com.utp.meditrackapp.core.config.NavigationService.getPatientInitialSearch();
        if (initialQuery != null && !initialQuery.isEmpty()) {
            searchField.setText(initialQuery);
            onSearch();
            
            if (!patientsTable.getItems().isEmpty()) {
                patientsTable.getSelectionModel().select(0);
                patientsTable.scrollTo(0);
                patientsTable.requestFocus();
            }
        }
    }

    private void applyRoleRestrictions() {
        SessionManager session = SessionManager.getInstance();
        System.out.println("[AUTH] Permisos: M7_PACIENTES=" + session.tienePermiso("M7_PACIENTES")
            + " | USUARIOS=" + session.tienePermiso("USUARIOS"));

        btnNuevoPaciente.setVisible(session.tienePermiso("M7_PACIENTES"));
        btnNuevoPaciente.setManaged(session.tienePermiso("M7_PACIENTES"));
    }

    private void setupTable() {
        colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        colNumDoc.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("isActivo"));

        setupEstadoColumn();
        setupTooltipColumn(colNombres);
        setupTooltipColumn(colApellidos);
        addButtonToTable();
    }

    private void setupEstadoColumn() {
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");
                    if (item == 1) {
                        badge.setText("ACTIVO");
                        badge.getStyleClass().add("success");
                    } else {
                        badge.setText("INACTIVO");
                        badge.getStyleClass().add("danger");
                    }
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private <T> void setupTooltipColumn(TableColumn<Paciente, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString());
                    setTooltip(new Tooltip(item.toString()));
                }
            }
        });
    }

    private void setupForm() {
        typeDocCombo.setItems(FXCollections.observableArrayList("DNI", "CE"));
        typeDocCombo.getSelectionModel().selectFirst();
    }

    private void loadPatients() {
        try {
            SessionManager session = SessionManager.getInstance();
            List<Paciente> pacientes;
            if (session.tienePermiso("M2_SEDES")) {
                pacientes = pacienteAdapter.listarPacientes();
            } else {
                var user = session.getCurrentUser();
                if (user != null && user.getSedeId() != null) {
                    pacientes = pacienteAdapter.listarPacientesPorSede(user.getSedeId());
                } else {
                    pacientes = pacienteAdapter.listarPacientes();
                }
            }
            patientsTable.setItems(FXCollections.observableArrayList(pacientes));
            updateSummary();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los pacientes: " + e.getMessage());
        }
    }

    private void updateSummary() {
        javafx.application.Platform.runLater(() -> {
            try {
                lblTotalPatients.setText(String.valueOf(pacienteAdapter.getContadorTotal()));
                lblTodayAttentions.setText(String.valueOf(pacienteAdapter.getAtendidosHoy()));
                lblNewPatientsMonth.setText(String.valueOf(pacienteAdapter.getNuevosDelMes()));
            } catch (Exception e) {
                System.err.println("[UI ERROR] Fallo al actualizar resumen: " + e.getMessage());
            }
        });
    }

    @FXML
    protected void onSearch() {
        String query = searchField.getText();
        SessionManager session = SessionManager.getInstance();
        List<Paciente> basePacientes;
        if (session.tienePermiso("M2_SEDES")) {
            basePacientes = pacienteAdapter.listarPacientes();
        } else {
            var user = session.getCurrentUser();
            if (user != null && user.getSedeId() != null) {
                basePacientes = pacienteAdapter.listarPacientesPorSede(user.getSedeId());
            } else {
                basePacientes = pacienteAdapter.listarPacientes();
            }
        }

        if (query == null || query.trim().isEmpty()) {
            patientsTable.setItems(FXCollections.observableArrayList(basePacientes));
        } else {
            String[] terms = query.trim().toLowerCase().split("\\s+");
            List<Paciente> filtered = basePacientes.stream()
                .filter(p -> {
                    String fullData = (p.getNumeroDocumento() + " " + p.getNombres() + " " + p.getApellidos()).toLowerCase();
                    for (String term : terms) {
                        if (!fullData.contains(term)) return false;
                    }
                    return true;
                })
                .toList();
            patientsTable.setItems(FXCollections.observableArrayList(filtered));
        }
        updateSummary();
    }

    @FXML
    protected void onNewPatient() {
        currentPaciente = null;
        formTitle.setText("Registrar Nuevo Paciente");
        clearForm();
        formOverlay.setVisible(true);
    }

    @FXML
    protected void onSavePatient() {
        if (currentPaciente == null) {
            currentPaciente = new Paciente();
        }

        currentPaciente.setTipoDocumento(typeDocCombo.getValue());
        currentPaciente.setNumeroDocumento(numDocField.getText());
        currentPaciente.setNombres(firstNameField.getText());
        currentPaciente.setApellidos(lastNameField.getText());
        currentPaciente.setTelefono(phoneField.getText());
        currentPaciente.setIsActivo(chkActivo.isSelected() ? 1 : 0);

        String result = pacienteAdapter.guardarPaciente(currentPaciente);

        if (result.equals("OK")) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Paciente guardado correctamente.");
            formOverlay.setVisible(false);
            loadPatients();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", result);
        }
    }

    @FXML
    protected void onCancelForm() {
        formOverlay.setVisible(false);
        clearForm();
    }

    private void clearForm() {
        numDocField.clear();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        typeDocCombo.getSelectionModel().selectFirst();
        chkActivo.setSelected(true);
    }

    private void addButtonToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnBlock = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(3, btnEdit, btnBlock, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setMinWidth(84);
                pane.setMaxWidth(84);

                btnEdit.setGraphic(new FontIcon("fas-edit"));
                btnEdit.getStyleClass().addAll("button", "flat", "sm");
                btnEdit.setTooltip(new Tooltip("Editar"));
                btnEdit.setMinWidth(26);
                btnEdit.setMaxWidth(26);
                btnEdit.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    showEditForm(p);
                });

                btnBlock.getStyleClass().addAll("button", "flat", "sm");
                btnBlock.setTooltip(new Tooltip("Desactivar"));
                btnBlock.setMinWidth(26);
                btnBlock.setMaxWidth(26);
                btnBlock.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    handleToggleBlock(p);
                });

                btnDelete.setGraphic(new FontIcon("fas-trash"));
                btnDelete.getStyleClass().addAll("button", "flat", "danger", "sm");
                btnDelete.setTooltip(new Tooltip("Eliminar"));
                btnDelete.setMinWidth(26);
                btnDelete.setMaxWidth(26);
                btnDelete.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Paciente p = getTableView().getItems().get(getIndex());
                    if (p.getIsActivo() == 1) {
                        btnBlock.setGraphic(new FontIcon("fas-ban"));
                        btnBlock.getStyleClass().removeAll("success");
                        if (!btnBlock.getStyleClass().contains("danger")) {
                            btnBlock.getStyleClass().add("danger");
                        }
                        btnBlock.setTooltip(new Tooltip("Desactivar"));
                    } else {
                        btnBlock.setGraphic(new FontIcon("fas-unlock"));
                        btnBlock.getStyleClass().removeAll("danger");
                        if (!btnBlock.getStyleClass().contains("success")) {
                            btnBlock.getStyleClass().add("success");
                        }
                        btnBlock.setTooltip(new Tooltip("Reactivar"));
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleToggleBlock(Paciente p) {
        if (p.getIsActivo() == 1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Bloqueo");
            alert.setHeaderText("¿Está seguro de bloquear al paciente?");
            alert.setContentText(p.getNombres() + " " + p.getApellidos() + " no podrá recibir nuevas atenciones.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String resultMsg = pacienteAdapter.eliminarPaciente(p.getId());
                if ("OK".equals(resultMsg)) {
                    loadPatients();
                } else {
                    showAlert(Alert.AlertType.WARNING, "No se pudo bloquear", resultMsg);
                }
            }
        } else {
            String resultMsg = pacienteAdapter.reactivarPaciente(p.getId());
            if ("OK".equals(resultMsg)) {
                loadPatients();
            } else {
                showAlert(Alert.AlertType.WARNING, "No se pudo reactivar", resultMsg);
            }
        }
    }

    private void showEditForm(Paciente p) {
        currentPaciente = p;
        formTitle.setText("Editar Paciente");
        typeDocCombo.setValue(p.getTipoDocumento());
        numDocField.setText(p.getNumeroDocumento());
        firstNameField.setText(p.getNombres());
        lastNameField.setText(p.getApellidos());
        phoneField.setText(p.getTelefono());
        chkActivo.setSelected(p.getIsActivo() == 1);
        formOverlay.setVisible(true);
    }

    private void handleDelete(Paciente p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Baja");
        alert.setHeaderText("¿Está seguro de dar de baja al paciente?");
        alert.setContentText(p.getNombres() + " " + p.getApellidos());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String resultMsg = pacienteAdapter.eliminarPaciente(p.getId());
            if ("OK".equals(resultMsg)) {
                loadPatients();
            } else {
                showAlert(Alert.AlertType.WARNING, "No se pudo desactivar", resultMsg);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
