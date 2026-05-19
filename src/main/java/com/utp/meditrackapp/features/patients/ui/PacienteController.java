package com.utp.meditrackapp.features.patients.ui;

import com.utp.meditrackapp.core.models.entity.Paciente;
import com.utp.meditrackapp.features.patients.service.PacienteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
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
    @FXML private TableColumn<Paciente, Void> colActions;

    // Form Overlay Fields
    @FXML private VBox formOverlay;
    @FXML private Label formTitle;
    @FXML private ComboBox<String> typeDocCombo;
    @FXML private TextField numDocField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;

    private final PacienteService pacienteService = new PacienteService();
    private Paciente currentPaciente; // Para edición

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadPatients();
        applyRoleRestrictions();
    }

    private void applyRoleRestrictions() {
        SessionManager session = SessionManager.getInstance();
        
        // El Administrador y el Químico pueden ver todo.
        // El Técnico tiene restricciones en la tabla (se maneja en el cellFactory)
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

        addButtonToTable();
    }

    private void setupForm() {
        typeDocCombo.setItems(FXCollections.observableArrayList("DNI", "CE"));
        typeDocCombo.getSelectionModel().selectFirst();
    }

    private void loadPatients() {
        List<Paciente> pacientes = pacienteService.listarPacientes();
        patientsTable.setItems(FXCollections.observableArrayList(pacientes));
    }

    @FXML
    protected void onSearch() {
        String query = searchField.getText();
        List<Paciente> resultados = pacienteService.buscarPacientes(query);
        patientsTable.setItems(FXCollections.observableArrayList(resultados));
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
                        
                        // Configurar botón Editar
                        btnEdit.setGraphic(new FontIcon("fas-edit"));
                        btnEdit.getStyleClass().add("btn-icon-edit");
                        btnEdit.setOnAction(event -> {
                            Paciente p = getTableView().getItems().get(getIndex());
                            showEditForm(p);
                        });

                        // Configurar botón Eliminar
                        btnDelete.setGraphic(new FontIcon("fas-trash"));
                        btnDelete.getStyleClass().add("btn-icon-delete");
                        btnDelete.setOnAction(event -> {
                            Paciente p = getTableView().getItems().get(getIndex());
                            handleDelete(p);
                        });

                        // REGLA DE ROL: Solo Químico y Admin pueden editar/eliminar.
                        // El Técnico (Operativo) solo puede visualizar y registrar nuevos.
                        if (session.isTecnico()) {
                            btnEdit.setVisible(false);
                            btnEdit.setManaged(false);
                            btnDelete.setVisible(false);
                            btnDelete.setManaged(false);
                        }
                        
                        // REGLA DE ROL: El Químico puede editar pero NO eliminar (borrado es estratégico)
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
                            // Si es técnico, el panel de acciones estará vacío (oculto)
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
        formOverlay.setVisible(true);
    }

    private void handleDelete(Paciente p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de eliminar al paciente?");
        alert.setContentText(p.getNombres() + " " + p.getApellidos());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (pacienteService.eliminarPaciente(p.getId())) {
                loadPatients();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el paciente.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
