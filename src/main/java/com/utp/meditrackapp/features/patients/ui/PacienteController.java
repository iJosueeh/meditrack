package com.utp.meditrackapp.features.patients.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Paciente;
import com.utp.meditrackapp.features.patients.service.PacienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;

public class PacienteController {

    @FXML private TextField searchField;
    @FXML private TableView<Paciente> patientsTable;
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

    private final PacienteService pacienteService = new PacienteService();
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
        if (session.isTecnico()) {
            System.out.println("[AUTH] El Técnico tiene acceso limitado a edición/borrado.");
        }
    }

    private void setupTable() {
        colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        colNumDoc.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("isActivo"));

        setupEstadoColumn();
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

    private void setupForm() {
        typeDocCombo.setItems(FXCollections.observableArrayList("DNI", "CE"));
        typeDocCombo.getSelectionModel().selectFirst();
    }

    private void loadPatients() {
        try {
            List<Paciente> pacientes = pacienteService.listarPacientes();
            patientsTable.setItems(FXCollections.observableArrayList(pacientes));
            updateSummary();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los pacientes: " + e.getMessage());
        }
    }

    private void updateSummary() {
        javafx.application.Platform.runLater(() -> {
            try {
                lblTotalPatients.setText(String.valueOf(pacienteService.getContadorTotal()));
                lblTodayAttentions.setText(String.valueOf(pacienteService.getAtendidosHoy()));
                lblNewPatientsMonth.setText(String.valueOf(pacienteService.getNuevosDelMes()));
            } catch (Exception e) {
                System.err.println("[UI ERROR] Fallo al actualizar resumen: " + e.getMessage());
            }
        });
    }

    @FXML
    protected void onSearch() {
        String query = searchField.getText();
        List<Paciente> resultados = pacienteService.buscarPacientes(query);
        patientsTable.setItems(FXCollections.observableArrayList(resultados));
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

        String result = pacienteService.guardarPaciente(currentPaciente);

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
        Callback<TableColumn<Paciente, Void>, TableCell<Paciente, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Paciente, Void> call(final TableColumn<Paciente, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button();
                    private final Button btnDelete = new Button();
                    private final HBox pane = new HBox(btnEdit, btnDelete);

                    {
                        SessionManager session = SessionManager.getInstance();
                        pane.setSpacing(10);
                        pane.setAlignment(Pos.CENTER);
                        
                        btnEdit.setGraphic(new FontIcon("fas-edit"));
                        btnEdit.getStyleClass().addAll("button", "flat");
                        btnEdit.setTooltip(new Tooltip("Editar datos del paciente"));
                        btnEdit.setOnAction(event -> {
                            Paciente p = getTableView().getItems().get(getIndex());
                            showEditForm(p);
                        });

                        btnDelete.setGraphic(new FontIcon("fas-trash"));
                        btnDelete.getStyleClass().addAll("button", "flat", "danger");
                        btnDelete.setTooltip(new Tooltip("Dar de baja paciente"));
                        btnDelete.setOnAction(event -> {
                            Paciente p = getTableView().getItems().get(getIndex());
                            handleDelete(p);
                        });

                        if (session.isTecnico()) {
                            btnEdit.setVisible(false);
                            btnEdit.setManaged(false);
                            btnDelete.setVisible(false);
                            btnDelete.setManaged(false);
                        }
                        
                        if (session.isQuimico()) {
                            btnDelete.setVisible(false);
                            btnDelete.setManaged(false);
                        }
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        colActions.setCellFactory(cellFactory);
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
            if (pacienteService.eliminarPaciente(p.getId())) {
                loadPatients();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo dar de baja al paciente.");
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
